package com.study.api_gateway.common.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate Limiting 서비스
 * Bucket4j를 사용한 토큰 버킷 알고리즘 기반
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimitService {
	
	private final RateLimitProperties properties;
	
	/**
	 * 사용자별 버킷 저장소 (인메모리)
	 * 실제 분산 환경에서는 Redis 등의 분산 저장소 사용 권장
	 */
	private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
	
	/**
	 * 인증된 사용자의 Rate Limit 체크
	 */
	public RateLimitResult checkRateLimit(String userId) {
		String key = "user:" + userId;
		RateLimitProperties.BucketConfig config = properties.getAuthenticated();
		return doCheck(key, config);
	}
	
	/**
	 * 비인증 사용자(IP 기반)의 Rate Limit 체크
	 */
	public RateLimitResult checkRateLimitByIp(String clientIp) {
		String key = "ip:" + clientIp;
		RateLimitProperties.BucketConfig config = properties.getAnonymous();
		return doCheck(key, config);
	}
	
	/**
	 * 엔드포인트별 커스텀 Rate Limit 체크
	 */
	public RateLimitResult checkRateLimitForEndpoint(String identifier, String endpointPattern) {
		RateLimitProperties.BucketConfig config = properties.getEndpoints().get(endpointPattern);
		if (config == null) {
			// 커스텀 설정이 없으면 기본 설정 사용
			return checkRateLimit(identifier);
		}
		String key = "endpoint:" + endpointPattern + ":" + identifier;
		return doCheck(key, config);
	}
	
	/**
	 * 실제 Rate Limit 체크 로직
	 */
	private RateLimitResult doCheck(String key, RateLimitProperties.BucketConfig config) {
		Bucket bucket = buckets.computeIfAbsent(key, k -> createBucket(config));
		
		ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
		
		long resetSeconds = config.getRefillSeconds();
		
		if (probe.isConsumed()) {
			log.debug("Rate limit check passed: key={}, remaining={}", key, probe.getRemainingTokens());
			return RateLimitResult.allowed(probe.getRemainingTokens(), config.getCapacity(), resetSeconds);
		} else {
			long waitTimeMillis = probe.getNanosToWaitForRefill() / 1_000_000;
			log.warn("Rate limit exceeded: key={}, waitTime={}ms", key, waitTimeMillis);
			return RateLimitResult.denied(waitTimeMillis, config.getCapacity(), resetSeconds);
		}
	}
	
	/**
	 * Bucket 생성
	 */
	private Bucket createBucket(RateLimitProperties.BucketConfig config) {
		Bandwidth limit = Bandwidth.builder()
				.capacity(config.getCapacity())
				.refillGreedy(config.getRefillTokens(), Duration.ofSeconds(config.getRefillSeconds()))
				.build();
		
		return Bucket.builder()
				.addLimit(limit)
				.build();
	}
	
	/**
	 * 특정 키의 버킷 리셋 (관리용)
	 */
	public void resetBucket(String key) {
		buckets.remove(key);
		log.info("Bucket reset: key={}", key);
	}
	
	/**
	 * 모든 버킷 클리어 (관리용)
	 */
	public void clearAllBuckets() {
		buckets.clear();
		log.info("All buckets cleared");
	}
	
	/**
	 * 현재 버킷 수 조회 (모니터링용)
	 */
	public int getBucketCount() {
		return buckets.size();
	}
	
	/**
	 * Rate Limit 체크 결과
	 */
	public record RateLimitResult(
			boolean allowed,
			long remainingTokens,
			long waitTimeMillis,
			long limit,
			long resetTimeSeconds
	) {
		public static RateLimitResult allowed(long remaining, long limit, long resetSeconds) {
			return new RateLimitResult(true, remaining, 0, limit, resetSeconds);
		}
		
		public static RateLimitResult denied(long waitTime, long limit, long resetSeconds) {
			return new RateLimitResult(false, 0, waitTime, limit, resetSeconds);
		}
	}
}

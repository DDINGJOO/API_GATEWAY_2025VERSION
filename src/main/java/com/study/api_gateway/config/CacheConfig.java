package com.study.api_gateway.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.concurrent.TimeUnit;

/**
 * 캐시 설정
 * Caffeine 캐시를 사용한 로컬 캐싱 구성
 */
@Configuration
@EnableCaching
public class CacheConfig {
	
	/**
	 * Place 정보 캐시 설정
	 * - TTL: 10분 (Place 정보는 자주 변경되지 않음)
	 * - 최대 크기: 500개 항목
	 */
	@Bean(name = "placeCacheManager")
	public CacheManager placeCacheManager() {
		CaffeineCacheManager cacheManager = new CaffeineCacheManager("placeCache", "placeBatchCache");
		cacheManager.setCaffeine(Caffeine.newBuilder()
				.expireAfterWrite(10, TimeUnit.MINUTES)  // 10분 후 만료
				.maximumSize(500)  // 최대 500개 항목 캐싱
				.recordStats());  // 캐시 통계 기록
		return cacheManager;
	}
	
	/**
	 * 가격 정책 캐시 설정 (YeYakHaeYo 서버용)
	 * - TTL: 5분 (가격은 상대적으로 자주 변경될 수 있음)
	 * - 최대 크기: 1000개 항목
	 */
	@Bean
	public CacheManager pricingCacheManager() {
		CaffeineCacheManager cacheManager = new CaffeineCacheManager("pricingCache", "pricingBatchCache");
		cacheManager.setCaffeine(Caffeine.newBuilder()
				.expireAfterWrite(5, TimeUnit.MINUTES)  // 5분 후 만료
				.maximumSize(1000)  // 최대 1000개 항목 캐싱
				.recordStats());  // 캐시 통계 기록
		return cacheManager;
	}
	
	/**
	 * 기본 캐시 매니저 (기타 캐싱 용도)
	 */
	@Primary
	@Bean
	public CacheManager defaultCacheManager() {
		CaffeineCacheManager cacheManager = new CaffeineCacheManager();
		cacheManager.setCaffeine(Caffeine.newBuilder()
				.expireAfterWrite(5, TimeUnit.MINUTES)
				.maximumSize(100));
		return cacheManager;
	}
}

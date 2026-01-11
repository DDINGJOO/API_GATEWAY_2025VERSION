package com.study.api_gateway.common.monitoring.endpoint;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * API Gateway 커스텀 Actuator 엔드포인트
 * /actuator/gateway 로 접근 가능
 */
@Component
@Endpoint(id = "gateway")
@RequiredArgsConstructor
public class ApiGatewayEndpoint {

	private final MeterRegistry meterRegistry;
	private final Instant startTime = Instant.now();

	@ReadOperation
	public Map<String, Object> gatewayInfo() {
		Map<String, Object> info = new LinkedHashMap<>();

		// 기본 정보
		info.put("name", "API Gateway");
		info.put("version", "1.0.0");
		info.put("uptime", getUptime());

		// JVM 정보
		info.put("jvm", getJvmInfo());

		// 요청 통계
		info.put("requests", getRequestStats());

		// Rate Limiting 통계
		info.put("rateLimiting", getRateLimitingStats());

		// 인증 통계
		info.put("authentication", getAuthStats());

		// 캐시 통계
		info.put("cache", getCacheStats());

		return info;
	}

	private Map<String, Object> getUptime() {
		Map<String, Object> uptime = new LinkedHashMap<>();
		Duration duration = Duration.between(startTime, Instant.now());

		uptime.put("startTime", startTime.toString());
		uptime.put("days", duration.toDays());
		uptime.put("hours", duration.toHours() % 24);
		uptime.put("minutes", duration.toMinutes() % 60);
		uptime.put("seconds", duration.getSeconds() % 60);
		uptime.put("formatted", formatDuration(duration));

		return uptime;
	}

	private String formatDuration(Duration duration) {
		long days = duration.toDays();
		long hours = duration.toHours() % 24;
		long minutes = duration.toMinutes() % 60;
		long seconds = duration.getSeconds() % 60;

		StringBuilder sb = new StringBuilder();
		if (days > 0) sb.append(days).append("d ");
		if (hours > 0) sb.append(hours).append("h ");
		if (minutes > 0) sb.append(minutes).append("m ");
		sb.append(seconds).append("s");

		return sb.toString().trim();
	}

	private Map<String, Object> getJvmInfo() {
		Map<String, Object> jvm = new LinkedHashMap<>();

		RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
		MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();

		jvm.put("version", System.getProperty("java.version"));
		jvm.put("vendor", System.getProperty("java.vendor"));

		// 메모리 정보
		Map<String, Object> memory = new LinkedHashMap<>();
		long heapUsed = memoryMXBean.getHeapMemoryUsage().getUsed();
		long heapMax = memoryMXBean.getHeapMemoryUsage().getMax();
		long nonHeapUsed = memoryMXBean.getNonHeapMemoryUsage().getUsed();

		memory.put("heapUsed", formatBytes(heapUsed));
		memory.put("heapMax", formatBytes(heapMax));
		memory.put("heapUsedPercentage", String.format("%.2f%%", (double) heapUsed / heapMax * 100));
		memory.put("nonHeapUsed", formatBytes(nonHeapUsed));

		jvm.put("memory", memory);

		// 스레드 정보
		jvm.put("threads", ManagementFactory.getThreadMXBean().getThreadCount());

		return jvm;
	}

	private String formatBytes(long bytes) {
		if (bytes < 1024) return bytes + " B";
		if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
		if (bytes < 1024 * 1024 * 1024) return String.format("%.2f MB", bytes / (1024.0 * 1024));
		return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
	}

	private Map<String, Object> getRequestStats() {
		Map<String, Object> stats = new LinkedHashMap<>();

		// Total requests (from http.server.requests)
		double totalRequests = 0;
		double totalErrors = 0;

		for (Counter counter : meterRegistry.find("api_gateway_requests_total").counters()) {
			totalRequests += counter.count();
		}

		for (Counter counter : meterRegistry.find("api_gateway_errors_total").counters()) {
			totalErrors += counter.count();
		}

		stats.put("totalRequests", (long) totalRequests);
		stats.put("totalErrors", (long) totalErrors);
		stats.put("errorRate", totalRequests > 0 ? String.format("%.2f%%", (totalErrors / totalRequests) * 100) : "0%");

		// Active requests
		meterRegistry.find("api_gateway_active_requests").gauges().forEach(gauge -> {
			stats.put("activeRequests", (long) gauge.value());
		});

		return stats;
	}

	private Map<String, Object> getRateLimitingStats() {
		Map<String, Object> stats = new LinkedHashMap<>();

		double rateLimited = 0;
		for (Counter counter : meterRegistry.find("api_gateway_rate_limited_requests_total").counters()) {
			rateLimited += counter.count();
		}

		stats.put("rateLimitedRequests", (long) rateLimited);

		return stats;
	}

	private Map<String, Object> getAuthStats() {
		Map<String, Object> stats = new LinkedHashMap<>();

		double authSuccess = 0;
		double authFailure = 0;

		for (Counter counter : meterRegistry.find("api_gateway_auth_success_total").counters()) {
			authSuccess += counter.count();
		}

		for (Counter counter : meterRegistry.find("api_gateway_auth_failure_total").counters()) {
			authFailure += counter.count();
		}

		stats.put("successCount", (long) authSuccess);
		stats.put("failureCount", (long) authFailure);
		stats.put("successRate", (authSuccess + authFailure) > 0 ?
				String.format("%.2f%%", (authSuccess / (authSuccess + authFailure)) * 100) : "N/A");

		return stats;
	}

	private Map<String, Object> getCacheStats() {
		Map<String, Object> stats = new LinkedHashMap<>();

		double cacheHits = 0;
		double cacheMisses = 0;

		for (Counter counter : meterRegistry.find("api_gateway_cache_hits_total").counters()) {
			cacheHits += counter.count();
		}

		for (Counter counter : meterRegistry.find("api_gateway_cache_misses_total").counters()) {
			cacheMisses += counter.count();
		}

		stats.put("hits", (long) cacheHits);
		stats.put("misses", (long) cacheMisses);
		stats.put("hitRate", (cacheHits + cacheMisses) > 0 ?
				String.format("%.2f%%", (cacheHits / (cacheHits + cacheMisses)) * 100) : "N/A");

		return stats;
	}
}

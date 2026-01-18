package com.study.api_gateway.common.monitoring.health;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * 외부 서비스 Health Check를 위한 기본 클래스
 */
@Slf4j
public abstract class ExternalServiceHealthIndicator implements ReactiveHealthIndicator {
	
	protected final WebClient webClient;
	protected final String serviceName;
	protected final String healthCheckPath;
	protected final Duration timeout;
	
	protected ExternalServiceHealthIndicator(WebClient webClient, String serviceName, String healthCheckPath) {
		this.webClient = webClient;
		this.serviceName = serviceName;
		this.healthCheckPath = healthCheckPath;
		this.timeout = Duration.ofSeconds(5);
	}
	
	protected ExternalServiceHealthIndicator(WebClient webClient, String serviceName, String healthCheckPath, Duration timeout) {
		this.webClient = webClient;
		this.serviceName = serviceName;
		this.healthCheckPath = healthCheckPath;
		this.timeout = timeout;
	}
	
	@Override
	public Mono<Health> health() {
		long startTime = System.currentTimeMillis();
		
		return webClient.get()
				.uri(healthCheckPath)
				.retrieve()
				.toBodilessEntity()
				.map(response -> {
					long responseTime = System.currentTimeMillis() - startTime;
					log.debug("[Health Check] {} - Status: UP, Response Time: {}ms", serviceName, responseTime);
					
					return Health.up()
							.withDetail("service", serviceName)
							.withDetail("status", "UP")
							.withDetail("responseTime", responseTime + "ms")
							.build();
				})
				.timeout(timeout)
				.onErrorResume(error -> {
					long responseTime = System.currentTimeMillis() - startTime;
					log.warn("[Health Check] {} - Status: DOWN, Error: {}, Response Time: {}ms",
							serviceName, error.getMessage(), responseTime);
					
					return Mono.just(Health.down()
							.withDetail("service", serviceName)
							.withDetail("status", "DOWN")
							.withDetail("error", error.getMessage())
							.withDetail("responseTime", responseTime + "ms")
							.build());
				});
	}
}

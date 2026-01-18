package com.study.api_gateway.common.monitoring.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Micrometer 메트릭 설정
 */
@Configuration
public class MetricsConfig {
	
	/**
	 * 공통 태그 설정
	 */
	@Bean
	public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
		return registry -> registry.config()
				.commonTags(Tags.of(
						Tag.of("service", "api-gateway"),
						Tag.of("env", System.getProperty("spring.profiles.active", "default"))
				));
	}
}

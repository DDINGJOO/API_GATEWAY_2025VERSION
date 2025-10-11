package com.study.api_gateway.config;

import org.springframework.context.annotation.Configuration;

/**
 * Central application configuration.
 *
 * Note:
 * - ProfileCache beans are provided by component-scanned classes with proper conditions:
 *   - RedisProfileCache is registered only when Redis is available and explicitly enabled.
 *   - NoopProfileCache is the fallback when no ProfileCache bean exists.
 *
 * This configuration class intentionally declares no ProfileCache @Bean to avoid
 * interfering with conditional bean detection and to ensure the fallback works.
 */
@Configuration
public class CustomConfig {
	// Intentionally left without @Bean methods for ProfileCache
}

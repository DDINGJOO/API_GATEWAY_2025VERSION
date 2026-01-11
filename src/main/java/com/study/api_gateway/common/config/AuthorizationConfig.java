package com.study.api_gateway.common.config;

import lombok.Getter;
import org.springframework.context.annotation.Configuration;

/**
 * 경로 기반 인가 설정
 * <p>
 * 사용법:
 * 1. requireRole() - 특정 역할이 필요한 경로 설정
 * 2. requireAnyRole() - 여러 역할 중 하나라도 있으면 접근 가능
 * 3. requireAllRoles() - 모든 역할이 필요한 경로 설정
 * <p>
 * 예시:
 * - /bff/v1/admin/** -> ADMIN 역할 필요
 * - /bff/v1/place/owner/** -> PLACE_OWNER 역할 필요
 * - /bff/v1/users/me -> 인증만 필요 (모든 역할 허용)
 */
@Configuration
@Getter
public class AuthorizationConfig {
	
	public AuthorizationConfig() {
		configureAuthorization();
	}
	
	private void configureAuthorization() {
		// ===== ADMIN 전용 경로 =====
		// 예시: requireRole("/bff/v1/admin/**", Role.ADMIN);
		
		// ===== PLACE_OWNER 전용 경로 =====
		// 예시: requireRole("/bff/v1/place/owner/**", Role.PLACE_OWNER);
		
		// ===== 다중 역할 허용 경로 =====
		// 예시: requireAnyRole("/bff/v1/shared/**", Role.ADMIN, Role.PLACE_OWNER);
		
		// ===== 모든 역할 필요 경로 (특수 케이스) =====
		// 예시: requireAllRoles("/bff/v1/special/**", Role.ADMIN, Role.USER);
	}
	
	
	/**
	 * 역할 매칭 타입
	 */
	public enum RoleMatchType {
		ANY,  // 하나라도 일치하면 OK
		ALL   // 모두 일치해야 OK
	}
	
}

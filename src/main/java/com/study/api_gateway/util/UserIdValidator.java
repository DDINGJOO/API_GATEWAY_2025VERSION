package com.study.api_gateway.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

/**
 * 사용자 ID 검증 유틸리티
 * <p>
 * JWT 토큰에서 추출된 userId(X-User-Id 헤더)와 요청에서 주장하는 userId가 일치하는지 검증합니다.
 * 이를 통해 다른 사용자를 사칭하여 작업을 수행하는 것을 방지합니다.
 * </p>
 */
@Slf4j
@Component
public class UserIdValidator {

	/**
	 * 토큰의 userId와 요청의 userId가 일치하는지 검증 (동기 버전)
	 * <p>
	 * 단순 문자열 비교만 수행하므로 블로킹 없이 빠르게 동작합니다.
	 * Reactive chain 밖에서 호출해도 성능 영향은 미미합니다 (~10-50ns).
	 * </p>
	 *
	 * @param request ServerHttpRequest (X-User-Id 헤더 포함)
	 * @param claimedUserId 요청에서 주장하는 userId
	 * @throws ResponseStatusException userId 불일치 시
	 */
	public void validate(ServerHttpRequest request, String claimedUserId) {
		String tokenUserId = extractTokenUserId(request);

		if (tokenUserId == null || tokenUserId.isEmpty()) {
			log.error("X-User-Id header is missing in request to {}", request.getPath());
			throw new ResponseStatusException(
					HttpStatus.UNAUTHORIZED,
					"인증 정보가 없습니다"
			);
		}

		if (claimedUserId == null || claimedUserId.isEmpty()) {
			log.warn("Claimed userId is null or empty for token userId: {}", tokenUserId);
			throw new ResponseStatusException(
					HttpStatus.BAD_REQUEST,
					"사용자 ID가 필요합니다"
			);
		}

		if (!tokenUserId.equals(claimedUserId)) {
			log.warn("User ID mismatch - Token: {}, Claimed: {}, Path: {}",
					tokenUserId, claimedUserId, request.getPath());
			throw new ResponseStatusException(
					HttpStatus.FORBIDDEN,
					"본인만 이 작업을 수행할 수 있습니다"
			);
		}

		log.debug("User ID validated successfully: {}", tokenUserId);
	}

	/**
	 * 토큰의 userId와 요청의 userId가 일치하는지 검증 (Reactive 버전)
	 * <p>
	 * Reactive chain 안에서 사용하기 위한 버전입니다.
	 * 에러 처리가 Reactive 스트림 내에서 이루어지므로 onError, retry 등의 연산자를 활용할 수 있습니다.
	 * </p>
	 *
	 * @param request ServerHttpRequest (X-User-Id 헤더 포함)
	 * @param claimedUserId 요청에서 주장하는 userId
	 * @return Mono&lt;Void&gt; - 성공 시 empty, 실패 시 error
	 */
	public Mono<Void> validateReactive(ServerHttpRequest request, String claimedUserId) {
		return Mono.defer(() -> {
			String tokenUserId = extractTokenUserId(request);

			if (tokenUserId == null || tokenUserId.isEmpty()) {
				log.error("X-User-Id header is missing in request to {}", request.getPath());
				return Mono.error(new ResponseStatusException(
						HttpStatus.UNAUTHORIZED,
						"인증 정보가 없습니다"
				));
			}

			if (claimedUserId == null || claimedUserId.isEmpty()) {
				log.warn("Claimed userId is null or empty for token userId: {}", tokenUserId);
				return Mono.error(new ResponseStatusException(
						HttpStatus.BAD_REQUEST,
						"사용자 ID가 필요합니다"
				));
			}

			if (!tokenUserId.equals(claimedUserId)) {
				log.warn("User ID mismatch - Token: {}, Claimed: {}, Path: {}",
						tokenUserId, claimedUserId, request.getPath());
				return Mono.error(new ResponseStatusException(
						HttpStatus.FORBIDDEN,
						"본인만 이 작업을 수행할 수 있습니다"
				));
			}

			log.debug("User ID validated successfully: {}", tokenUserId);
			return Mono.empty();
		});
	}

	/**
	 * 토큰에서 userId 추출
	 *
	 * @param request ServerHttpRequest
	 * @return userId (X-User-Id 헤더 값), 없으면 null
	 */
	public String extractTokenUserId(ServerHttpRequest request) {
		return request.getHeaders().getFirst("X-User-Id");
	}

	/**
	 * userId 추출 및 검증 (Reactive 버전)
	 * <p>
	 * userId를 추출하고 검증한 뒤, 성공 시 userId를 반환합니다.
	 * 이후 로직에서 userId가 필요한 경우 사용하면 편리합니다.
	 * </p>
	 *
	 * @param request ServerHttpRequest
	 * @param claimedUserId 요청에서 주장하는 userId
	 * @return Mono&lt;String&gt; - 검증된 userId
	 */
	public Mono<String> extractAndValidate(ServerHttpRequest request, String claimedUserId) {
		return Mono.defer(() -> {
			String tokenUserId = extractTokenUserId(request);

			if (tokenUserId == null || tokenUserId.isEmpty()) {
				log.error("X-User-Id header is missing in request to {}", request.getPath());
				return Mono.error(new ResponseStatusException(
						HttpStatus.UNAUTHORIZED,
						"인증 정보가 없습니다"
				));
			}

			if (claimedUserId == null || claimedUserId.isEmpty()) {
				log.warn("Claimed userId is null or empty for token userId: {}", tokenUserId);
				return Mono.error(new ResponseStatusException(
						HttpStatus.BAD_REQUEST,
						"사용자 ID가 필요합니다"
				));
			}

			if (!tokenUserId.equals(claimedUserId)) {
				log.warn("User ID mismatch - Token: {}, Claimed: {}, Path: {}",
						tokenUserId, claimedUserId, request.getPath());
				return Mono.error(new ResponseStatusException(
						HttpStatus.FORBIDDEN,
						"권한이 없습니다"
				));
			}

			log.debug("User ID validated successfully: {}", tokenUserId);
			return Mono.just(tokenUserId);
		});
	}

	/**
	 * 리소스 소유자 검증
	 * <p>
	 * 조회한 리소스의 소유자(writerId 등)와 현재 사용자가 일치하는지 검증합니다.
	 * 수정/삭제 작업 전에 사용합니다.
	 * </p>
	 *
	 * @param request ServerHttpRequest
	 * @param resourceOwnerId 리소스의 실제 소유자 ID
	 * @param resourceName 리소스 이름 (에러 메시지용, 예: "게시글", "댓글")
	 * @return Mono&lt;Void&gt; - 성공 시 empty, 실패 시 error
	 */
	public Mono<Void> validateOwnership(ServerHttpRequest request, String resourceOwnerId, String resourceName) {
		return Mono.defer(() -> {
			String tokenUserId = extractTokenUserId(request);

			if (tokenUserId == null || tokenUserId.isEmpty()) {
				log.error("X-User-Id header is missing in request to {}", request.getPath());
				return Mono.error(new ResponseStatusException(
						HttpStatus.UNAUTHORIZED,
						"인증 정보가 없습니다"
				));
			}

			if (resourceOwnerId == null || resourceOwnerId.isEmpty()) {
				log.warn("Resource owner ID is null or empty");
				return Mono.error(new ResponseStatusException(
						HttpStatus.INTERNAL_SERVER_ERROR,
						"리소스 소유자 정보를 찾을 수 없습니다"
				));
			}

			if (!tokenUserId.equals(resourceOwnerId)) {
				log.warn("Ownership validation failed - Token: {}, Resource Owner: {}, Resource: {}, Path: {}",
						tokenUserId, resourceOwnerId, resourceName, request.getPath());
				return Mono.error(new ResponseStatusException(
						HttpStatus.FORBIDDEN,
						String.format("본인의 %s만 수정/삭제할 수 있습니다", resourceName)
				));
			}

			log.debug("Ownership validated successfully - User: {}, Resource: {}", tokenUserId, resourceName);
			return Mono.empty();
		});
	}
}
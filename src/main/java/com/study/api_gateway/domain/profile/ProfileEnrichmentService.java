package com.study.api_gateway.domain.profile;

import com.study.api_gateway.domain.profile.cache.ProfileCache;
import com.study.api_gateway.domain.profile.dto.BatchUserSummaryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 프로필 보강 유틸리티
 * - 응답 맵에서 userId 또는 writerId를 찾아 닉네임, 프로필 이미지 URL을 채워 넣습니다.
 * - 향후 Redis 캐싱을 고려하여 설계되었으며, 현재는 Noop 캐시를 통해 항상 미스 처리됩니다.
 * <p>
 * 처리 파이프라인:
 * 1) 페이로드에 포함된 모든 사용자 ID 수집 (중복 제거)
 * 2) 캐시 조회 (향후 Redis)로 존재하는 항목 로드
 * 3) 캐시에 없는 ID만 프로필 서버 배치 API로 조회
 * 4) 캐시 결과와 API 결과를 머지 후, 대상 응답에 닉네임/프로필 이미지를 주입
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ProfileEnrichmentService {
	
	// 유저 관련 ID로 추정되는 키 목록 (확장 가능)
	private static final Set<String> USER_ID_KEYS = Set.of(
			"userId", "writerId", "ownerId", "creatorId", "likerId", "senderId", "receiverId", "reporterId", "reportedId"
	);
	
	// 프로필 기본값: 닉네임/이미지 URL이 없을 때 응답에 채워넣는 값
	private static final String DEFAULT_NICKNAME = "상어크앙";
	private static final String DEFAULT_PROFILE_IMAGE_URL = "와방쌘 상어";
	
	private static final int BATCH_SIZE = 200; // 대량 방어용 내부 분할 크기
	private static final int SOFT_CAP = 5000; // 너무 큰 요청에 대한 소프트 상한
	
	private final ProfileClient profileClient;
	private final ProfileCache profileCache; // placeholder for future Redis integration
	
	/**
	 * 항목 리스트 보강
	 * - 각 맵에서 writerId 또는 userId를 추출하여 프로필 정보를 배치 조회/캐시 조회 후 주입합니다.
	 * - 중복된 사용자 ID는 한 번만 조회합니다.
	 *
	 * @param items writerId 또는 userId 필드를 포함할 수 있는 맵 리스트
	 * @return 닉네임(nickname), 프로필 이미지(profileImageUrl)가 주입된 리스트를 포함하는 Mono
	 */
	public Mono<List<Map<String, Object>>> enrichItemList(List<Map<String, Object>> items) {
		if (items == null || items.isEmpty()) return Mono.just(items == null ? List.of() : items);
		Set<String> userIds = new LinkedHashSet<>();
		for (Map<String, Object> m : items) {
			collectUserIds(m, userIds);
		}
		if (userIds.isEmpty()) return Mono.just(items);
		return loadProfiles(userIds)
				.map(profileMap -> {
					for (Map<String, Object> m : items) {
						applyProfile(m, profileMap);
					}
					return items;
				});
	}
	
	/**
	 * 단일 게시글 보강
	 * - 단일 게시글 맵에서 writerId를 찾아 프로필 정보를 주입합니다.
	 *
	 * @param article 게시글 본문 맵 (writerId 포함 가능)
	 * @return nickname, profileImageUrl이 주입된 게시글 맵을 포함하는 Mono
	 */
	public Mono<Map<String, Object>> enrichArticle(Map<String, Object> article) {
		if (article == null) return Mono.just(new LinkedHashMap<>());
		Set<String> userIds = new LinkedHashSet<>();
		collectUserIds(article, userIds);
		if (userIds.isEmpty()) return Mono.just(article);
		return loadProfiles(userIds)
				.map(profileMap -> {
					applyProfile(article, profileMap);
					return article;
				});
	}
	
	/**
	 * 게시글 및 댓글 트리 보강
	 * - 단일 게시글 맵과, 대댓글(replies)을 포함할 수 있는 댓글 리스트를 함께 보강합니다.
	 * - 게시글/댓글/대댓글에서 writerId 또는 userId를 모두 수집하여 한 번에 조회합니다.
	 *
	 * @param article  게시글 본문 맵 (writerId 포함 가능)
	 * @param comments 댓글 리스트 (각 항목에 writerId 포함 가능, 대댓글은 replies 필드 하위에 존재)
	 * @return "article", "comments" 키를 가진 결과 맵을 포함하는 Mono (각 항목에 nickname, profileImageUrl 주입)
	 */
	public Mono<Map<String, Object>> enrichArticleAndComments(Map<String, Object> article,
	                                                          List<Map<String, Object>> comments) {
		Set<String> userIds = new LinkedHashSet<>();
		if (article != null) collectUserIds(article, userIds);
		if (comments != null) {
			for (Map<String, Object> c : comments) collectUserIdsRecursive(c, userIds);
		}
		if (userIds.isEmpty()) {
			return Mono.just(buildArticleWithComments(article, comments));
		}
		return loadProfiles(userIds)
				.map(profileMap -> {
					if (article != null) applyProfile(article, profileMap);
					if (comments != null) for (Map<String, Object> c : comments) applyProfileRecursive(c, profileMap);
					return buildArticleWithComments(article, comments);
				});
	}
	
	/**
	 * 프로필 로딩 (캐시 + 배치 API)
	 * - 전달된 userIds를 캐시에서 먼저 조회하고, 누락분만 배치 API로 조회한 뒤 병합합니다.
	 *
	 * @param userIds 조회 대상 사용자 ID 집합
	 * @return userId -> BatchUserSummaryResponse 맵을 포함하는 Mono
	 */
	private Mono<Map<String, BatchUserSummaryResponse>> loadProfiles(Set<String> userIds) {
		if (userIds == null || userIds.isEmpty()) return Mono.just(Map.of());
		// 너무 큰 요청에 대해서는 소프트 상한을 적용하고 경고 로그만 남김
		final Set<String> idsToUse;
		if (userIds.size() > SOFT_CAP) {
			log.warn("profile enrichment requested for too many userIds: size={} > cap={}, truncating", userIds.size(), SOFT_CAP);
			idsToUse = userIds.stream().limit(SOFT_CAP).collect(Collectors.toCollection(LinkedHashSet::new));
		} else {
			idsToUse = new LinkedHashSet<>(userIds);
		}
		return profileCache.getAll(idsToUse)
				.onErrorResume(e -> {
					log.warn("profile cache getAll failed: {}", e.toString());
					return Mono.just(Map.of());
				})
				.defaultIfEmpty(Map.of())
				.flatMap(cached -> {
					Set<String> missing = new LinkedHashSet<>(idsToUse);
					missing.removeAll(cached.keySet());
					Mono<Map<String, BatchUserSummaryResponse>> fetchedMono;
					if (missing.isEmpty()) {
						fetchedMono = Mono.just(Map.of());
					} else {
						fetchedMono = fetchInBatches(new ArrayList<>(missing))
								.defaultIfEmpty(List.of())
								.map(list -> list.stream()
										.filter(Objects::nonNull)
										.collect(Collectors.toMap(BatchUserSummaryResponse::getUserId, Function.identity(), (a, b) -> a)))
								// 캐시 저장은 응답 체인과 분리하여 비동기로 처리합니다.
								// 즉시 map을 반환하여 응답 구성을 진행하고, putAll은 fire-and-forget으로 수행합니다.
								.doOnNext(map -> profileCache.putAll(map)
										.doOnError(e -> log.warn("failed to write profiles to cache: {}", e.toString()))
										.subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic())
										.subscribe());
					}
					return fetchedMono.map(fetched -> {
						if (cached.isEmpty()) return fetched;
						if (fetched.isEmpty()) return cached;
						Map<String, BatchUserSummaryResponse> merged = new LinkedHashMap<>(cached);
						merged.putAll(fetched);
						return merged;
					});
				});
	}
	
	private Mono<List<BatchUserSummaryResponse>> fetchInBatches(List<String> ids) {
		if (ids == null || ids.isEmpty()) return Mono.just(List.of());
		List<List<String>> parts = new ArrayList<>();
		for (int i = 0; i < ids.size(); i += BATCH_SIZE) {
			parts.add(ids.subList(i, Math.min(i + BATCH_SIZE, ids.size())));
		}
		return Flux.fromIterable(parts)
				.concatMap(profileClient::fetchUserSummariesBatch)
				.collectList()
				.map(listOfLists -> listOfLists.stream().filter(java.util.Objects::nonNull).flatMap(java.util.Collection::stream).collect(java.util.stream.Collectors.toList()));
	}
	
	/**
	 * 게시글과 댓글 리스트를 하나의 응답 맵으로 구성합니다.
	 *
	 * @param article  게시글 맵
	 * @param comments 댓글 리스트
	 * @return {"article": article, "comments": comments} 형태의 맵
	 */
	private Map<String, Object> buildArticleWithComments(Map<String, Object> article, List<Map<String, Object>> comments) {
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("article", article);
		result.put("comments", comments == null ? List.of() : comments);
		return result;
	}
	
	/**
	 * 단일 맵에서 writerId 또는 userId를 찾아 수집합니다.
	 *
	 * @param m   대상 맵
	 * @param acc 수집 대상 Set (중복 방지)
	 */
	private void collectUserIds(Map<String, Object> m, Set<String> acc) {
		if (m == null) return;
		for (String key : USER_ID_KEYS) {
			Object val = m.get(key);
			if (val != null) {
				acc.add(String.valueOf(val));
				break;
			}
		}
	}
	
	/**
	 * 댓글 트리에서 재귀적으로 사용자 ID를 수집합니다.
	 * - replies 필드를 따라 내려가며 각 맵의 writerId/userId를 수집합니다.
	 *
	 * @param m   현재 노드 맵
	 * @param acc 수집 대상 Set (중복 방지)
	 */
	@SuppressWarnings("unchecked")
	private void collectUserIdsRecursive(Map<String, Object> m, Set<String> acc) {
		collectUserIds(m, acc);
		Object replies = m == null ? null : m.get("replies");
		if (replies instanceof List<?> list) {
			for (Object o : list) {
				if (o instanceof Map<?, ?> mm) {
					collectUserIdsRecursive((Map<String, Object>) mm, acc);
				}
			}
		}
	}
	
	/**
	 * 단일 맵에 프로필 정보를 주입합니다.
	 * - writerId 또는 userId를 기준으로 nickname, profileImageUrl을 설정합니다.
	 *
	 * @param m          대상 맵
	 * @param profileMap userId -> 프로필 응답 맵
	 */
	private void applyProfile(Map<String, Object> m, Map<String, BatchUserSummaryResponse> profileMap) {
		if (m == null || profileMap == null) return;
		String uid = null;
		for (String key : USER_ID_KEYS) {
			Object v = m.get(key);
			if (v != null) {
				uid = String.valueOf(v);
				break;
			}
		}
		if (uid == null) return;
		BatchUserSummaryResponse p = profileMap.get(uid);
		// 닉네임/프로필 이미지가 없을 때 기본값으로 채웁니다.
		String nickname = (p == null || isBlank(p.getNickname())) ? DEFAULT_NICKNAME : p.getNickname();
		String imageUrl = (p == null || isBlank(p.getProfileImageUrl())) ? DEFAULT_PROFILE_IMAGE_URL : p.getProfileImageUrl();
		m.put("nickname", nickname);
		m.put("profileImageUrl", imageUrl);
	}
	
	/**
	 * 댓글 트리 전반에 프로필 정보를 재귀적으로 주입합니다.
	 * - 현재 노드에 주입 후, replies 하위의 모든 맵에도 동일하게 적용합니다.
	 *
	 * @param m          현재 노드 맵
	 * @param profileMap userId -> 프로필 응답 맵
	 */
	@SuppressWarnings("unchecked")
	private void applyProfileRecursive(Map<String, Object> m, Map<String, BatchUserSummaryResponse> profileMap) {
		applyProfile(m, profileMap);
		Object replies = m == null ? null : m.get("replies");
		if (replies instanceof List<?> list) {
			for (Object o : list) {
				if (o instanceof Map<?, ?> mm) {
					applyProfileRecursive((Map<String, Object>) mm, profileMap);
				}
			}
		}
	}
	
	/**
	 * 임의의 객체(맵/리스트 구조)를 스캔하여 userId/writerId 등으로 추정되는 값을 찾고 보강합니다.
	 * - 맵/리스트의 중첩 구조를 모두 순회합니다.
	 */
	public Mono<Object> enrichAny(Object data) {
		if (data == null) return Mono.justOrEmpty(data);
		Set<String> userIds = new LinkedHashSet<>();
		collectUserIdsDeep(data, userIds);
		if (userIds.isEmpty()) return Mono.just(data);
		return loadProfiles(userIds)
				.map(profileMap -> {
					injectProfilesDeep(data, profileMap);
					return data;
				});
	}
	
	@SuppressWarnings("unchecked")
	private void collectUserIdsDeep(Object node, Set<String> acc) {
		if (node == null) return;
		if (node instanceof Map<?, ?> mm) {
			Map<String, Object> m = (Map<String, Object>) mm;
			// 현재 맵에서 수집
			for (String key : USER_ID_KEYS) {
				Object val = m.get(key);
				if (val != null) {
					acc.add(String.valueOf(val));
					// 한 맵에 여러 키가 있어도 우선 하나만 등록하고 계속 순회
					break;
				}
			}
			// 자식 값 순회
			for (Object v : m.values()) {
				if (v instanceof Map<?, ?> || v instanceof List<?>) {
					collectUserIdsDeep(v, acc);
				}
			}
		} else if (node instanceof List<?> list) {
			for (Object elem : list) {
				if (elem instanceof Map<?, ?> || elem instanceof List<?>) {
					collectUserIdsDeep(elem, acc);
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private void injectProfilesDeep(Object node, Map<String, BatchUserSummaryResponse> profileMap) {
		if (node == null || profileMap == null || profileMap.isEmpty()) return;
		if (node instanceof Map<?, ?> mm) {
			Map<String, Object> m = (Map<String, Object>) mm;
			String uid = null;
			for (String key : USER_ID_KEYS) {
				Object v = m.get(key);
				if (v != null) {
					uid = String.valueOf(v);
					break;
				}
			}
			if (uid != null) {
				BatchUserSummaryResponse p = profileMap.get(uid);
				String nickname = (p == null || isBlank(p.getNickname())) ? DEFAULT_NICKNAME : p.getNickname();
				String imageUrl = (p == null || isBlank(p.getProfileImageUrl())) ? DEFAULT_PROFILE_IMAGE_URL : p.getProfileImageUrl();
				m.put("nickname", nickname);
				m.put("profileImageUrl", imageUrl);
			}
			for (Object v : m.values()) {
				if (v instanceof Map<?, ?> || v instanceof List<?>) {
					injectProfilesDeep(v, profileMap);
				}
			}
		} else if (node instanceof List<?> list) {
			for (Object elem : list) {
				if (elem instanceof Map<?, ?> || elem instanceof List<?>) {
					injectProfilesDeep(elem, profileMap);
				}
			}
		}
	}
	
	private boolean isBlank(String s) {
		return s == null || s.trim().isEmpty();
	}
	
}

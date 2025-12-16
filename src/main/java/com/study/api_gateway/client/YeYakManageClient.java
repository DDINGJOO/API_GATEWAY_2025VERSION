package com.study.api_gateway.client;

import com.study.api_gateway.dto.reservationManage.enums.ReservationStatus;
import com.study.api_gateway.dto.reservationManage.request.ReservationCreateRequest;
import com.study.api_gateway.dto.reservationManage.request.UserInfoUpdateRequest;
import com.study.api_gateway.dto.reservationManage.response.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Set;

/**
 * YeYakManage Server와 통신하는 WebClient 기반 클라이언트
 * 예약 관리 API 제공
 */
@Component
public class YeYakManageClient {
	private final WebClient webClient;
	private final String PREFIX = "/api/v1/reservations";
	
	public YeYakManageClient(@Qualifier("yeYakManageWebClient") WebClient webClient) {
		this.webClient = webClient;
	}
	
	/**
	 * 예약 생성 (예약자 정보 업데이트)
	 * POST /api/v1/reservations
	 */
	public Mono<ReservationCreateResponse> createReservation(ReservationCreateRequest request) {
		return webClient.post()
				.uri(uriBuilder -> uriBuilder
						.path(PREFIX)
						.build())
				.bodyValue(request)
				.retrieve()
				.bodyToMono(ReservationCreateResponse.class);
	}
	
	/**
	 * 예약 상세 조회
	 * GET /api/v1/reservations/{id}
	 */
	public Mono<ReservationDetailResponse> getReservationById(Long id) {
		return webClient.get()
				.uri(uriBuilder -> uriBuilder
						.path(PREFIX + "/{id}")
						.build(id))
				.retrieve()
				.bodyToMono(ReservationDetailResponse.class);
	}
	
	/**
	 * 일간 예약 목록 조회
	 * GET /api/v1/reservations/daily?date={date}
	 */
	public Mono<DailyReservationResponse> getDailyReservations(String date) {
		return webClient.get()
				.uri(uriBuilder -> uriBuilder
						.path(PREFIX + "/daily")
						.queryParam("date", date)
						.build())
				.retrieve()
				.bodyToMono(DailyReservationResponse.class);
	}
	
	/**
	 * 주간 예약 목록 조회
	 * GET /api/v1/reservations/weekly?startDate={startDate}
	 */
	public Mono<DailyReservationResponse> getWeeklyReservations(String startDate) {
		return webClient.get()
				.uri(uriBuilder -> uriBuilder
						.path(PREFIX + "/weekly")
						.queryParam("startDate", startDate)
						.build())
				.retrieve()
				.bodyToMono(DailyReservationResponse.class);
	}
	
	/**
	 * 월간 예약 목록 조회
	 * GET /api/v1/reservations/monthly?yearMonth={yearMonth}
	 */
	public Mono<DailyReservationResponse> getMonthlyReservations(String yearMonth) {
		return webClient.get()
				.uri(uriBuilder -> uriBuilder
						.path(PREFIX + "/monthly")
						.queryParam("yearMonth", yearMonth)
						.build())
				.retrieve()
				.bodyToMono(DailyReservationResponse.class);
	}
	
	/**
	 * 사용자별 예약 목록 조회 (커서 페이징)
	 * GET /api/v1/reservations/users/{userId}?cursor={cursor}&size={size}&statuses={statuses}
	 *
	 * @return 내부 응답 DTO (placeId, roomId만 포함) - API Gateway에서 enrichment 필요
	 */
	public Mono<InternalUserReservationsResponse> getUserReservations(
			Long userId,
			String cursor,
			Integer size,
			Set<ReservationStatus> statuses
	) {
		return webClient.get()
				.uri(uriBuilder -> {
					uriBuilder.path(PREFIX + "/users/{userId}");

					if (cursor != null) {
						uriBuilder.queryParam("cursor", cursor);
					}

					if (size != null) {
						uriBuilder.queryParam("size", size);
					}

					if (statuses != null && !statuses.isEmpty()) {
						String statusesStr = String.join(",", statuses.stream().map(Enum::name).toList());
						uriBuilder.queryParam("statuses", statusesStr);
					}
					
					return uriBuilder.build(userId);
				})
				.retrieve()
				.bodyToMono(InternalUserReservationsResponse.class);
	}
	
	/**
	 * 예약 사용자 정보 업데이트
	 * POST /api/v1/reservations/{id}/user-info
	 */
	public Mono<UserInfoUpdateResponse> updateUserInfo(Long reservationId, UserInfoUpdateRequest request) {
		return webClient.post()
				.uri(PREFIX + "/{id}/user-info", reservationId)
				.bodyValue(request)
				.retrieve()
				.bodyToMono(UserInfoUpdateResponse.class);
	}
	
	/**
	 * 결제 취소 (승인 전)
	 * PENDING_CONFIRMED 상태의 예약에 대해 결제 취소 요청
	 * POST /api/v1/reservations/{id}/cancel
	 *
	 * @param reservationId 예약 ID
	 */
	public Mono<Void> cancelPayment(Long reservationId) {
		return webClient.post()
				.uri(PREFIX + "/{id}/cancel", reservationId)
				.retrieve()
				.bodyToMono(Void.class);
	}
	
	/**
	 * 환불 요청 (승인 후)
	 * CONFIRMED 또는 REJECTED 상태의 예약에 대해 환불 요청
	 * POST /api/v1/reservations/{id}/refund
	 *
	 * @param reservationId 예약 ID
	 */
	public Mono<Void> refundReservation(Long reservationId) {
		return webClient.post()
				.uri(PREFIX + "/{id}/refund", reservationId)
				.retrieve()
				.bodyToMono(Void.class);
	}
}

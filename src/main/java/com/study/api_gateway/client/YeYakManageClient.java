package com.study.api_gateway.client;

import com.study.api_gateway.dto.reservationManage.enums.PeriodType;
import com.study.api_gateway.dto.reservationManage.enums.ReservationStatus;
import com.study.api_gateway.dto.reservationManage.request.ReservationCreateRequest;
import com.study.api_gateway.dto.reservationManage.request.UserInfoUpdateRequest;
import com.study.api_gateway.dto.reservationManage.response.DailyReservationResponse;
import com.study.api_gateway.dto.reservationManage.response.ReservationCreateResponse;
import com.study.api_gateway.dto.reservationManage.response.ReservationDetailResponse;
import com.study.api_gateway.dto.reservationManage.response.UserInfoUpdateResponse;
import com.study.api_gateway.dto.reservationManage.response.UserReservationsResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

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
	 * GET /api/v1/reservations/users/{userId}?period={period}&cursor={cursor}&size={size}&statuses={statuses}
	 */
	public Mono<UserReservationsResponse> getUserReservations(
			Long userId,
			PeriodType period,
			String cursor,
			Integer size,
			List<ReservationStatus> statuses
	) {
		return webClient.get()
				.uri(uriBuilder -> {
					uriBuilder.path(PREFIX + "/users/{userId}");
					uriBuilder.queryParam("period", period.name());

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
				.bodyToMono(UserReservationsResponse.class);
	}

	/**
	 * 예약 사용자 정보 업데이트 (예약 생성 2단계)
	 * POST /api/v1/reservations/{id}/user-info
	 */
	public Mono<UserInfoUpdateResponse> updateUserInfo(Long reservationId, UserInfoUpdateRequest request) {
		return webClient.post()
				.uri(PREFIX + "/{id}/user-info", reservationId)
				.bodyValue(request)
				.retrieve()
				.bodyToMono(UserInfoUpdateResponse.class);
	}
}
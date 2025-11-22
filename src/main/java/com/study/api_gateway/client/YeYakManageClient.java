package com.study.api_gateway.client;

import com.study.api_gateway.dto.reservationManage.enums.PeriodType;
import com.study.api_gateway.dto.reservationManage.enums.ReservationStatus;
import com.study.api_gateway.dto.reservationManage.request.ReservationCreateRequest;
import com.study.api_gateway.dto.reservationManage.response.DailyReservationResponse;
import com.study.api_gateway.dto.reservationManage.response.ReservationCreateResponse;
import com.study.api_gateway.dto.reservationManage.response.ReservationDetailResponse;
import com.study.api_gateway.dto.reservationManage.response.UserReservationsResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
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
				.uri(PREFIX)
				.bodyValue(request)
				.retrieve()
				.bodyToMono(ReservationCreateResponse.class);
	}
	
	/**
	 * 예약 상세 조회
	 * GET /api/v1/reservations/{id}
	 */
	public Mono<ReservationDetailResponse> getReservationById(Long id) {
		String uriString = PREFIX + "/" + id;
		
		return webClient.get()
				.uri(uriString)
				.retrieve()
				.bodyToMono(ReservationDetailResponse.class);
	}
	
	/**
	 * 일간 예약 목록 조회
	 * GET /api/v1/reservations/daily?date={date}
	 */
	public Mono<DailyReservationResponse> getDailyReservations(String date) {
		String uriString = UriComponentsBuilder.fromPath(PREFIX + "/daily")
				.queryParam("date", date)
				.toUriString();
		
		return webClient.get()
				.uri(uriString)
				.retrieve()
				.bodyToMono(DailyReservationResponse.class);
	}
	
	/**
	 * 주간 예약 목록 조회
	 * GET /api/v1/reservations/weekly?startDate={startDate}
	 */
	public Mono<DailyReservationResponse> getWeeklyReservations(String startDate) {
		String uriString = UriComponentsBuilder.fromPath(PREFIX + "/weekly")
				.queryParam("startDate", startDate)
				.toUriString();
		
		return webClient.get()
				.uri(uriString)
				.retrieve()
				.bodyToMono(DailyReservationResponse.class);
	}
	
	/**
	 * 월간 예약 목록 조회
	 * GET /api/v1/reservations/monthly?yearMonth={yearMonth}
	 */
	public Mono<DailyReservationResponse> getMonthlyReservations(String yearMonth) {
		String uriString = UriComponentsBuilder.fromPath(PREFIX + "/monthly")
				.queryParam("yearMonth", yearMonth)
				.toUriString();
		
		return webClient.get()
				.uri(uriString)
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
		UriComponentsBuilder builder = UriComponentsBuilder.fromPath(PREFIX + "/users/" + userId);
		
		builder.queryParam("period", period.name());
		
		if (cursor != null) {
			builder.queryParam("cursor", cursor);
		}
		
		if (size != null) {
			builder.queryParam("size", size);
		}
		
		if (statuses != null && !statuses.isEmpty()) {
			String statusesStr = String.join(",", statuses.stream().map(Enum::name).toList());
			builder.queryParam("statuses", statusesStr);
		}
		
		String uriString = builder.toUriString();
		
		return webClient.get()
				.uri(uriString)
				.retrieve()
				.bodyToMono(UserReservationsResponse.class);
	}
}

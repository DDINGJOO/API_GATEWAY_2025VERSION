package com.study.api_gateway.api.reservationManage.service;

import com.study.api_gateway.api.reservationManage.client.YeYakManageClient;
import com.study.api_gateway.api.reservationManage.dto.enums.ReservationStatus;
import com.study.api_gateway.api.reservationManage.dto.request.ReservationCreateRequest;
import com.study.api_gateway.api.reservationManage.dto.request.UserInfoUpdateRequest;
import com.study.api_gateway.api.reservationManage.dto.response.*;
import com.study.api_gateway.common.resilience.ResilienceOperator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Set;

/**
 * ReservationManage(YeYakManage) 도메인 Facade Service
 * Controller와 Client 사이의 중간 계층으로 Resilience 패턴 적용
 */
@Service
@RequiredArgsConstructor
public class ReservationManageFacadeService {

	private final YeYakManageClient yeYakManageClient;
	private final ResilienceOperator resilience;

	private static final String SERVICE_NAME = "reservation-manage-service";

	public Mono<ReservationCreateResponse> createReservation(ReservationCreateRequest request) {
		return yeYakManageClient.createReservation(request)
				.transform(resilience.protect(SERVICE_NAME));
	}

	public Mono<InternalReservationDetailResponse> getReservationById(Long id) {
		return yeYakManageClient.getReservationById(id)
				.transform(resilience.protect(SERVICE_NAME));
	}

	public Mono<DailyReservationResponse> getDailyReservations(String date) {
		return yeYakManageClient.getDailyReservations(date)
				.transform(resilience.protect(SERVICE_NAME));
	}

	public Mono<DailyReservationResponse> getWeeklyReservations(String startDate) {
		return yeYakManageClient.getWeeklyReservations(startDate)
				.transform(resilience.protect(SERVICE_NAME));
	}

	public Mono<DailyReservationResponse> getMonthlyReservations(String yearMonth) {
		return yeYakManageClient.getMonthlyReservations(yearMonth)
				.transform(resilience.protect(SERVICE_NAME));
	}

	public Mono<InternalUserReservationsResponse> getUserReservations(
			Long userId,
			String cursor,
			Integer size,
			Set<ReservationStatus> statuses
	) {
		return yeYakManageClient.getUserReservations(userId, cursor, size, statuses)
				.transform(resilience.protect(SERVICE_NAME));
	}

	public Mono<UserInfoUpdateResponse> updateUserInfo(Long reservationId, UserInfoUpdateRequest request) {
		return yeYakManageClient.updateUserInfo(reservationId, request)
				.transform(resilience.protect(SERVICE_NAME));
	}

	public Mono<Void> cancelPayment(Long reservationId) {
		return yeYakManageClient.cancelPayment(reservationId)
				.transform(resilience.protect(SERVICE_NAME));
	}

	public Mono<Void> refundReservation(Long reservationId) {
		return yeYakManageClient.refundReservation(reservationId)
				.transform(resilience.protect(SERVICE_NAME));
	}
}

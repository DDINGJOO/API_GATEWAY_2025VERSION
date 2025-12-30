package com.study.api_gateway.service;

import com.study.api_gateway.dto.place.response.PlaceInfoResponse;
import com.study.api_gateway.dto.reservationManage.response.InternalReservationDetailResponse;
import com.study.api_gateway.dto.reservationManage.response.InternalUserReservationsResponse;
import com.study.api_gateway.dto.reservationManage.response.ReservationDetailResponse;
import com.study.api_gateway.dto.reservationManage.response.UserReservationsResponse;
import com.study.api_gateway.dto.room.response.RoomDetailResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 예약 정보 Enrichment 서비스
 * YeYakManage 서버 응답에 Place/Room 상세 정보를 주입
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationEnrichmentService {
	
	private final PlaceCacheService placeCacheService;
	private final RoomCacheService roomCacheService;
	
	/**
	 * 예약 목록에 Place/Room 정보 주입
	 *
	 * @param internalResponse YeYakManage 서버 응답 (placeId, roomId만 포함)
	 * @return Enriched 응답 (placeInfo, roomInfo 포함)
	 */
	public Mono<UserReservationsResponse> enrichUserReservations(InternalUserReservationsResponse internalResponse) {
		if (internalResponse == null || internalResponse.getContent() == null || internalResponse.getContent().isEmpty()) {
			return Mono.just(UserReservationsResponse.builder()
					.content(List.of())
					.cursor(internalResponse != null && internalResponse.getCursor() != null
							? UserReservationsResponse.CursorInfo.builder()
							.next(internalResponse.getCursor().getNext())
							.hasNext(internalResponse.getCursor().getHasNext())
							.build()
							: null)
					.size(internalResponse != null ? internalResponse.getSize() : 0)
					.build());
		}
		
		List<InternalUserReservationsResponse.InternalUserReservationItem> items = internalResponse.getContent();
		
		// 1. 유니크한 placeId, roomId 수집
		List<Long> uniquePlaceIds = items.stream()
				.map(InternalUserReservationsResponse.InternalUserReservationItem::getPlaceId)
				.filter(Objects::nonNull)
				.distinct()
				.collect(Collectors.toList());
		
		List<Long> uniqueRoomIds = items.stream()
				.map(InternalUserReservationsResponse.InternalUserReservationItem::getRoomId)
				.filter(Objects::nonNull)
				.distinct()
				.collect(Collectors.toList());
		
		log.info("Enriching {} reservations with {} unique places and {} unique rooms",
				items.size(), uniquePlaceIds.size(), uniqueRoomIds.size());
		
		// 2. Place/Room 정보 병렬 배치 조회 (캐시 활용)
		Mono<Map<Long, PlaceInfoResponse>> placesMono = placeCacheService.getPlacesByBatchWithCache(uniquePlaceIds)
				.map(response -> {
					if (response.getResults() == null) {
						return Map.<Long, PlaceInfoResponse>of();
					}
					return response.getResults().stream()
							.collect(Collectors.toMap(
									place -> Long.parseLong(place.getId()),
									place -> place,
									(existing, replacement) -> existing
							));
				})
				.doOnNext(map -> log.info("Fetched {} places", map.size()));
		
		Mono<Map<Long, RoomDetailResponse>> roomsMono = roomCacheService.getRoomsByBatchWithCache(uniqueRoomIds)
				.doOnNext(map -> log.info("Fetched {} rooms", map.size()));
		
		// 3. 데이터 조합하여 최종 응답 생성
		return Mono.zip(placesMono, roomsMono)
				.map(tuple -> {
					Map<Long, PlaceInfoResponse> placeMap = tuple.getT1();
					Map<Long, RoomDetailResponse> roomMap = tuple.getT2();
					
					List<UserReservationsResponse.UserReservationItem> enrichedItems = items.stream()
							.map(item -> enrichItem(item, placeMap, roomMap))
							.collect(Collectors.toList());
					
					return UserReservationsResponse.builder()
							.content(enrichedItems)
							.cursor(internalResponse.getCursor() != null
									? UserReservationsResponse.CursorInfo.builder()
									.next(internalResponse.getCursor().getNext())
									.hasNext(internalResponse.getCursor().getHasNext())
									.build()
									: null)
							.size(internalResponse.getSize())
							.build();
				});
	}
	
	/**
	 * 개별 예약 항목에 Place/Room 정보 주입
	 */
	private UserReservationsResponse.UserReservationItem enrichItem(
			InternalUserReservationsResponse.InternalUserReservationItem item,
			Map<Long, PlaceInfoResponse> placeMap,
			Map<Long, RoomDetailResponse> roomMap
	) {
		// Place 정보 매핑
		UserReservationsResponse.PlaceInfo placeInfo = null;
		if (item.getPlaceId() != null) {
			PlaceInfoResponse place = placeMap.get(item.getPlaceId());
			if (place != null) {
				placeInfo = UserReservationsResponse.PlaceInfo.builder()
						.placeId(item.getPlaceId().intValue())
						.placeName(place.getPlaceName())
						.build();
			} else {
				// Place 정보가 없어도 placeId는 포함
				placeInfo = UserReservationsResponse.PlaceInfo.builder()
						.placeId(item.getPlaceId().intValue())
						.placeName(null)
						.build();
			}
		}
		
		// Room 정보 매핑
		UserReservationsResponse.RoomInfo roomInfo = null;
		if (item.getRoomId() != null) {
			RoomDetailResponse room = roomMap.get(item.getRoomId());
			if (room != null) {
				roomInfo = UserReservationsResponse.RoomInfo.builder()
						.roomId(item.getRoomId().intValue())
						.roomName(room.getRoomName())
						.imageUrls(room.getImageUrls())
						.timeSlot(room.getTimeSlot() != null ? room.getTimeSlot().name() : null)
						.build();
			} else {
				// Room 정보가 없어도 roomId는 포함
				roomInfo = UserReservationsResponse.RoomInfo.builder()
						.roomId(item.getRoomId().intValue())
						.roomName(null)
						.imageUrls(List.of())
						.timeSlot(null)
						.build();
			}
		}
		
		return UserReservationsResponse.UserReservationItem.builder()
				.reservationId(item.getReservationId())
				.placeInfo(placeInfo)
				.roomInfo(roomInfo)
				.reservationDate(item.getReservationDate())
				.startTimes(item.getStartTimes())
				.status(item.getStatus())
				.totalPrice(item.getTotalPrice())
				.reserverName(item.getReserverName())
				.reserverPhone(item.getReserverPhone())
				.build();
	}
	
	/**
	 * 예약 상세에 Place/Room 정보 주입
	 *
	 * @param internalResponse YeYakManage 서버 응답 (placeId, roomId만 포함)
	 * @return Enriched 응답 (placeInfo, roomInfo 포함)
	 */
	public Mono<ReservationDetailResponse> enrichReservationDetail(InternalReservationDetailResponse internalResponse) {
		if (internalResponse == null) {
			return Mono.empty();
		}
		
		Long placeId = internalResponse.getPlaceId();
		Long roomId = internalResponse.getRoomId();
		
		log.info("Enriching reservation detail: reservationId={}, placeId={}, roomId={}",
				internalResponse.getReservationId(), placeId, roomId);
		
		// Place/Room 정보 병렬 조회 (캐시 활용)
		Mono<PlaceInfoResponse> placeMono = placeId != null
				? placeCacheService.getPlacesByBatchWithCache(List.of(placeId))
				.map(response -> response.getResults() != null && !response.getResults().isEmpty()
						? response.getResults().get(0)
						: null)
				: Mono.just((PlaceInfoResponse) null);
		
		Mono<RoomDetailResponse> roomMono = roomId != null
				? roomCacheService.getRoomsByBatchWithCache(List.of(roomId))
				.map(map -> map.get(roomId))
				: Mono.just((RoomDetailResponse) null);
		
		return Mono.zip(placeMono, roomMono)
				.map(tuple -> {
					PlaceInfoResponse place = tuple.getT1();
					RoomDetailResponse room = tuple.getT2();
					
					return buildDetailResponse(internalResponse, place, room);
				})
				.defaultIfEmpty(buildDetailResponse(internalResponse, null, null));
	}
	
	/**
	 * 상세 응답 빌드
	 */
	private ReservationDetailResponse buildDetailResponse(
			InternalReservationDetailResponse internal,
			PlaceInfoResponse place,
			RoomDetailResponse room
	) {
		// Place 정보 매핑
		ReservationDetailResponse.PlaceInfo placeInfo = null;
		if (internal.getPlaceId() != null) {
			ReservationDetailResponse.PlaceInfo.PlaceInfoBuilder builder = ReservationDetailResponse.PlaceInfo.builder()
					.placeId(internal.getPlaceId());
			
			if (place != null) {
				builder.placeName(place.getPlaceName());
				if (place.getLocation() != null) {
					if (place.getLocation().getAddress() != null) {
						builder.fullAddress(place.getLocation().getAddress().getFullAddress());
					}
					builder.latitude(place.getLocation().getLatitude());
					builder.longitude(place.getLocation().getLongitude());
				}
			}
			placeInfo = builder.build();
		}
		
		// Room 정보 매핑
		ReservationDetailResponse.RoomInfo roomInfo = null;
		if (internal.getRoomId() != null) {
			ReservationDetailResponse.RoomInfo.RoomInfoBuilder builder = ReservationDetailResponse.RoomInfo.builder()
					.roomId(internal.getRoomId());
			
			if (room != null) {
				builder.roomName(room.getRoomName())
						.imageUrls(room.getImageUrls())
						.timeSlot(room.getTimeSlot() != null ? room.getTimeSlot().name() : null);
			} else {
				builder.imageUrls(List.of());
			}
			roomInfo = builder.build();
		}
		
		// selectedProducts 변환
		List<ReservationDetailResponse.SelectedProduct> selectedProducts = null;
		if (internal.getSelectedProducts() != null) {
			selectedProducts = internal.getSelectedProducts().stream()
					.map(p -> ReservationDetailResponse.SelectedProduct.builder()
							.productId(p.getProductId())
							.productName(p.getProductName())
							.quantity(p.getQuantity())
							.unitPrice(p.getUnitPrice())
							.subtotal(p.getSubtotal())
							.build())
					.collect(Collectors.toList());
		}
		
		return ReservationDetailResponse.builder()
				.reservationId(internal.getReservationId())
				.userId(internal.getUserId())
				.placeInfo(placeInfo)
				.roomInfo(roomInfo)
				.status(internal.getStatus())
				.reservationDate(internal.getReservationDate())
				.startTimes(internal.getStartTimes())
				.totalPrice(internal.getTotalPrice())
				.reservationTimePrice(internal.getReservationTimePrice())
				.isBlackUser(internal.getIsBlackUser())
				.reserverName(internal.getReserverName())
				.reserverPhone(internal.getReserverPhone())
				.selectedProducts(selectedProducts)
				.additionalInfo(internal.getAdditionalInfo())
				.approvedAt(internal.getApprovedAt())
				.approvedBy(internal.getApprovedBy())
				.rejectedAt(internal.getRejectedAt())
				.rejectedReason(internal.getRejectedReason())
				.rejectedBy(internal.getRejectedBy())
				.createdAt(internal.getCreatedAt())
				.updatedAt(internal.getUpdatedAt())
				.build();
	}
}

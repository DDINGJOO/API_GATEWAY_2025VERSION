package com.study.api_gateway.dto.product.enums;

/**
 * 상품의 가격 책정 방식
 */
public enum PricingType {
	INITIAL_PLUS_ADDITIONAL,  // 초기 대여료 + 추가 요금
	ONE_TIME,                 // 1회 대여료 고정
	SIMPLE_STOCK              // 단순 재고 관리
}
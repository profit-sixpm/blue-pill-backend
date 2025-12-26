package com.sixpm.domain.ai.dto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public record CriteriaResponse(
    @JsonPropertyDescription("공고에서 제한하는 거주 지역명 (예: '광주광역시'). 지역 제한이 없으면 null.")
    String residenceRegion,

    @JsonPropertyDescription("신청 가능한 최소 연령 (숫자). 보통 '성년자'인 경우 19.")
    Integer minAge,

    @JsonPropertyDescription("무주택세대구성원 필수 여부. 영구임대는 대부분 true.")
    Boolean requiresHomeless,

    @JsonPropertyDescription("가구원수별 '월평균 소득 100% 기준 금액'. Key: 가구원수(1, 2...), Value: 금액(원).")
    Map<Integer, Long> incomeBenchmark,

    @JsonPropertyDescription("자녀 수에 따른 자산 및 자동차 한도 목록.")
    List<AssetLimitRule> assetLimits,

    @JsonPropertyDescription("공급 유형, 가구원수, 자녀수에 따른 소득 비율(%) 매트릭스.")
    List<IncomeRatioRule> incomeRatios
) {
    public static CriteriaResponse empty() {
        return new CriteriaResponse(
            null, 19, true, 
            Collections.emptyMap(), 
            Collections.emptyList(), 
            Collections.emptyList()
        );
    }

    // --- LLM 응답 매핑용 내부 DTO ---

    public record AssetLimitRule(
            @JsonPropertyDescription("자녀 수 조건 (0, 1, 2...). '2인 이상'인 경우 2로 기재.")
            int childCount,
            @JsonPropertyDescription("총자산 한도액 (원 단위)")
            long assetLimit,
            @JsonPropertyDescription("자동차 가액 한도 (원 단위)")
            long carLimit
    ) {}

    public record IncomeRatioRule(
            @JsonPropertyDescription("공급 유형 코드. 예: GENERAL(일반), DISABLED(장애인), NATIONAL_MERIT(국가유공자), MULTI_CHILD(다자녀) 등.")
            String supplyType,
            @JsonPropertyDescription("최소 가구원 수")
            int minHousehold,
            @JsonPropertyDescription("최대 가구원 수. 상한이 없으면 99.")
            int maxHousehold,
            @JsonPropertyDescription("필수 자녀 수. 자녀 수 조건이 없으면 -1.")
            int childCount,
            @JsonPropertyDescription("적용 소득 비율(%). 예: 50, 70, 100.")
            int ratio
    ) {}
}
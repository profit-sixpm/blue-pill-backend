package com.sixpm.domain.report.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 분석리포트 생성 요청
 */
@Data
@Schema(description = "분석리포트 생성 요청")
public class CreateReportRequest {

    @NotNull(message = "나이는 필수입니다")
    @Min(value = 19, message = "나이는 19세 이상이어야 합니다")
    @Schema(description = "나이", example = "30")
    private Integer age;

    @NotBlank(message = "거주지역은 필수입니다")
    @Schema(description = "거주지역", example = "서울특별시")
    private String residenceArea;

    @NotNull(message = "거주기간은 필수입니다")
    @Min(value = 0, message = "거주기간은 0 이상이어야 합니다")
    @Schema(description = "거주기간 (년)", example = "5")
    private Integer residencePeriod;

    @NotNull(message = "세대원 수는 필수입니다")
    @Min(value = 1, message = "세대원 수는 1 이상이어야 합니다")
    @Schema(description = "세대원 수", example = "3")
    private Integer householdMembers;

    @NotNull(message = "미성년 자녀수는 필수입니다")
    @Min(value = 0, message = "미성년 자녀수는 0 이상이어야 합니다")
    @Schema(description = "미성년 자녀수", example = "1")
    private Integer minorChildren;

    @NotNull(message = "월평균 소득은 필수입니다")
    @Min(value = 0, message = "월평균 소득은 0 이상이어야 합니다")
    @Schema(description = "월평균 소득 (만원)", example = "300")
    private Integer monthlyIncome;

    @NotNull(message = "총 자산 가액은 필수입니다")
    @Min(value = 0, message = "총 자산 가액은 0 이상이어야 합니다")
    @Schema(description = "총 자산 가액 (만원)", example = "10000")
    private Integer totalAssets;

    @NotNull(message = "자동차 가액은 필수입니다")
    @Min(value = 0, message = "자동차 가액은 0 이상이어야 합니다")
    @Schema(description = "자동차 가액 (만원)", example = "2000")
    private Integer carValue;

    @NotNull(message = "청약통장 보유 여부는 필수입니다")
    @Schema(description = "청약통장 보유 여부", example = "true")
    private Boolean hasSavingsAccount;

    @Min(value = 0, message = "납입 횟수는 0 이상이어야 합니다")
    @Schema(description = "청약통장 납입 횟수", example = "24")
    private Integer paymentCount;

    @Schema(description = "추가 자격 (쉼표로 구분)", example = "신혼부부,생애최초")
    private String additionalQualifications;

    @NotNull(message = "무주택 세대주 여부는 필수입니다")
    @Schema(description = "무주택 세대주 여부", example = "true")
    private Boolean isHomelessHouseholder;

    @NotNull(message = "한부모 가족 여부는 필수입니다")
    @Schema(description = "한부모 가족 여부", example = "false")
    private Boolean isSingleParent;

    @NotNull(message = "혼인 중 여부는 필수입니다")
    @Schema(description = "혼인 중 여부", example = "true")
    private Boolean isMarried;

    @NotNull(message = "장애인 여부는 필수입니다")
    @Schema(description = "장애인 여부", example = "false")
    private Boolean isDisabled;

    @NotNull(message = "중증 장애인 여부는 필수입니다")
    @Schema(description = "중증 장애인 여부", example = "false")
    private Boolean isSeverelyDisabled;

    @NotNull(message = "우선공급 대상 여부는 필수입니다")
    @Schema(description = "우선공급 대상 여부", example = "false")
    private Boolean isPrioritySupply;
}


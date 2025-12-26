package com.sixpm.domain.report.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 사용자 상세 정보 (분석리포트용)
 * 모든 정보를 하나의 테이블에 저장
 */
@Entity
@Table(name = "user_detail_info")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserDetailInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    // 기본 정보
    @Column(name = "age", nullable = false)
    private Integer age;

    @Column(name = "residence_area", nullable = false, length = 100)
    private String residenceArea;

    @Column(name = "residence_period", nullable = false)
    private Integer residencePeriod;

    // 가구 정보
    @Column(name = "household_members", nullable = false)
    private Integer householdMembers;

    @Column(name = "minor_children", nullable = false)
    private Integer minorChildren;

    @Column(name = "is_homeless_householder", nullable = false)
    private Boolean isHomelessHouseholder;

    @Column(name = "is_single_parent", nullable = false)
    private Boolean isSingleParent;

    @Column(name = "is_married", nullable = false)
    private Boolean isMarried;

    // 재산 정보
    @Column(name = "monthly_income", nullable = false)
    private Integer monthlyIncome;

    @Column(name = "total_assets", nullable = false)
    private Integer totalAssets;

    @Column(name = "car_value", nullable = false)
    private Integer carValue;

    // 청약통장 정보
    @Column(name = "has_savings_account", nullable = false)
    private Boolean hasSavingsAccount;

    @Column(name = "payment_count")
    private Integer paymentCount;

    // 자격 정보
    @Column(name = "additional_qualifications", columnDefinition = "TEXT")
    private String additionalQualifications;

    @Column(name = "is_disabled", nullable = false)
    private Boolean isDisabled;

    @Column(name = "is_severely_disabled", nullable = false)
    private Boolean isSeverelyDisabled;

    @Column(name = "is_priority_supply", nullable = false)
    private Boolean isPrioritySupply;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public UserDetailInfo(Long userId, Integer age, String residenceArea, Integer residencePeriod,
                         Integer householdMembers, Integer minorChildren, Boolean isHomelessHouseholder,
                         Boolean isSingleParent, Boolean isMarried, Integer monthlyIncome,
                         Integer totalAssets, Integer carValue, Boolean hasSavingsAccount,
                         Integer paymentCount, String additionalQualifications, Boolean isDisabled,
                         Boolean isSeverelyDisabled, Boolean isPrioritySupply) {
        this.userId = userId;
        this.age = age;
        this.residenceArea = residenceArea;
        this.residencePeriod = residencePeriod;
        this.householdMembers = householdMembers;
        this.minorChildren = minorChildren;
        this.isHomelessHouseholder = isHomelessHouseholder;
        this.isSingleParent = isSingleParent;
        this.isMarried = isMarried;
        this.monthlyIncome = monthlyIncome;
        this.totalAssets = totalAssets;
        this.carValue = carValue;
        this.hasSavingsAccount = hasSavingsAccount;
        this.paymentCount = paymentCount;
        this.additionalQualifications = additionalQualifications;
        this.isDisabled = isDisabled;
        this.isSeverelyDisabled = isSeverelyDisabled;
        this.isPrioritySupply = isPrioritySupply;
    }

    public void update(Integer age, String residenceArea, Integer residencePeriod,
                       Integer householdMembers, Integer minorChildren, Boolean isHomelessHouseholder,
                       Boolean isSingleParent, Boolean isMarried, Integer monthlyIncome,
                       Integer totalAssets, Integer carValue, Boolean hasSavingsAccount,
                       Integer paymentCount, String additionalQualifications, Boolean isDisabled,
                       Boolean isSeverelyDisabled, Boolean isPrioritySupply) {
        this.age = age;
        this.residenceArea = residenceArea;
        this.residencePeriod = residencePeriod;
        this.householdMembers = householdMembers;
        this.minorChildren = minorChildren;
        this.isHomelessHouseholder = isHomelessHouseholder;
        this.isSingleParent = isSingleParent;
        this.isMarried = isMarried;
        this.monthlyIncome = monthlyIncome;
        this.totalAssets = totalAssets;
        this.carValue = carValue;
        this.hasSavingsAccount = hasSavingsAccount;
        this.paymentCount = paymentCount;
        this.additionalQualifications = additionalQualifications;
        this.isDisabled = isDisabled;
        this.isSeverelyDisabled = isSeverelyDisabled;
        this.isPrioritySupply = isPrioritySupply;
    }
}


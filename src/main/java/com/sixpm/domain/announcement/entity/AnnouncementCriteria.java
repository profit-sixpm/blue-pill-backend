package com.sixpm.domain.announcement.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "announcement_criteria")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AnnouncementCriteria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "announcement_id", nullable = false)
    private Announcement announcement;

    // 1. 기본 자격 요건
    @Column(name = "residence_region", length = 100)
    private String residenceRegion;

    @Column(name = "min_age")
    private Integer minAge;

    @Column(name = "requires_homeless")
    private Boolean requiresHomeless;

    // 2. 기준 소득표 (Key: 가구원수, Value: 금액)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "income_benchmark", columnDefinition = "jsonb")
    private Map<Integer, Long> incomeBenchmark;

    // 3. 자산 기준 매트릭스
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "asset_limits", columnDefinition = "jsonb")
    private List<AssetLimitRule> assetLimits;

    // 4. 소득 비율 매트릭스
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "income_ratios", columnDefinition = "jsonb")
    private List<IncomeRatioRule> incomeRatios;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public AnnouncementCriteria(Announcement announcement, String residenceRegion, Integer minAge, Boolean requiresHomeless,
                                Map<Integer, Long> incomeBenchmark, List<AssetLimitRule> assetLimits, List<IncomeRatioRule> incomeRatios) {
        this.announcement = announcement;
        this.residenceRegion = residenceRegion;
        this.minAge = minAge != null ? minAge : 19;
        this.requiresHomeless = requiresHomeless != null ? requiresHomeless : true;
        this.incomeBenchmark = incomeBenchmark;
        this.assetLimits = assetLimits;
        this.incomeRatios = incomeRatios;
    }

    public void updateCriteria(String residenceRegion, Integer minAge, Boolean requiresHomeless,
                               Map<Integer, Long> incomeBenchmark, List<AssetLimitRule> assetLimits, List<IncomeRatioRule> incomeRatios) {
        this.residenceRegion = residenceRegion;
        this.minAge = minAge;
        this.requiresHomeless = requiresHomeless;
        this.incomeBenchmark = incomeBenchmark;
        this.assetLimits = assetLimits;
        this.incomeRatios = incomeRatios;
    }

    // --- JSON 매핑용 내부 Record (Entity 전용) ---

    public record AssetLimitRule(
            int childCount,
            long assetLimit,
            long carLimit
    ) {}

    public record IncomeRatioRule(
            String supplyType,
            int minHousehold,
            int maxHousehold,
            int childCount,
            int ratio
    ) {}
}

package com.sixpm.domain.report.service;

import com.sixpm.domain.report.dto.request.CreateReportRequest;
import com.sixpm.domain.report.entity.UserDetailInfo;
import com.sixpm.domain.report.repository.UserDetailInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 분석리포트 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final UserDetailInfoRepository userDetailInfoRepository;

    /**
     * 분석리포트 생성
     */
    @Transactional
    public void createReport(CreateReportRequest request) {
        Long userId = getCurrentUserId();

        log.info("Creating analysis report for user: {}", userId);

        // 기존 데이터가 있으면 삭제 (업데이트 대신)
        userDetailInfoRepository.findByUserId(userId)
                .ifPresent(userDetailInfoRepository::delete);

        // 사용자 상세 정보 저장 (모든 정보를 하나의 테이블에)
        UserDetailInfo userDetailInfo = UserDetailInfo.builder()
                .userId(userId)
                // 기본 정보
                .age(request.getAge())
                .residenceArea(request.getResidenceArea())
                .residencePeriod(request.getResidencePeriod())
                // 가구 정보
                .householdMembers(request.getHouseholdMembers())
                .minorChildren(request.getMinorChildren())
                .isHomelessHouseholder(request.getIsHomelessHouseholder())
                .isSingleParent(request.getIsSingleParent())
                .isMarried(request.getIsMarried())
                // 재산 정보
                .monthlyIncome(request.getMonthlyIncome())
                .totalAssets(request.getTotalAssets())
                .carValue(request.getCarValue())
                // 청약통장 정보
                .hasSavingsAccount(request.getHasSavingsAccount())
                .paymentCount(request.getPaymentCount())
                // 자격 정보
                .additionalQualifications(request.getAdditionalQualifications())
                .isDisabled(request.getIsDisabled())
                .isSeverelyDisabled(request.getIsSeverelyDisabled())
                .isPrioritySupply(request.getIsPrioritySupply())
                .build();

        userDetailInfoRepository.save(userDetailInfo);

        log.info("Analysis report created successfully for user: {}", userId);
    }

    /**
     * 현재 로그인한 사용자 ID 조회
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("인증되지 않은 사용자입니다");
        }

        // UserDetails에서 username을 가져와서 userId로 변환
        // TODO: JWT에서 userId를 직접 가져오도록 개선 필요
        String username = authentication.getName();

        // 임시로 1L 반환 (나중에 JWT에서 userId 추출하도록 수정)
        return 1L;
    }
}


package com.sixpm.domain.report.service;

import com.sixpm.domain.announcement.dto.response.AnnouncementListResponse;
import com.sixpm.domain.announcement.dto.response.AnnouncementListResponse.AnnouncementItem;
import com.sixpm.domain.announcement.entity.Announcement;
import com.sixpm.domain.announcement.entity.AnnouncementCriteria;
import com.sixpm.domain.announcement.repository.AnnouncementCriteriaRepository;
import com.sixpm.domain.announcement.repository.AnnouncementRepository;
import com.sixpm.domain.report.dto.request.CreateReportRequest;
import com.sixpm.domain.report.dto.request.SearchQueryRequest;
import com.sixpm.domain.report.dto.response.AiConsulting;
import com.sixpm.domain.report.dto.response.DetailItem;
import com.sixpm.domain.report.dto.response.ReportResponse;
import com.sixpm.domain.report.entity.UserDetailInfo;
import com.sixpm.domain.report.repository.UserDetailInfoRepository;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.AdvisorParams;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 분석리포트 서비스
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class ReportService {

    private final UserDetailInfoRepository userDetailInfoRepository;
    private final VectorStore vectorStore;
    private final AnnouncementRepository announcementRepository;
    private final AnnouncementCriteriaRepository criteriaRepository;
    private final RewriteQueryTransformer queryTransformer;
    private final ChatClient chatClient;
    private final RetrievalAugmentationAdvisor ragAdvisor;

    public ReportService(
            UserDetailInfoRepository userDetailInfoRepository,
            VectorStore vectorStore,
            AnnouncementRepository announcementRepository,
            AnnouncementCriteriaRepository criteriaRepository,
            ChatClient.Builder chatClientBuilder
    ) {
        this.userDetailInfoRepository = userDetailInfoRepository;
        this.vectorStore = vectorStore;
        this.announcementRepository = announcementRepository;
        this.criteriaRepository = criteriaRepository;

        this.queryTransformer = RewriteQueryTransformer.builder()
                .chatClientBuilder(chatClientBuilder)
                .build();
        this.chatClient = chatClientBuilder.build();

        // RAG Advisor 초기화
        var documentRetriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
                .similarityThreshold(0.5)
                .topK(10)
                .build();

        var queryAugmenter = ContextualQueryAugmenter.builder()
                .allowEmptyContext(true)
                .build();

        this.ragAdvisor = RetrievalAugmentationAdvisor.builder()
                .documentRetriever(documentRetriever)
                .queryAugmenter(queryAugmenter)
                .build();
    }

    /**
     * 공고 자연어 검색 (RAG + Query Optimization)
     */
    public List<AnnouncementItem> searchAnnouncements(String query) {
        log.info("Original query: {}", query);

        // 1. 쿼리 최적화 (Rewrite)
        Query originalQuery = new Query(query);
        Query optimizedQuery = queryTransformer.transform(originalQuery);

        log.info("Optimized query: {}", optimizedQuery.text());

        // 2. Vector Store 검색 (유사도 0.5 이상, 상위 20개)
        List<Document> documents = vectorStore.similaritySearch(
                SearchRequest.builder().query(optimizedQuery.text())
                        .topK(5)
                        .similarityThreshold(0.5)
                        .build()
        );

        if (documents.isEmpty()) {
            return Collections.emptyList();
        }

        // 3. 공고 ID 추출 (중복 제거)
        Set<Long> announcementIds = documents.stream()
                .map(doc -> {
                    Object idObj = doc.getMetadata().get("notice_id");
                    return idObj != null ? Long.parseLong(idObj.toString()) : null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (announcementIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 4. DB에서 공고 정보 조회
        List<Announcement> announcements = announcementRepository.findAllById(announcementIds);

        // 5. DTO 변환 및 반환
        return announcements.stream()
                .map(this::convertToAnnouncementItem)
                .collect(Collectors.toList());
    }

    /**
     * 분석리포트 생성 및 반환
     */
    @Transactional
    public ReportResponse createReport(CreateReportRequest request) {
        Long userId = getCurrentUserId();
        log.info("Generating report for user: {}, announcement: {}", userId, request.getAnnouncementId());

        // 1. 데이터 로드
        Announcement announcement = announcementRepository.findById(request.getAnnouncementId())
                .orElseThrow(() -> new IllegalArgumentException("공고를 찾을 수 없습니다."));
        AnnouncementCriteria criteria = criteriaRepository.findByAnnouncementId(announcement.getId())
                .orElseThrow(() -> new IllegalArgumentException("해당 공고의 상세 자격 요건 정보가 없습니다."));

        // 2. 정량 분석 (Rule Engine)
        List<DetailItem> details = analyzeEligibility(request, criteria);

        // 3. 최종 상태 및 점수 결정
        boolean isAllPassed = details.stream().filter(d -> !d.category().equals("가점")).allMatch(DetailItem::passed);
        String status = isAllPassed ? "PASS" : "FAIL";
        int totalScore = calculateTotalScore(request, criteria);

        // 4. 정성 분석 (AI 컨설팅)
        AiConsulting consulting = generateAiConsulting(request, announcement, status, details);

        // 사용자 상세 정보 저장 (이력 관리용)
        saveUserDetailInfo(userId, request);

        return new ReportResponse(status, totalScore, details, consulting);
    }

    private void saveUserDetailInfo(Long userId, CreateReportRequest request) {
        // 기존 데이터가 있으면 삭제 (최신 정보 유지)
        userDetailInfoRepository.findByUserId(userId)
                .ifPresent(userDetailInfoRepository::delete);

        UserDetailInfo userDetailInfo = UserDetailInfo.builder()
                .userId(userId)
                .age(request.getAge())
                .residenceArea(request.getResidenceArea())
                .residencePeriod(request.getResidencePeriod())
                .householdMembers(request.getHouseholdMembers())
                .minorChildren(request.getMinorChildren())
                .isHomelessHouseholder(request.getIsHomelessHouseholder())
                .isSingleParent(request.getIsSingleParent())
                .isMarried(request.getIsMarried())
                .monthlyIncome(request.getMonthlyIncome())
                .totalAssets(request.getTotalAssets())
                .carValue(request.getCarValue())
                .hasSavingsAccount(request.getHasSavingsAccount())
                .paymentCount(request.getPaymentCount())
                .additionalQualifications(request.getAdditionalQualifications())
                .isDisabled(request.getIsDisabled())
                .isSeverelyDisabled(request.getIsSeverelyDisabled())
                .isPrioritySupply(request.getIsPrioritySupply())
                .build();

        userDetailInfoRepository.save(userDetailInfo);
    }

    private List<DetailItem> analyzeEligibility(CreateReportRequest user, AnnouncementCriteria criteria) {
        List<DetailItem> details = new ArrayList<>();

        // 거주지 분석
        String criteriaRegion = criteria.getResidenceRegion();
        boolean regionPassed = criteriaRegion == null || user.getResidenceArea().contains(criteriaRegion);
        details.add(new DetailItem("거주지", regionPassed, user.getResidenceArea(), 
                criteriaRegion != null ? criteriaRegion : "제한 없음",
                regionPassed ? "거주지 요건을 충족합니다." : "공고 지역 거주자가 아닙니다."));

        // 나이 분석
        boolean agePassed = user.getAge() >= criteria.getMinAge();
        details.add(new DetailItem("연령", agePassed, "만 " + user.getAge() + "세", "만 " + criteria.getMinAge() + "세 이상",
                agePassed ? "신청 가능 연령입니다." : "신청 최소 연령에 미달합니다."));

        // 자산 분석
        int childCount = Math.min(user.getMinorChildren(), 2);
        AnnouncementCriteria.AssetLimitRule assetRule = criteria.getAssetLimits().stream()
                .filter(r -> r.childCount() == childCount)
                .findFirst()
                .orElse(new AnnouncementCriteria.AssetLimitRule(0, 255000000L, 37080000L)); // Fallback

        boolean assetPassed = user.getTotalAssets() <= assetRule.assetLimit();
        details.add(new DetailItem("총자산", assetPassed, String.format("%,d원", user.getTotalAssets()),
                String.format("%,d원 이하", assetRule.assetLimit()),
                assetPassed ? "자산 보유 기준 이내입니다." : "총자산 기준을 초과하였습니다."));

        // 소득 분석
        Long benchmark = criteria.getIncomeBenchmark().getOrDefault(Math.min(user.getHouseholdMembers(), 8), 7000000L);
        long incomeLimit = (long) (benchmark * 0.7); // 일반 70% 가정
        boolean incomePassed = user.getMonthlyIncome() <= incomeLimit;
        details.add(new DetailItem("월소득", incomePassed, String.format("%,d원", user.getMonthlyIncome()),
                String.format("%,d원 이하", incomeLimit),
                incomePassed ? "소득 기준 이내입니다." : "월평균 소득 기준을 초과하였습니다."));

        return details;
    }

    private int calculateTotalScore(CreateReportRequest user, AnnouncementCriteria criteria) {
        int score = 0;
        if (user.getResidencePeriod() >= 15) score += 30;
        else if (user.getResidencePeriod() >= 10) score += 25;
        if (user.getHouseholdMembers() >= 4) score += 30;
        if (user.getPaymentCount() >= 120) score += 10;
        return score;
    }

    /**
     * LLM을 사용한 정성적 조언 생성 (RAG 적용)
     */
    private AiConsulting generateAiConsulting(CreateReportRequest user, Announcement announcement, String status, List<DetailItem> details) {
        String detailContext = details.stream()
                .map(d -> String.format("- %s: %s (%s)", d.category(), d.passed() ? "충족" : "미충족", d.message()))
                .collect(Collectors.joining("\n"));

        String prompt = status.equals("PASS")
                ? "사용자는 이 청약에 적합(PASS)합니다. 공고문의 내용을 바탕으로 당첨 가능성을 높이는 전략과 당첨 후 입주까지의 자금 계획, 서류 준비 등 로드맵을 조언해주세요."
                : "사용자는 이 청약에 부적합(FAIL)합니다. 미충족된 항목을 중심으로 탈락 사유를 설명하고, 공고문에서 언급된 다른 전형이나 예외 조항 등을 참고하여 해결책을 제시해주세요.";

        // RAG 필터: 해당 공고 ID에 해당하는 문서만 검색
        String filterExpression = "notice_id == " + announcement.getId();

        return chatClient.prompt()
                .advisors(ragAdvisor) // RAG 적용
                .advisors(a -> a.param(VectorStoreDocumentRetriever.FILTER_EXPRESSION, filterExpression))
                .advisors(AdvisorParams.ENABLE_NATIVE_STRUCTURED_OUTPUT) // JSON 출력 보장
                .user(u -> u.text("공고명: {title}\n판정 결과: {status}\n상세 내역:\n{details}\n\n{instruction}")
                        .param("title", announcement.getHouseNm())
                        .param("status", status)
                        .param("details", detailContext)
                        .param("instruction", prompt))
                .call()
                .entity(AiConsulting.class);
    }

    private AnnouncementListResponse.AnnouncementItem convertToAnnouncementItem(Announcement announcement) {
        return AnnouncementListResponse.AnnouncementItem.builder()
                .id(announcement.getId())
                .announcementName(announcement.getHouseNm())
                .announcementDate(announcement.getRcritPblancDe())
                .receptionStartDate(announcement.getRceptBgnde())
                .receptionEndDate(announcement.getRceptEndde())
                .receptionStatus(determineReceptionStatus(announcement))
                .regionCode(announcement.getSubscrptAreaCode())
                .regionName(announcement.getSubscrptAreaCodeNm())
                .pdfUrl(announcement.getPdfFileUrl())
                .createdAt(announcement.getCreatedAt())
                .build();
    }

    private String determineReceptionStatus(Announcement announcement) {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String startDate = announcement.getRceptBgnde();
        String endDate = announcement.getRceptEndde();

        if (startDate == null || endDate == null) return "공고중";
        if (today.compareTo(startDate) < 0) return "공고중";
        if (today.compareTo(startDate) >= 0 && today.compareTo(endDate) <= 0) return "접수중";
        return "접수마감";
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return 1L; // 임시 반환
        }
        // TODO: JWT에서 userId 추출
        return 1L;
    }
}
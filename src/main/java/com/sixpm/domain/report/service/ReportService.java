package com.sixpm.domain.report.service;

import com.sixpm.domain.announcement.dto.response.AnnouncementListResponse;
import com.sixpm.domain.announcement.dto.response.AnnouncementListResponse.AnnouncementItem;
import com.sixpm.domain.announcement.entity.Announcement;
import com.sixpm.domain.announcement.entity.AnnouncementCriteria;
import com.sixpm.domain.announcement.repository.AnnouncementCriteriaRepository;
import com.sixpm.domain.announcement.repository.AnnouncementRepository;
import com.sixpm.domain.report.dto.request.CreateReportRequest;
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
import org.springframework.ai.rag.preretrieval.query.expansion.MultiQueryExpander;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
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
    private final MultiQueryExpander queryExpander;
    private final ChatClient chatClient;
    private final RetrievalAugmentationAdvisor ragAdvisor;
    private final Resource systemPromptResource;

    public ReportService(
            UserDetailInfoRepository userDetailInfoRepository,
            VectorStore vectorStore,
            AnnouncementRepository announcementRepository,
            AnnouncementCriteriaRepository criteriaRepository,
            ChatClient.Builder chatClientBuilder,
            @Value("classpath:prompts/consulting-system-prompt.txt") Resource systemPromptResource
    ) {
        this.userDetailInfoRepository = userDetailInfoRepository;
        this.vectorStore = vectorStore;
        this.announcementRepository = announcementRepository;
        this.criteriaRepository = criteriaRepository;
        this.systemPromptResource = systemPromptResource;

        this.chatClient = chatClientBuilder.build();

        // 1. 쿼리 변환기 (검색어 최적화)
        this.queryTransformer = RewriteQueryTransformer.builder()
                .chatClientBuilder(chatClientBuilder)
                .build();

        // 2. 쿼리 확장기 (다각도 검색)
        this.queryExpander = MultiQueryExpander.builder()
                .chatClientBuilder(chatClientBuilder)
                .numberOfQueries(3)
                .build();

        // 3. RAG Advisor 구성
        var documentRetriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
                .similarityThreshold(0.5)
                .topK(5)
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
     * 공고 자연어 검색 (RAG + Query Optimization + Expansion)
     */
    public List<AnnouncementItem> searchAnnouncements(String query) {
        log.info("Original query: {}", query);

        // 1. 쿼리 최적화
        Query originalQuery = new Query(query);
        Query optimizedQuery = queryTransformer.transform(originalQuery);
        log.info("Optimized query: {}", optimizedQuery.text());

        // 2. 쿼리 확장
        List<Query> expandedQueries = queryExpander.expand(optimizedQuery);
        log.info("Expanded queries: {}", expandedQueries.stream().map(Query::text).toList());

        // 3. Vector Store 검색
        List<Document> documents = new ArrayList<>();
        for (Query q : expandedQueries) {
            documents.addAll(vectorStore.similaritySearch(
                    SearchRequest.builder().query(q.text())
                            .topK(5)
                            .similarityThreshold(0.5)
                            .build()
            ));
        }

        if (documents.isEmpty()) {
            return Collections.emptyList();
        }

        // 4. 공고 ID 추출 (중복 제거)
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

        // 5. DB 조회 및 반환
        List<Announcement> announcements = announcementRepository.findAllById(announcementIds);
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

        Announcement announcement = announcementRepository.findById(request.getAnnouncementId())
                .orElseThrow(() -> new IllegalArgumentException("공고를 찾을 수 없습니다."));
        AnnouncementCriteria criteria = criteriaRepository.findByAnnouncementId(announcement.getId())
                .orElseThrow(() -> new IllegalArgumentException("해당 공고의 상세 자격 요건 정보가 없습니다."));

        // 정량 분석
        List<DetailItem> details = analyzeEligibility(request, criteria);
        
        boolean isAllPassed = details.stream().filter(d -> !d.category().equals("가점")).allMatch(DetailItem::passed);
        String status = isAllPassed ? "PASS" : "FAIL";
        int totalScore = calculateTotalScore(request, criteria);

        // 정성 분석 (AI 컨설팅)
        AiConsulting consulting = generateAiConsulting(request, announcement, status, details);

        saveUserDetailInfo(userId, request);

        return new ReportResponse(status, totalScore, details, consulting);
    }

    /**
     * LLM을 사용한 정성적 조언 생성 (RAG 적용)
     */
    private AiConsulting generateAiConsulting(CreateReportRequest user, Announcement announcement, String status, List<DetailItem> details) {
        String userProfileStr = formatUserProfile(user);
        String analysisResultStr = details.stream()
                .map(d -> String.format("- %s: %s (%s)", d.category(), d.passed() ? "충족" : "미충족", d.message()))
                .collect(Collectors.joining("\n"));

        String userPrompt;
        if ("PASS".equals(status)) {
            userPrompt = """
                이 공고의 '당첨자 선정 방법', '가점 기준', '제출 서류', '계약 체결 절차' 정보를 중점적으로 검색해서 알려줘.
                그리고 이를 바탕으로 당첨 확률을 높일 수 있는 전략과, 입주까지의 구체적인 로드맵(자금, 서류 등)을 조언해줘.
                """;
        } else {
            userPrompt = """
                이 공고의 '신청 자격', '소득 및 자산 보유 기준', '공급 대상별 세부 요건' 정보를 중점적으로 검색해서 알려줘.
                특히 사용자가 미충족한 항목에 대한 구체적인 조항을 찾아서 탈락 사유를 설명하고,
                '특별공급', '우선공급' 등 예외적으로 지원 가능한 다른 전형이 있는지 찾아서 대안을 제시해줘.
                """;
        }

        String filterExpression = "notice_id == " + announcement.getId();

        return chatClient.prompt()
                .advisors(ragAdvisor)
                .advisors(a -> a.param(VectorStoreDocumentRetriever.FILTER_EXPRESSION, filterExpression))
                .advisors(AdvisorParams.ENABLE_NATIVE_STRUCTURED_OUTPUT)
                .system(s -> s.text(systemPromptResource)
                        .param("userProfile", userProfileStr)
                        .param("analysisResult", analysisResultStr))
                .user(userPrompt)
                .call()
                .entity(AiConsulting.class);
    }

    private String formatUserProfile(CreateReportRequest u) {
        return String.format("""
            - 나이: 만 %d세
            - 거주지: %s (거주기간: %d년)
            - 가구원수: %d명 (미성년 자녀: %d명)
            - 무주택세대주: %s
            - 혼인여부: %s (한부모: %s)
            - 월소득: %,d원
            - 총자산: %,d원 (자동차: %,d원)
            - 청약통장: %s (납입: %d회)
            - 기타: %s, 장애인(%s), 중증(%s), 우선공급대상(%s)
            """,
            u.getAge(), u.getResidenceArea(), u.getResidencePeriod(),
            u.getHouseholdMembers(), u.getMinorChildren(),
            u.getIsHomelessHouseholder() ? "예" : "아니오",
            u.getIsMarried() ? "기혼" : "미혼", u.getIsSingleParent() ? "예" : "아니오",
            u.getMonthlyIncome(), u.getTotalAssets(), u.getCarValue(),
            u.getHasSavingsAccount() ? "보유" : "미보유", u.getPaymentCount(),
            u.getAdditionalQualifications(), u.getIsDisabled() ? "예" : "아니오", 
            u.getIsSeverelyDisabled() ? "예" : "아니오", u.getIsPrioritySupply() ? "예" : "아니오"
        );
    }

    private void saveUserDetailInfo(Long userId, CreateReportRequest request) {
        UserDetailInfo userDetailInfo = userDetailInfoRepository.findByUserId(userId)
                .orElseGet(() -> UserDetailInfo.builder().userId(userId).build());

        userDetailInfo.update(
                request.getAge(),
                request.getResidenceArea(),
                request.getResidencePeriod(),
                request.getHouseholdMembers(),
                request.getMinorChildren(),
                request.getIsHomelessHouseholder(),
                request.getIsSingleParent(),
                request.getIsMarried(),
                request.getMonthlyIncome(),
                request.getTotalAssets(),
                request.getCarValue(),
                request.getHasSavingsAccount(),
                request.getPaymentCount(),
                request.getAdditionalQualifications(),
                request.getIsDisabled(),
                request.getIsSeverelyDisabled(),
                request.getIsPrioritySupply()
        );

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
                .orElse(new AnnouncementCriteria.AssetLimitRule(0, 255000000L, 37080000L));

        boolean assetPassed = user.getTotalAssets() <= assetRule.assetLimit();
        details.add(new DetailItem("총자산", assetPassed, String.format("%,d원", user.getTotalAssets()),
                String.format("%,d원 이하", assetRule.assetLimit()),
                assetPassed ? "자산 보유 기준 이내입니다." : "총자산 기준을 초과하였습니다."));

        // 소득 분석
        Long benchmark = criteria.getIncomeBenchmark().getOrDefault(Math.min(user.getHouseholdMembers(), 8), 7000000L);
        long incomeLimit = (long) (benchmark * 0.7);
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
            return 1L; 
        }
        return 1L;
    }
}
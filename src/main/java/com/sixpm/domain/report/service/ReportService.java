package com.sixpm.domain.report.service;

import com.sixpm.domain.announcement.dto.response.AnnouncementListResponse;
import com.sixpm.domain.announcement.dto.response.AnnouncementListResponse.AnnouncementItem;
import com.sixpm.domain.announcement.entity.Announcement;
import com.sixpm.domain.announcement.repository.AnnouncementRepository;
import com.sixpm.domain.report.dto.request.CreateReportRequest;
import com.sixpm.domain.report.dto.request.SearchQueryRequest;
import com.sixpm.domain.report.entity.UserDetailInfo;
import com.sixpm.domain.report.repository.UserDetailInfoRepository;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
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
public class ReportService {

  private final UserDetailInfoRepository userDetailInfoRepository;
  private final VectorStore vectorStore;
  private final AnnouncementRepository announcementRepository;
  private final RewriteQueryTransformer queryTransformer;

  public ReportService(
      UserDetailInfoRepository userDetailInfoRepository,
      VectorStore vectorStore,
      AnnouncementRepository announcementRepository,
      ChatClient.Builder chatClientBuilder
  ) {
    this.userDetailInfoRepository = userDetailInfoRepository;
    this.vectorStore = vectorStore;
    this.announcementRepository = announcementRepository;

    // 쿼리 최적화기 초기화 (ChatClient 사용)
    this.queryTransformer = RewriteQueryTransformer.builder()
        .chatClientBuilder(chatClientBuilder)
        .build();
  }

  /**
   * 공고 자연어 검색 (RAG + Query Optimization)
   */
  public List<AnnouncementItem> searchAnnouncements(SearchQueryRequest request) {
    log.info("Original query: {}", request.query());

    // 1. 쿼리 최적화 (Rewrite)
    Query originalQuery = new Query(request.query());
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

  private AnnouncementListResponse.AnnouncementItem convertToAnnouncementItem(
      Announcement announcement) {
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

    if (startDate == null || endDate == null) {
      return "공고중";
    }
    if (today.compareTo(startDate) < 0) {
      return "공고중";
    }
    if (today.compareTo(startDate) >= 0 && today.compareTo(endDate) <= 0) {
      return "접수중";
    }
    return "접수마감";
  }

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


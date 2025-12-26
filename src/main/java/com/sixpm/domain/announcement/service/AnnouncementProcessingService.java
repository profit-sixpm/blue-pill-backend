package com.sixpm.domain.announcement.service;

import com.sixpm.domain.ai.dto.CriteriaResponse;
import com.sixpm.domain.ai.service.ingestion.CriteriaExtractor;
import com.sixpm.domain.ai.service.ingestion.VectorIngester;
import com.sixpm.domain.announcement.dto.response.ParsedDocument;
import com.sixpm.domain.announcement.entity.Announcement;
import com.sixpm.domain.announcement.entity.AnnouncementCriteria;
import com.sixpm.domain.announcement.repository.AnnouncementCriteriaRepository;
import com.sixpm.domain.announcement.repository.AnnouncementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnnouncementProcessingService {

    private final AnnouncementParseService parseService;
    private final CriteriaExtractor criteriaExtractor;
    private final VectorIngester vectorIngester;
    private final AnnouncementRepository announcementRepository;
    private final AnnouncementCriteriaRepository criteriaRepository;

    @Async
    @Transactional
    public void processAnnouncementAsync(Long announcementId) {
        log.info("Starting async processing for announcement ID: {}", announcementId);

        try {
            Announcement announcement = announcementRepository.findById(announcementId)
                    .orElseThrow(() -> new IllegalArgumentException("Announcement not found: " + announcementId));

            String pdfUrl = announcement.getPdfFileUrl();
            if (pdfUrl == null || pdfUrl.isBlank()) {
                log.warn("No PDF URL found for announcement {}. Skipping processing.", announcementId);
                return;
            }

            // 1. PDF 파싱
            ParsedDocument parsedDoc = parseService.parseAnnouncementPdf(pdfUrl);

            // 2. AI 정보 추출 (DTO 반환)
            CriteriaResponse dtoResponse = criteriaExtractor.extract(parsedDoc.fullText());

            // 3. DB 저장 (DTO -> Entity 변환)
            saveCriteria(announcement, dtoResponse);

            // 4. 벡터 저장
            vectorIngester.ingest(parsedDoc, announcement.getId(), announcement.getHouseNm());

            log.info("Successfully processed announcement ID: {}", announcementId);

        } catch (Exception e) {
            log.error("Failed to process announcement ID: {}", announcementId, e);
        }
    }

    private void saveCriteria(Announcement announcement, CriteriaResponse dto) {
        // DTO -> Entity 변환
        List<AnnouncementCriteria.AssetLimitRule> assetEntities = dto.assetLimits() == null ? Collections.emptyList() :
                dto.assetLimits().stream()
                .map(rule -> new AnnouncementCriteria.AssetLimitRule(
                        rule.childCount(), rule.assetLimit(), rule.carLimit()))
                .collect(Collectors.toList());

        List<AnnouncementCriteria.IncomeRatioRule> incomeEntities = dto.incomeRatios() == null ? Collections.emptyList() :
                dto.incomeRatios().stream()
                .map(rule -> new AnnouncementCriteria.IncomeRatioRule(
                        rule.supplyType(), rule.minHousehold(), rule.maxHousehold(), rule.childCount(), rule.ratio()))
                .collect(Collectors.toList());

        Optional<AnnouncementCriteria> existing = criteriaRepository.findByAnnouncementId(announcement.getId());

        if (existing.isPresent()) {
            AnnouncementCriteria criteria = existing.get();
            criteria.updateCriteria(
                    dto.residenceRegion(),
                    dto.minAge(),
                    dto.requiresHomeless(),
                    dto.incomeBenchmark(),
                    assetEntities,
                    incomeEntities
            );
        } else {
            AnnouncementCriteria criteria = AnnouncementCriteria.builder()
                    .announcement(announcement)
                    .residenceRegion(dto.residenceRegion())
                    .minAge(dto.minAge())
                    .requiresHomeless(dto.requiresHomeless())
                    .incomeBenchmark(dto.incomeBenchmark())
                    .assetLimits(assetEntities)
                    .incomeRatios(incomeEntities)
                    .build();
            criteriaRepository.save(criteria);
        }
    }
}

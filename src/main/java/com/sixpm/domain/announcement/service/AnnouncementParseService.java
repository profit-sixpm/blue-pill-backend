package com.sixpm.domain.announcement.service;

import com.sixpm.domain.announcement.dto.response.ParsedDocument;
import com.sixpm.infrastructure.upstage.client.UpstageClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.nio.file.Paths;

@Service
public class AnnouncementParseService {

    private static final Logger logger = LoggerFactory.getLogger(AnnouncementParseService.class);

    private final AnnouncementPdfDownloadService pdfDownloadService;
    private final UpstageClient upstageClient;

    public AnnouncementParseService(AnnouncementPdfDownloadService pdfDownloadService, UpstageClient upstageClient) {
        this.pdfDownloadService = pdfDownloadService;
        this.upstageClient = upstageClient;
    }

    /**
     * PDF URL을 통해 파일을 다운로드하고 Upstage API를 사용하여 파싱합니다.
     *
     * @param pdfFileUrl 파싱할 PDF 파일의 URL
     * @return 파싱된 문서 데이터 (전체 텍스트 및 요소 정보)
     */
    public ParsedDocument parseAnnouncementPdf(String pdfFileUrl) {
        logger.info("Starting to parse announcement PDF from URL: {}", pdfFileUrl);

        // 1. PDF 다운로드
        byte[] fileBytes = pdfDownloadService.downloadPdf(pdfFileUrl);

        // 2. 파일명 추출 (URL에서)
        String filename = extractFilenameFromUrl(pdfFileUrl);

        // 3. Upstage 파싱 요청
        ParsedDocument parsedDocument = upstageClient.parseDocument(fileBytes, filename);
        
        logger.info("Successfully parsed announcement PDF. Text length: {}", parsedDocument.fullText().length());
        
        return parsedDocument;
    }

    private String extractFilenameFromUrl(String url) {
        try {
            String path = URI.create(url).getPath();
            return Paths.get(path).getFileName().toString();
        } catch (Exception e) {
            logger.warn("Failed to extract filename from URL: {}. Using default name.", url);
            return "announcement.pdf";
        }
    }
}
package com.sixpm.domain.announcement.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.URI;

@Service
public class AnnouncementPdfDownloadService {

    private static final Logger logger = LoggerFactory.getLogger(AnnouncementPdfDownloadService.class);
    private final RestClient restClient;

    public AnnouncementPdfDownloadService(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }

    public byte[] downloadPdf(String pdfFileUrl) {
        if (pdfFileUrl == null || pdfFileUrl.isBlank()) {
            throw new IllegalArgumentException("PDF file URL cannot be null or empty");
        }

        try {
            logger.info("Downloading PDF from URL: {}", pdfFileUrl);
            byte[] fileBytes = restClient.get()
                    .uri(URI.create(pdfFileUrl))
                    .retrieve()
                    .body(byte[].class);

            if (fileBytes == null || fileBytes.length == 0) {
                throw new RuntimeException("Downloaded PDF content is empty");
            }

            logger.info("Successfully downloaded PDF. Size: {} bytes", fileBytes.length);
            return fileBytes;

        } catch (Exception e) {
            logger.error("Failed to download PDF from URL: {}", pdfFileUrl, e);
            throw new RuntimeException("Failed to download PDF", e);
        }
    }
}
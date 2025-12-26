package com.sixpm.domain.announcement.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * S3 파일 업로드 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket:}")
    private String bucketName;

    /**
     * PDF 파일을 S3에 업로드
     *
     * @param pdfBytes PDF 파일 바이트 배열
     * @param date 공고일자 (YYYYMMDD)
     * @param fileName 파일명
     * @return S3 URL
     */
    public String uploadPdf(byte[] pdfBytes, String date, String fileName) {
        if (bucketName == null || bucketName.isEmpty()) {
            log.warn("S3 bucket name is not configured. Skipping upload.");
            return "S3 bucket not configured";
        }

        try {
            // 날짜별로 폴더 구조 생성: announcements/YYYY/MM/DD/파일명
            String formattedDate = formatDate(date);
            String key = String.format("announcements/%s/%s", formattedDate, fileName);

            log.info("Uploading PDF to S3. Bucket: {}, Key: {}", bucketName, key);

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType("application/pdf")
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(pdfBytes));

            String s3Url = String.format("https://%s.s3.amazonaws.com/%s", bucketName, key);
            log.info("Successfully uploaded PDF to S3: {}", s3Url);

            return s3Url;
        } catch (Exception e) {
            log.error("Error uploading PDF to S3. FileName: {}", fileName, e);
            throw new RuntimeException("S3 업로드 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 날짜 포맷 변환 (YYYYMMDD -> YYYY/MM/DD)
     */
    private String formatDate(String date) {
        if (date == null || date.length() != 8) {
            return date;
        }

        try {
            LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyyMMdd"));
            return localDate.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        } catch (Exception e) {
            log.warn("Failed to format date: {}", date, e);
            return date;
        }
    }
}


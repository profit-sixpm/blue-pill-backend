package com.sixpm.domain.announcement.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 청약 공고 조회 및 PDF 업로드 요청
 */
@Data
@Schema(description = "청약 공고 조회 및 PDF 업로드 요청")
public class AnnouncementFetchRequest {

    @NotBlank(message = "날짜는 필수입니다")
    @Pattern(regexp = "\\d{8}", message = "날짜는 YYYYMMDD 형식이어야 합니다")
    @Schema(description = "공고일자 (YYYYMMDD)", example = "20231225")
    private String announcementDate;
}


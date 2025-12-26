package com.sixpm.domain.announcement.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 청약 공고 처리 결과 응답
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "청약 공고 처리 결과")
public class AnnouncementFetchResponse {

    @Schema(description = "처리된 공고 수")
    private Integer processedCount;

    @Schema(description = "PDF 업로드된 공고 수")
    private Integer uploadedCount;

    @Schema(description = "처리 실패 수")
    private Integer failedCount;

    @Schema(description = "처리된 공고 상세 정보")
    private List<ProcessedAnnouncement> announcements;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "처리된 공고 정보")
    public static class ProcessedAnnouncement {
        @Schema(description = "주택관리번호")
        private String houseManageNo;

        @Schema(description = "공고번호")
        private String pblancNo;

        @Schema(description = "주택명")
        private String houseNm;

        @Schema(description = "S3 업로드 URL")
        private String s3Url;

        @Schema(description = "처리 상태")
        private String status;

        @Schema(description = "에러 메시지")
        private String errorMessage;
    }
}


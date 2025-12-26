package com.sixpm.domain.announcement.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 청약공고 리스트 조회 응답 DTO
 */
@Getter
@Builder
@AllArgsConstructor
@Schema(description = "청약공고 리스트 조회 응답")
public class AnnouncementListResponse {

    @Schema(description = "공고 목록")
    private List<AnnouncementItem> announcements;

    @Schema(description = "페이지 정보")
    private PageInfo pageInfo;

    @Getter
    @Builder
    @AllArgsConstructor
    @Schema(description = "공고 아이템")
    public static class AnnouncementItem {

        @Schema(description = "공고 ID", example = "1")
        private Long id;

        @Schema(description = "공고명", example = "행복도시 5-1L1BL 공공분양주택")
        private String announcementName;

        @Schema(description = "공고일", example = "20251219")
        private String announcementDate;

        @Schema(description = "접수 시작일", example = "20251220")
        private String receptionStartDate;

        @Schema(description = "접수 종료일", example = "20251231")
        private String receptionEndDate;

        @Schema(description = "접수 상태 (접수중, 접수마감, 공고중)", example = "접수중")
        private String receptionStatus;

        @Schema(description = "지역코드", example = "11")
        private String regionCode;

        @Schema(description = "지역명", example = "서울특별시")
        private String regionName;

        @Schema(description = "PDF URL (있는 경우)")
        private String pdfUrl;

        @Schema(description = "생성일시")
        private LocalDateTime createdAt;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @Schema(description = "페이지 정보")
    public static class PageInfo {

        @Schema(description = "현재 페이지 번호 (0부터 시작)", example = "0")
        private int currentPage;

        @Schema(description = "페이지 크기", example = "20")
        private int pageSize;

        @Schema(description = "전체 요소 개수", example = "150")
        private long totalElements;

        @Schema(description = "전체 페이지 수", example = "8")
        private int totalPages;

        @Schema(description = "첫 페이지 여부", example = "true")
        private boolean first;

        @Schema(description = "마지막 페이지 여부", example = "false")
        private boolean last;
    }
}


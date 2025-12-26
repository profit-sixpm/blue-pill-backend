package com.sixpm.domain.announcement.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 청약공고 리스트 조회 요청 DTO
 */
@Getter
@Setter
@Schema(description = "청약공고 리스트 조회 요청")
public class AnnouncementListRequest {

    @Schema(description = "페이지 번호 (0부터 시작)", example = "0", defaultValue = "0")
    private int page = 0;

    @Schema(description = "페이지 크기", example = "20", defaultValue = "20")
    private int size = 20;

    @Schema(description = "지역코드 (선택)", example = "11")
    private String regionCode;

    @Schema(description = "정렬 기준 (LATEST: 최신순, RECEPTION: 접수일순)", example = "LATEST", defaultValue = "LATEST")
    private String sortBy = "LATEST";
}


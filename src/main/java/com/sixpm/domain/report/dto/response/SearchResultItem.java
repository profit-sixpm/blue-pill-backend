package com.sixpm.domain.report.dto.response;

public record SearchResultItem(
    Long announcementId,
    String title,
    Double similarity,
    String matchedContent
) {}

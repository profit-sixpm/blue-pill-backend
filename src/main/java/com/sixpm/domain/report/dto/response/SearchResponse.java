package com.sixpm.domain.report.dto.response;

import java.util.List;

public record SearchResponse(
    List<SearchResultItem> results
) {}

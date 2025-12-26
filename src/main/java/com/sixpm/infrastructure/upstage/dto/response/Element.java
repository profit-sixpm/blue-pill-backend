package com.sixpm.infrastructure.upstage.dto.response;

import java.util.List;

public record Element(
    int id,
    int page,
    String category,
    Content content,
    List<Coordinates> coordinates
) {}

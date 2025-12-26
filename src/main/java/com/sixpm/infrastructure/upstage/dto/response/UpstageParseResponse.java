package com.sixpm.infrastructure.upstage.dto.response;

import java.util.List;

public record UpstageParseResponse(
    Content content,
    List<Element> elements
) {}

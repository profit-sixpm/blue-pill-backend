package com.sixpm.domain.announcement.dto.response;

import com.sixpm.infrastructure.upstage.dto.response.Element;
import java.util.List;

public record ParsedDocument(
    String fullText,
    List<Element> elements
) {}

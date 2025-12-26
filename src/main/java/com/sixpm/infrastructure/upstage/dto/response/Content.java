package com.sixpm.infrastructure.upstage.dto.response;

public record Content(
    String html,
    String markdown,
    String text
) {}

package com.sixpm.domain.report.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.List;

public record AiConsulting(
    @JsonPropertyDescription("컨설팅 제목 (예: '당첨 가능성이 매우 높은 안정권입니다')")
    String title,
    @JsonPropertyDescription("상세 조언 내용 (적합 시 당첨 전략, 부적합 시 보완 방법)")
    String advice,
    @JsonPropertyDescription("향후 진행 로드맵 또는 대안 액션 리스트")
    List<String> steps,
    @JsonPropertyDescription("판단 근거가 된 공고문 내용이나 조항 (출처)")
    List<String> references
) {}

package com.sixpm.domain.ai.service.ingestion;

import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NoticeTextSplitter {

    private final TokenTextSplitter tokenTextSplitter;

    public NoticeTextSplitter() {
        // 기본값: 청크 크기 800 토큰, 최소 350자, 최대 2000자 등 설정 가능
        // 여기서는 기본 생성자 또는 커스텀 설정 사용
        this.tokenTextSplitter = new TokenTextSplitter(

        );
    }

    public List<Document> apply(List<Document> documents) {
        return tokenTextSplitter.apply(documents);
    }
}

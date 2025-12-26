package com.sixpm.domain.ai.service.ingestion;

import com.sixpm.domain.announcement.dto.response.ParsedDocument;
import com.sixpm.infrastructure.upstage.dto.response.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class VectorIngester {

    private static final Logger logger = LoggerFactory.getLogger(VectorIngester.class);
    
    private final VectorStore vectorStore;
    private final NoticeTextSplitter noticeTextSplitter;

    public VectorIngester(VectorStore vectorStore, NoticeTextSplitter noticeTextSplitter) {
        this.vectorStore = vectorStore;
        this.noticeTextSplitter = noticeTextSplitter;
    }

    public void ingest(ParsedDocument parsedDoc, Long noticeId, String noticeTitle) {
        if (parsedDoc == null || parsedDoc.elements().isEmpty()) {
            logger.warn("Parsed document is empty, skipping ingestion.");
            return;
        }

        // 1. 테이블이 아닌 텍스트 요소들을 하나의 긴 문자열로 병합
        StringBuilder fullTextBuilder = new StringBuilder();

        for (Element element : parsedDoc.elements()) {
            // Table은 제외 (CriteriaExtractor에서 별도로 처리됨)
            // Figure(이미지 등) 제외
            if (!"table".equalsIgnoreCase(element.category()) && 
                !"figure".equalsIgnoreCase(element.category())) {
                
                String text = element.content() != null ? element.content().text() : "";
                if (text != null && !text.isBlank()) {
                    fullTextBuilder.append(text).append("\n");
                }
            }
        }

        String fullText = fullTextBuilder.toString();
        if (fullText.isBlank()) {
            logger.info("No text content found to ingest for notice: {}", noticeTitle);
            return;
        }

        // 2. 단일 대형 Document 생성
        Document sourceDocument = new Document(
            fullText, 
            Map.of(
                "notice_id", noticeId,
                "notice_title", noticeTitle,
                "source_type", "text"
            )
        );

        // 3. 효율적인 분할 (토큰 기반, 오버랩 적용)
        List<Document> chunks = noticeTextSplitter.apply(List.of(sourceDocument));

        // 4. Vector Store 저장
        if (!chunks.isEmpty()) {
            vectorStore.add(chunks);
            logger.info("Successfully ingested {} chunks for notice: {}", chunks.size(), noticeTitle);
        }
    }
}

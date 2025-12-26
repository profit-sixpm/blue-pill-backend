package com.sixpm.infrastructure.upstage.client;

import com.sixpm.domain.announcement.dto.response.ParsedDocument;
import com.sixpm.infrastructure.upstage.dto.response.UpstageParseResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.Collections;

@Component
public class UpstageClient {

    private final RestClient restClient;
    private final String apiKey;

    public UpstageClient(
            RestClient.Builder restClientBuilder,
            @Value("${upstage.api.base-url}") String baseUrl,
            @Value("${upstage.api.key}") String apiKey
    ) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
        this.apiKey = apiKey;
    }

    public ParsedDocument parseDocument(byte[] fileBytes, String filename) {
        MultiValueMap<String, Object> body = createRequestBody(
            fileBytes, filename);

        UpstageParseResponse response = restClient.post()
                .header("Authorization", "Bearer " + apiKey)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body)
                .retrieve()
                .body(UpstageParseResponse.class);

        if (response == null) {
            return new ParsedDocument("", Collections.emptyList());
        }

        String fullText = response.content() != null && response.content().markdown() != null
                ? response.content().markdown()
                : "";
        
        return new ParsedDocument(fullText, response.elements() != null ? response.elements() : Collections.emptyList());
    }

    private static @NonNull MultiValueMap<String, Object> createRequestBody(
        byte[] fileBytes, String filename) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        // ByteArrayResource의 getFilename이 null을 반환하면 Multipart 요청에서 파일명이 누락될 수 있으므로 오버라이드
        ByteArrayResource resource = new ByteArrayResource(fileBytes) {
            @Override
            public String getFilename() {
                return filename != null && !filename.isBlank() ? filename : "document.pdf";
            }
        };

        body.add("document", resource);
        body.add("model", "document-parse");
        body.add("output_formats", "['markdown', 'html', 'text']");
        return body;
    }
}

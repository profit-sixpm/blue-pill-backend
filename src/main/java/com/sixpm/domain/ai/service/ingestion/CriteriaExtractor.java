package com.sixpm.domain.ai.service.ingestion;

import com.sixpm.domain.ai.dto.CriteriaResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.AdvisorParams;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class CriteriaExtractor {

    private static final Logger logger = LoggerFactory.getLogger(CriteriaExtractor.class);
    private final ChatClient chatClient;
    private final Resource promptResource;

    public CriteriaExtractor(
            @Qualifier("openAiChatModel") ChatModel chatModel,
            @Value("classpath:prompts/criteria-extraction-prompt.txt") Resource promptResource
    ) {
        this.chatClient = ChatClient.builder(chatModel)
                .build();
        this.promptResource = promptResource;
    }

    public CriteriaResponse extract(String tableText) {
        if (tableText == null || tableText.isBlank()) {
            logger.warn("Input text for criteria extraction is empty.");
            return CriteriaResponse.empty();
        }

        try {
            logger.info("Extracting criteria matrix from announcement tables...");
            CriteriaResponse response = chatClient.prompt()
                    .advisors(AdvisorParams.ENABLE_NATIVE_STRUCTURED_OUTPUT) // Native Structured Output 활성화
                    .user(spec -> spec.text(promptResource)
                            .param("text", tableText))
                    .call()
                    .entity(CriteriaResponse.class);

            if (response != null) {
                logger.info("Successfully extracted criteria: region={}, asset_rules={}, income_rules={}",
                        response.residenceRegion(), 
                        response.assetLimits() != null ? response.assetLimits().size() : 0,
                        response.incomeRatios() != null ? response.incomeRatios().size() : 0);
                return response;
            }
            return CriteriaResponse.empty();
        } catch (Exception e) {
            logger.error("Failed to extract criteria from text.", e);
            return CriteriaResponse.empty();
        }
    }
}

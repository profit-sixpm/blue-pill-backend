package com.sixpm.config.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for OpenAI chat model and ChatClient
 * Used for RAG-based consulting generation and query processing
 */
@Configuration
public class OpenAiConfig {

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    @Value("${spring.ai.openai.chat.base-url}")
    private String baseUrl;

    @Value("${spring.ai.openai.chat.options.model}")
    private String model;

    /**
     * Creates OpenAI chat model bean
     *
     * @return Configured OpenAiChatModel instance
     */
    @Bean
    public ChatModel chatModel() {
        OpenAiApi api = OpenAiApi.builder()
            .apiKey(apiKey)
            .baseUrl(baseUrl)
            .build();

        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model(model)
                .temperature(0.7)
                .build();

        return OpenAiChatModel.builder()
            .openAiApi(api)
            .defaultOptions(options)
            .build();
    }

    /**
     * Creates ChatClient bean for fluent chat API
     * Used throughout RAG pipeline for LLM interactions
     *
     * @param chatModel Chat model to use
     * @return Configured ChatClient instance
     */
    @Bean
    public ChatClient chatClient(ChatModel chatModel) {
        return ChatClient.builder(chatModel).build();
    }
}

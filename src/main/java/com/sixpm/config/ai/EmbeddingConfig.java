package com.sixpm.config.ai;

import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for OpenAI embedding model Uses text-embedding-3-small for cost-effective,
 * high-quality embeddings
 */
@Configuration
public class EmbeddingConfig {

  @Value("${spring.ai.openai.embedding.api-key}")
  private String apiKey;

  @Value("${spring.ai.openai.embedding.base-url}")
  private String baseUrl;

  @Value("${spring.ai.openai.embedding.options.model}")
  private String model;

  /**
   * Creates OpenAI embedding model bean text-embedding-3-small: 1536 dimensions, cost-effective
   *
   * @return Configured OpenAiEmbeddingModel instance
   */
  @Bean
  public EmbeddingModel embeddingModel() {
    OpenAiApi api = OpenAiApi.builder()
        .apiKey(apiKey)
        .baseUrl(baseUrl)
        .build();

    OpenAiEmbeddingOptions options = OpenAiEmbeddingOptions.builder()
        .model(model)
        .dimensions(1536)
        .build();

    return new OpenAiEmbeddingModel(api, MetadataMode.EMBED, options);
  }
}

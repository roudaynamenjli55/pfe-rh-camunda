package org.example.rhcamunda.Config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuration Spring AI avec OpenRouter (compatible OpenAI API).
 *
 * Constructeur complet Spring AI 1.0.0-M5 :
 * OpenAiApi(baseUrl, apiKey, headers, completionsPath, embeddingsPath,
 *           RestClient.Builder, WebClient.Builder, ResponseErrorHandler)
 *
 * ⚠️ ResponseErrorHandler ne peut PAS être null (assertion interne Spring AI)
 *    → on passe DefaultResponseErrorHandler()
 */
@Configuration
public class AiConfig {

    @Value("${spring.ai.openai.api-key:not-configured}")
    private String apiKey;

    @Bean
    public OpenAiApi openAiApi() {
        // Headers spécifiques OpenRouter (HTTP-Referer et X-Title sont requis)
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("HTTP-Referer", "http://localhost:4200");
        headers.add("X-Title", "RH-Chatbot-Bancaire");

        RestClient.Builder restClientBuilder = RestClient.builder()
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey);

        WebClient.Builder webClientBuilder = WebClient.builder()
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey);

        return new OpenAiApi(
                "https://openrouter.ai/api/v1", // Base URL OpenRouter
                apiKey,
                headers,
                "/chat/completions",
                "/embeddings",
                restClientBuilder,
                webClientBuilder,
                new DefaultResponseErrorHandler()  // ✅ NON NULL — requis par Spring AI M5
        );
    }

    @Bean
    public OpenAiChatModel openAiChatModel(OpenAiApi openAiApi) {
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model("openai/gpt-4o-mini")
                .temperature(0.3)
                .maxTokens(1000)
                .build();

        return new OpenAiChatModel(openAiApi, options);
    }

    @Bean
    public ChatClient chatClient(OpenAiChatModel openAiChatModel) {
        return ChatClient.builder(openAiChatModel).build();
    }
}
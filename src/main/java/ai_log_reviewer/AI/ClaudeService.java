package ai_log_reviewer.AI;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
public class ClaudeService {
    private final WebClient webClient;

    public ClaudeService(
            @Value("${claude.api-key}") String apiKey
    ) {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.anthropic.com")
                .defaultHeader("x-api-key", apiKey)
                .defaultHeader("anthropic-version", "2023-06-01")
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    public String review(String diff) {
        Map<String, Object> requestBody = Map.of(
                "model", "claude-sonnet-4-6",
                "max_tokens", 1024,
                "messages", List.of(
                        Map.of("role", "user", "content", buildPrompt(diff))
                )
        );

        return webClient.post()
                .uri("/v1/messages")
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        response.bodyToMono(String.class)
                                .flatMap(body -> {
                                    if (body.contains("credit balance is too low")) {
                                        System.out.println("Claude API error: Credits too low");
                                    } else {
                                        System.out.println("Claude API error: " + body);
                                    }
                                    return reactor.core.publisher.Mono.empty();
                                })
                )
                .bodyToMono(ClaudeResponse.class)
                .map(response -> response.content().get(0).text())
                .block();
    }

    private String buildPrompt(String diff) {
        return """
                You are an expert code reviewer. Review the following pull request diff and provide feedback on:
                - Correctness and logic
                - Error handling
                - Code quality and naming
                - Any potential bugs or edge cases
                - Test coverage gaps
                
                Be concise and constructive. Format your response as markdown.
                
                ## Diff
                """ + diff;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ClaudeResponse(List<ContentBlock> content) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ContentBlock(String type, String text) {}
}

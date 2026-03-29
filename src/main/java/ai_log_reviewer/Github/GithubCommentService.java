package ai_log_reviewer.Github;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.util.Map;

@Service
public class GithubCommentService {

    private final WebClient webClient;

    public GithubCommentService(
            WebClient.Builder webClientBuilder,
            @Value("${github.token}") String ghToken
    ) {
        this.webClient = webClientBuilder
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create().followRedirect(true)))
                .defaultHeader("Authorization", "Bearer " + ghToken)
                .defaultHeader("Accept", "application/vnd.github+json")
                .defaultHeader("X-GitHub-Api-Version", "2022-11-28")
                .build();
    }

    public void postComment(String reviewMessage, String repoFullName, int prNumber) {
        Map<String, String> body = Map.of("body", reviewMessage);

        webClient.post()
                .uri("https://api.github.com/repos/" + repoFullName + "/issues/" + prNumber + "/comments")
                .bodyValue(body)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        response.bodyToMono(String.class)
                                .flatMap(error -> {
                                    System.out.println("Failed to post comment: " + error);
                                    return reactor.core.publisher.Mono.empty();
                                })
                )
                .bodyToMono(Void.class)
                .block();

        System.out.println("Review comment posted to PR #" + prNumber);
    }

}

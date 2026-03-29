package ai_log_reviewer.Github;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Service
public class GithubService {

    private final WebClient webClient;

    public GithubService(
        @Value("${github.token}") String ghToken
    ) {
        this.webClient = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create().followRedirect(true)))
                .defaultHeader("Authorization", "Bearer " + ghToken)
                .defaultHeader("Accept", "application/vnd.github+json")
                .build();
    }

    //Fetch git diff

    public String getDiff(String diffUrl) {
        return webClient.get().uri(diffUrl).retrieve().bodyToMono(String.class).block();
    }
    //Fetch git issue

}

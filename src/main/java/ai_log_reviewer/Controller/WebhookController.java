package ai_log_reviewer.Controller;

import ai_log_reviewer.AI.ClaudeService;
import ai_log_reviewer.Github.GithubService;
import ai_log_reviewer.Model.PullRequestEvent;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhook")
public class WebhookController {

    private final GithubService gitHubService;

    private final ClaudeService claudeService;

    public WebhookController(GithubService gitHubService, ClaudeService claudeService) {
        this.gitHubService = gitHubService;
        this.claudeService = claudeService;
    }

    @PostMapping("/github")
    public ResponseEntity<Void> handle(
            @RequestHeader("X-GitHub-Event") String event,
            @RequestBody PullRequestEvent prEvent) {
        System.out.println("Webhook received");
        System.out.println("PR Title: " + prEvent.pullRequest().title());
        System.out.println("Repo: " + prEvent.repository().fullName());

        String diff = gitHubService.getDiff(prEvent.pullRequest().diffUrl());
        String review = claudeService.review(diff);

        System.out.println(review);
        return ResponseEntity.ok().build();
    }
}

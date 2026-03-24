package ai_log_reviewer.Controller;

import ai_log_reviewer.Github.GithubService;
import ai_log_reviewer.Model.PullRequestEvent;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/webhook")
public class WebhookController {

    private final GithubService gitHubService;

    public WebhookController(GithubService gitHubService) {
        this.gitHubService = gitHubService;
    }

    @PostMapping("/github")
    public ResponseEntity<Void> handle(
            @RequestHeader("X-GitHub-Event") String event,
            @RequestBody PullRequestEvent prEvent) {
        System.out.println("Webhook received");
        System.out.println("Action: " + prEvent.action());
        System.out.println("PR Title: " + prEvent.pullRequest().title());
        System.out.println("Diff URL: " + prEvent.pullRequest().diffUrl());
        System.out.println("Repo: " + prEvent.repository().fullName());

        System.out.println("Diff:" + gitHubService.getDiff(prEvent.pullRequest().diffUrl()));
        return ResponseEntity.ok().build();
    }
}

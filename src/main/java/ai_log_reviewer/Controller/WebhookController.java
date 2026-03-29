package ai_log_reviewer.Controller;

import ai_log_reviewer.AI.ClaudeService;
import ai_log_reviewer.Github.GithubCommentService;
import ai_log_reviewer.Github.GithubService;
import ai_log_reviewer.Model.PullRequestEvent;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhook")
public class WebhookController {

    private final GithubService gitHubService;

    private final ClaudeService claudeService;

    private final GithubCommentService commentService;

    public WebhookController(GithubService gitHubService, ClaudeService claudeService, GithubCommentService commentService) {
        this.gitHubService = gitHubService;
        this.claudeService = claudeService;
        this.commentService = commentService;
    }

    @PostMapping("/github")
    public ResponseEntity<Void> handle(
            @RequestHeader("X-GitHub-Event") String event,
            @RequestBody PullRequestEvent prEvent) {
        System.out.println("Webhook received");
        String diff = gitHubService.getDiff(prEvent.pullRequest().diffUrl());
        String review = claudeService.review(diff);
        commentService.postComment(review, prEvent.repository().fullName(), prEvent.pullRequest().number());

        return ResponseEntity.ok().build();
    }
}

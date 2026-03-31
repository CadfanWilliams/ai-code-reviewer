package ai_log_reviewer.Controller;

import ai_log_reviewer.AI.ClaudeService;
import ai_log_reviewer.Github.GithubCommentService;
import ai_log_reviewer.Github.GithubService;
import ai_log_reviewer.Model.PullRequestEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/webhook")
public class WebhookController {

    private static final Logger log = LoggerFactory.getLogger(WebhookController.class);
    private final GithubService gitHubService;
    private final ClaudeService claudeService;
    private final GithubCommentService commentService;
    private static final Set<String> HANDLE_ACTIONS = Set.of("opened", "synchronize");

    public WebhookController(GithubService gitHubService, ClaudeService claudeService, GithubCommentService commentService) {
        this.gitHubService = gitHubService;
        this.claudeService = claudeService;
        this.commentService = commentService;
    }

    @PostMapping("/github")
    public ResponseEntity<Void> handle(
            @RequestHeader("X-GitHub-Event") String event,
            @RequestBody PullRequestEvent prEvent) {

        if(!event.equals("pull_request")) {
            log.debug("Ignoring event type: {}", event);
            return ResponseEntity.ok().build();
        }
        if(!HANDLE_ACTIONS.contains(prEvent.action())){
            log.debug("Ignoring PR action: {}", prEvent.action());
            return ResponseEntity.ok().build();
        }

        if (prEvent.pullRequest() == null || prEvent.repository() == null) {
            log.warn("Received pull_request event with null pullRequest or repository — skipping");
            return ResponseEntity.badRequest().build();
        }

        int prNumber = prEvent.pullRequest().number();
        String repoFullName = prEvent.repository().fullName();
        String diffUrl = prEvent.pullRequest().diffUrl();
        log.info("Reviewing PR #{} on {} (action: {})", prNumber, repoFullName, prEvent.action());

        if(diffUrl.isEmpty()) {
            log.warn("Empty diff URL for PR #{} on {} — skipping review", prNumber, repoFullName);
            return ResponseEntity.ok().build();
        }
        String diff;
        try {
            diff = gitHubService.getDiff(diffUrl);
        } catch (Exception e) {
            log.error("Failed to process PR #{} on {}: {}", prNumber, repoFullName, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }

        if (diff == null || diff.isBlank()) {
            log.warn("Empty diff for PR #{} on {} — skipping review", prNumber, repoFullName);
            return ResponseEntity.ok().build();
        }

        String review;
        try {
            review = claudeService.review(diff);
        } catch (Exception e) {
            log.error("Failed to retrieve review for #{} on {}: {}", prNumber, repoFullName, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }

        if (review == null || review.isBlank()) {
            log.warn("Empty review returned for PR #{} — skipping comment", prNumber);
            return ResponseEntity.ok().build();
        }

        commentService.postComment(review, repoFullName, prNumber);
        return ResponseEntity.ok().build();
    }
}

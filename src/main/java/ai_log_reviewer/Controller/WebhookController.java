package ai_log_reviewer.Controller;

import ai_log_reviewer.Model.PullRequestEvent;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/webhook")
public class WebhookController {

    ObjectMapper mapper = new ObjectMapper();
    @PostMapping("/github")
    public ResponseEntity<Void> handle(
            @RequestHeader("X-GitHub-Event") String event,
            @RequestBody String rawPayload) {
        System.out.println("Webhook received");
        PullRequestEvent pr = mapper.convertValue(rawPayload, PullRequestEvent.class);
        System.out.printf("Hello %s. Thankyou for your pull request: %s I am now going to review it ", pr.repository().fullName(), pr.pullRequest().title());
        return ResponseEntity.ok().build();
    }
}

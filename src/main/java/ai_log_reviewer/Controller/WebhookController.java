package ai_log_reviewer.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhook")
public class WebhookController {

    @PostMapping("/github")
    public ResponseEntity<Void> handle(
            @RequestHeader("X-GitHub-Event") String event,
            @RequestBody String rawPayload) {
        System.out.println("Webhook received");
        System.out.println("Event type: " + event);
        System.out.println("Payload: " + rawPayload);
        //Turn into PullRequestEvent
        //Send to prompt generator
        return ResponseEntity.ok().build();
    }
}

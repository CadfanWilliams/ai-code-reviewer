package com.cadfanwilliams.aicodereviewer.controller;

import com.cadfanwilliams.aicodereviewer.ai.ClaudeService;
import com.cadfanwilliams.aicodereviewer.github.GithubCommentService;
import com.cadfanwilliams.aicodereviewer.github.GithubService;
import com.cadfanwilliams.aicodereviewer.model.PullRequestEvent;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

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

        if(!Objects.equals(prEvent.action(), "closed")){
            String diff = gitHubService.getDiff(prEvent.pullRequest().diffUrl());
            String review = claudeService.review(diff);
            commentService.postComment(review, prEvent.repository().fullName(), prEvent.pullRequest().number());
        } else {
            System.out.println(prEvent.pullRequest().title() + " Closed");
        }

        return ResponseEntity.ok().build();
    }
}

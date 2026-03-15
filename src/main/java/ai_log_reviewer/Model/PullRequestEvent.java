package ai_log_reviewer.Model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PullRequestEvent(
        String action,

        @JsonProperty("pull_request")
        PullRequest pullRequest,

        Repository repository
) {
    public record PullRequest(
            String number,
            String title,
            String body,

            @JsonProperty("diff_url")
            String diffUrl
    ) {}

    public record Repository(
            @JsonProperty("full_name")
            String fullName
    ) {}
}

package ai_log_reviewer.Model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PullRequestEvent(
        String action,

        //this is the schema for the git diff!
        @JsonProperty("pull_request")
        PullRequest pullRequest,

        //This is the pull request event!
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

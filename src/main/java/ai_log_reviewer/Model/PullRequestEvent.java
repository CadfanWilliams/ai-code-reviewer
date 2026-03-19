package ai_log_reviewer.Model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PullRequestEvent(
        String action,
        @JsonProperty("pull_request")
        PullRequest pullRequest,

        Repository repository
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PullRequest(
            String number,
            String title,
            String body,

            @JsonProperty("diff_url")
            String diffUrl
    ) {}
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Repository(
            @JsonProperty("full_name")
            String fullName
    ) {}
}
package ai_log_reviewer.Model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PullRequestEvent(
        String action,

        //this is the schema for the git diff!
        @JsonProperty("pull_request")
        PullRequest pullRequest,

        //This is the pull request event!
        Repository repository
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PullRequest(
            int number,
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
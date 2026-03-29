# ai-code-reviewer

<details>

<summary>Test Run 1</summary>

I made a dummy pull request that was obviously incorrect and below is the statement Claude sonnet 4-6 made

This was a small PR and I assume that it will scale will repo and PR size but this single call cost £0.01

# Code Review

## Summary
This PR appears to make several miscellaneous changes including IDE configuration updates, debug print statements, and minor code comments. There are several concerns across all changed files.

---

## Issues

### ? Critical

#### `AiLogReviewerApplication.java` ? Debug Print Statements
```java
System.out.println("Now this is a test!");
System.out.println("Double time!");
```
These look like accidental debug lines left in from testing. They serve no purpose in production code and should be removed. Even the existing `"Listening for webhooks!"` line would be better served by a proper logger (e.g., `log.info(...)`).

**Recommendation:** Remove these lines entirely and replace startup messages with a proper logging framework (SLF4J/Logback):
```java
private static final Logger log = LoggerFactory.getLogger(AiLogReviewerApplication.class);
// ...
log.info("Application started. Listening for webhooks.");
```

---

### ? Moderate

#### `PullRequestEvent.java` ? Low-Quality Comments
```java
//this is the schema for the git diff!
//This is the pull request event!
```
- The comments are inaccurate or misleading (`PullRequest` is not "the schema for the git diff" ? it's the pull request object).
- Inconsistent capitalization between comments.
- Comments like these add noise rather than clarity. The field names and types are already self-documenting.

**Recommendation:** Remove these comments, or replace them with meaningful Javadoc if clarification is truly needed:
```java
/** The pull request details associated with this event. */
@JsonProperty("pull_request")
PullRequest pullRequest,
```

---

### ? Minor

#### `.idea/compiler.xml` ? IDE Config in Version Control
The change renames the annotation processor profile and removes an explicit Lombok JAR path. While this may fix a build issue, IDE config files (`.idea/`) are generally noisy in PRs and can cause conflicts across developer environments.

**Recommendation:** Consider adding `.idea/` to `.gitignore` (or at minimum `.idea/compiler.xml`) and documenting the Lombok setup in a README instead.

---

## Summary Table

| File | Severity | Issue |
|---|---|---|
| `AiLogReviewerApplication.java` | ? Critical | Debug print statements committed to production code |
| `PullRequestEvent.java` | ? Moderate | Inaccurate, noisy, and inconsistent comments |
| `.idea/compiler.xml` | ? Minor | IDE config churn; consider gitignoring |

---

## Test Coverage
No tests were added or modified in this PR. Given that even small changes to model records (like `PullRequestEvent`) can affect deserialization behavior, consider adding or verifying unit tests for JSON deserialization of `PullRequestEvent`.

---

**Overall:** This PR should not be merged as-is. The debug print statements are the most pressing concern and suggest the change was not ready for review.

</details>


<details>

<summary>Test Run 2 with actual code </summary>

## Code Review

### `GithubCommentService.java`

#### ? Bugs / Correctness

**Silent failure on 4xx errors** ? The `onStatus` handler swallows 4xx errors by returning `Mono.empty()`, which means the caller has no idea the comment failed. The `System.out.println("Review comment posted...")` will still execute even after a 401/403/404.

```java
// Current: misleading success log even on failure
.onStatus(HttpStatusCode::is4xxClientError, response ->
        response.bodyToMono(String.class)
                .flatMap(error -> {
                    System.out.println("Failed to post comment: " + error);
                    return reactor.core.publisher.Mono.empty(); // ? swallows the error
                })
)
// ...
System.out.println("Review comment posted to PR #" + prNumber); // always runs
```

Fix: propagate an exception instead, and handle 5xx errors too.

```java
.onStatus(HttpStatusCode::is4xxClientError, response ->
        response.bodyToMono(String.class)
                .flatMap(error -> Mono.error(new RuntimeException("GitHub API error: " + error)))
)
.onStatus(HttpStatusCode::is5xxServerError, response ->
        Mono.error(new RuntimeException("GitHub server error")))
```

**`repoFullName` not sanitised in URI** ? If `repoFullName` contains unexpected characters (e.g. spaces from a malformed event), the URL will be invalid. Use `UriComponentsBuilder` or the `uri(fn)` overload:

```java
.uri(uriBuilder -> uriBuilder
    .scheme("https").host("api.github.com")
    .path("/repos/{repo}/issues/{pr}/comments")
    .build(repoFullName, prNumber))
```

---

#### ?? Design / Quality

**Blocking call in a reactive chain** ? `.block()` is acceptable for a simple synchronous controller, but it ties up a thread and defeats the purpose of using `WebClient`. Consider returning `Mono<Void>` from `postComment` and subscribing in the controller, or switch to `RestClient` if blocking is intentional.

**`System.out.println` instead of a logger** ? Used throughout both files. Replace with SLF4J:

```java
private static final Logger log = LoggerFactory.getLogger(GithubCommentService.class);
log.error("Failed to post comment: {}", error);
```

**Hardcoded base URL** ? `https://api.github.com` is hardcoded. Extract to a `@Value`-injected property to support GitHub Enterprise or testing.

**Constructor over-builds the `WebClient`** ? `followRedirect(true)` is unusual for a JSON API; GitHub doesn't return redirects for comment endpoints. This adds unnecessary complexity and could mask redirect-based security issues.

---

### `WebhookController.java`

#### ? Bugs / Correctness

**No event type filtering** ? The handler processes every GitHub event (push, star, fork, etc.), but `prEvent.pullRequest()` will likely be `null` for non-PR events, causing a `NullPointerException`.

```java
// Add guard
if (!"pull_request".equals(event)) {
    return ResponseEntity.ok().build();
}
// Also check action: only review "opened" / "synchronize"
if (!"opened".equals(prEvent.action()) && !"synchronize".equals(prEvent.action())) {
    return ResponseEntity.ok().build();
}
```

**No null checks on the diff or review** ? If `getDiff` returns `null`/empty or `claudeService.review` returns null, `postComment` will post an empty/null comment or throw.

**Removed useful debug logging** ? The PR title and repo name logs were removed with no replacement. At minimum, add structured logging:

```java
log.info("Handling PR #{} on {}", prEvent.pullRequest().number(), prEvent.repository().fullName());
```

---

### Test Coverage Gaps

| Scenario | Covered? |
|---|---|
| 4xx response from GitHub API | ? |
|

</details>

pretty neat 



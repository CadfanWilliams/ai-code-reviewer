package ai_log_reviewer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AiLogReviewerApplication {

	public static void main(String[] args) {
		SpringApplication.run(AiLogReviewerApplication.class, args);
		System.out.println("Listening for webhooks!");
		System.out.println("Now this is a test!");
		System.out.println("Double time!");
	}

}

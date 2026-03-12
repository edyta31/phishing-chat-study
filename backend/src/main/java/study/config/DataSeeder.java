package study.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import study.model.Task;
import study.repository.TaskRepository;

/**
 * Seeds 4 placeholder tasks for the user study when the database is empty
 */
@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedTasks(TaskRepository tasks) {
        return args -> {
            if (tasks.count() > 0) return;
            tasks.save(createTask("email", "Email from bank", "<div style=\"font-family: sans-serif;\"><p>Dear customer,</p><p>Your account has been locked. <a href=\"http://suspicious.example.com/login\">Click here</a> to verify your identity.</p><p>Bank Security Team</p></div>", "phish"));
            tasks.save(createTask("email", "Shipping notification", "<div style=\"font-family: sans-serif;\"><p>Hi,</p><p>Your order #12345 has shipped. Track it at the carrier's official website.</p><p>Thanks,<br>Store Team</p></div>", "legit"));
            tasks.save(createTask("post", "Social media post", "<p>Free iPhone giveaway! Click the link in my bio and enter your email to win.</p>", "phish"));
            tasks.save(createTask("site", "University login", "https://httpbin.org/html", "legit"));
        };
    }

    private static Task createTask(String kind, String title, String payload, String groundTruth) {
        Task t = new Task();
        t.setKind(kind);
        t.setTitle(title);
        t.setPayload(payload);
        t.setGroundTruth(groundTruth);
        return t;
    }
}

package study.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import study.model.Task;
import study.repository.TaskRepository;

/**
 * Seeds 5 example tasks for the user study when the database is empty.
 * Order of insertion matches the desired fixed sequence for all participants:
 * 1) mail1 (phishing internal email), 2) site1 (legit UPS site),
 * 3) mail3 (phishing Outlook storage email), 4) message1 (phishing SMS),
 * 5) mail2 (ambivalent Notino email).
 */
@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedTasks(TaskRepository tasks) {
        return args -> {
            if (tasks.count() > 0) return;

            // 1) mail1 – phishing internal company email
            tasks.save(createTask(
                    "email",
                    "Company internal email",
                    "<div style=\"text-align:center; font-family: sans-serif;\">" +
                            "<img src=\"/examples/mail1.png\" alt=\"Company internal email\" style=\"max-width: 100%; height: auto;\" />" +
                            "</div>",
                    "Email from IT Support (noreply@ms-support.com), subject 'Set up your company account password'. Sender marked outside your organisation. Asks to click a link to set password.",
                    "phish"
            ));

            // 2) site1 – legitimate UPS login website
            tasks.save(createTask(
                    "website",
                    "UPS login page",
                    "<div style=\"text-align:center; font-family: sans-serif;\">" +
                            "<img src=\"/examples/site1.png\" alt=\"UPS login page\" style=\"max-width: 100%; height: auto;\" />" +
                            "</div>",
                    "Browser showing id.ups.com with UPS logo, Welcome, email/username field, Continue button. HTTPS padlock visible.",
                    "legit"
            ));

            // 3) mail3 – phishing Outlook storage full email
            tasks.save(createTask(
                    "email",
                    "Outlook mailbox full",
                    "<div style=\"text-align:center; font-family: sans-serif;\">" +
                            "<img src=\"/examples/mail3.png\" alt=\"Outlook mailbox full\" style=\"max-width: 100%; height: auto;\" />" +
                            "</div>",
                    "Email from 'Outlook Mail' (noreply@outlook-storage-help.com). Subject: Your Mailbox is 95% Full. Progress bar 14.35 GB of 15 GB. Button 'Manage Storage Settings'. Warns of delayed or failed delivery.",
                    "phish"
            ));

            // 4) message1 – phishing post office message / SMS
            tasks.save(createTask(
                    "sms",
                    "Post office delivery message",
                    "<div style=\"text-align:center; font-family: sans-serif;\">" +
                            "<img src=\"/examples/message1.png\" alt=\"Post office SMS\" style=\"max-width: 100%; height: auto;\" />" +
                            "</div>",
                    "SMS from 'Post Office': unable to deliver parcel due to unpaid shipping fee; asks to confirm details at postoffice-delivery-help.com",
                    "phish"
            ));

            // 5) mail2 – ambivalent Notino order update email
            tasks.save(createTask(
                    "email",
                    "Notino order update (ambivalent)",
                    "<div style=\"text-align:center; font-family: sans-serif;\">" +
                            "<img src=\"/examples/mail2.png\" alt=\"Notino order update\" style=\"max-width: 100%; height: auto;\" />" +
                            "</div>",
                    "Email from Notino Customer Care (support@notino.com), subject 'Additional information required for your order'. Asks to review delivery details due to possible incomplete postal code.",
                    "ambivalent"
            ));
        };
    }
    private static Task createTask(String kind, String title, String payload, String contentDescription, String groundTruth) {
        Task t = new Task();
        t.setKind(kind);
        t.setTitle(title);
        t.setPayload(payload);
        t.setContentDescription(contentDescription);
        t.setGroundTruth(groundTruth);
        return t;
    }
}

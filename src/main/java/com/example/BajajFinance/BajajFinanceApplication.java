package com.example.BajajFinance;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class BajajFinanceApplication implements CommandLineRunner {

    private final RestTemplate restTemplate = new RestTemplate();

    public static void main(String[] args) {
        SpringApplication.run(BajajFinanceApplication.class, args);
    }

    @Override
    public void run(String... args) {
        try {
            // ---------------------------
            // 1) Replace these with your details
            // ---------------------------
            String name = "Afreen Samiullah";        // 👈 put your real name
            String regNo = "22BLC1043";             // 👈 put your actual registration number
            String email = "afreensamiullah2k4@gmail.com";      // 👈 put your email

            // ---------------------------
            // 2) Call the generateWebhook API
            // ---------------------------
            String generateUrl = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";

            Map<String, String> generateBody = new HashMap<>();
            generateBody.put("name", name);
            generateBody.put("regNo", regNo);
            generateBody.put("email", email);

            HttpHeaders genHeaders = new HttpHeaders();
            genHeaders.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> genEntity = new HttpEntity<>(generateBody, genHeaders);

            ResponseEntity<Map> genResponse = restTemplate.postForEntity(generateUrl, genEntity, Map.class);

            if (!genResponse.getStatusCode().is2xxSuccessful()) {
                System.err.println("❌ Failed to get webhook. Status: " + genResponse.getStatusCode());
                return;
            }

            Map bodyMap = genResponse.getBody();
            String webhookUrl = (String) bodyMap.get("webhook");
            String accessToken = (String) bodyMap.get("accessToken");

            System.out.println("✅ Webhook URL: " + webhookUrl);
            System.out.println("✅ Access Token: " + accessToken);

            // ---------------------------
            // 3) Your SQL query goes here
            // ---------------------------
            String finalQuery = """
                    SELECT p.AMOUNT AS SALARY,
                    CONCAT(e.FIRST_NAME, ' ', e.LAST_NAME) AS NAME,
                    TIMESTAMPDIFF(YEAR, e.DOB, CURDATE()) AS AGE,
                    d.DEPARTMENT_NAME
                    FROM PAYMENTS p
                    JOIN EMPLOYEE e ON p.EMP_ID = e.EMP_ID
                    JOIN DEPARTMENT d ON e.DEPARTMENT = d.DEPARTMENT_ID
                    WHERE DAY(p.PAYMENT_TIME) <> 1
                    ORDER BY p.AMOUNT DESC
                    LIMIT 1;
                    """;

            // ---------------------------
            // 4) Submit the query to the webhook
            // ---------------------------
            Map<String, String> answerBody = new HashMap<>();
            answerBody.put("finalQuery", finalQuery);

            HttpHeaders answerHeaders = new HttpHeaders();
            answerHeaders.setContentType(MediaType.APPLICATION_JSON);

            // Some systems want "Authorization: <token>", others want "Authorization: Bearer <token>"
            // We try raw token first (per instructions)
            answerHeaders.set("Authorization", accessToken);

            HttpEntity<Map<String, String>> answerEntity = new HttpEntity<>(answerBody, answerHeaders);

            ResponseEntity<String> answerResponse = restTemplate.postForEntity(webhookUrl, answerEntity, String.class);

            System.out.println("✅ Submission Status: " + answerResponse.getStatusCode());
            System.out.println("✅ Submission Response: " + answerResponse.getBody());

        } catch (Exception ex) {
            System.err.println("❌ Error occurred:");
            ex.printStackTrace();
        } finally {
            // Exit so the program ends after running
            System.exit(0);
        }
    }
}

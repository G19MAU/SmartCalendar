package com.smartcalender.app.service;

import brevo.ApiClient;
import brevo.ApiException;
import brevo.Configuration;
import brevoApi.TransactionalEmailsApi;
import brevoModel.SendSmtpEmail;
import brevoModel.SendSmtpEmailSender;
import brevoModel.SendSmtpEmailTo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * The EmailService class is responsible for sending emails using the Brevo API.
 * It supports verification and password reset emails with dynamic parameters.
 */
@Service
public class EmailService {

    private String apiKey;
    private final TransactionalEmailsApi api;

    public EmailService(@Value("${brevo.api.key}") String apiKey) {
        System.out.println("=================================================");
        System.out.println("=== EmailService Initialization START ===");
        System.out.println("=================================================");

        this.apiKey = apiKey;

        // Diagnostic logging for API key
        if (apiKey == null) {
            System.err.println("❌ ERROR: Brevo API key is NULL!");
        } else if (apiKey.isEmpty()) {
            System.err.println("❌ ERROR: Brevo API key is EMPTY!");
        } else if (apiKey.startsWith("${")) {
            System.err.println("❌ ERROR: Brevo API key not resolved from properties: " + apiKey);
        } else {
            System.out.println("✅ Brevo API key loaded successfully");
            System.out.println("   - Key length: " + apiKey.length() + " characters");
            System.out.println("   - Key prefix: " + apiKey.substring(0, Math.min(15, apiKey.length())) + "...");
            System.out.println("   - Key starts with 'xkeysib-': " + apiKey.startsWith("xkeysib-"));
        }

        try {
            ApiClient client = Configuration.getDefaultApiClient();
            System.out.println("✅ ApiClient created");

            client.setApiKey(apiKey);
            System.out.println("✅ API key set on client");

            this.api = new TransactionalEmailsApi(client);
            System.out.println("✅ TransactionalEmailsApi initialized");
        } catch (Exception e) {
            System.err.println("❌ ERROR during EmailService initialization:");
            System.err.println("   Error type: " + e.getClass().getName());
            System.err.println("   Error message: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }

        System.out.println("=================================================");
        System.out.println("=== EmailService Initialization COMPLETE ===");
        System.out.println("=================================================");
    }


    /**
     * Sends a verification email using a Brevo template.
     *
     * @param to the recipient's email address
     * @param subject the subject of the email
     * @param verificationUrl the verification URL
     * @param otp the one-time password
     */
    public void sendVerificationEmail(String to, String subject, String verificationUrl, String otp) {
        System.out.println("=================================================");
        System.out.println("=== Sending Verification Email START ===");
        System.out.println("=================================================");
        System.out.println("   To: " + to);
        System.out.println("   Subject: " + subject);
        System.out.println("   Template ID: 1");
        System.out.println("   Verification URL: " + verificationUrl);
        System.out.println("   OTP: " + otp);

        try {
            System.out.println(">>> Creating SendSmtpEmail object...");
            SendSmtpEmail email = new SendSmtpEmail();

            System.out.println(">>> Setting sender...");
            email.setSender(new SendSmtpEmailSender().name("SmartCalendar Team").email("no-reply@smartcalendar.se"));

            System.out.println(">>> Setting recipient...");
            email.setTo(Collections.singletonList(new SendSmtpEmailTo().email(to)));

            System.out.println(">>> Setting subject...");
            email.setSubject(subject);

            System.out.println(">>> Setting template ID...");
            email.setTemplateId(1L);

            System.out.println(">>> Setting parameters...");
            Map<String, Object> params = new HashMap<>();
            params.put("verificationUrl", verificationUrl);
            params.put("otp", otp);
            email.setParams(params);
            System.out.println("   Parameters set: " + params.keySet());

            System.out.println(">>> Calling Brevo API sendTransacEmail()...");
            api.sendTransacEmail(email);

            System.out.println("=================================================");
            System.out.println("✅ Verification email sent successfully!");
            System.out.println("=================================================");

        } catch (ApiException e) {
            System.err.println("=================================================");
            System.err.println("❌ VERIFICATION EMAIL SENDING FAILED (ApiException)!");
            System.err.println("=================================================");
            System.err.println("Error code: " + e.getCode());
            System.err.println("Error message: " + e.getMessage());
            System.err.println("Error response body: " + e.getResponseBody());
            System.err.println("Error response headers: " + e.getResponseHeaders());
            System.err.println("Full stack trace:");
            e.printStackTrace();
            System.err.println("=================================================");

            throw new RuntimeException("Failed to send verification email: " + e.getMessage(), e);
        } catch (Exception e) {
            System.err.println("=================================================");
            System.err.println("❌ VERIFICATION EMAIL SENDING FAILED (General Exception)!");
            System.err.println("=================================================");
            System.err.println("Error type: " + e.getClass().getName());
            System.err.println("Error message: " + e.getMessage());
            System.err.println("Full stack trace:");
            e.printStackTrace();
            System.err.println("=================================================");

            throw new RuntimeException("Failed to send verification email: " + e.getMessage(), e);
        }
    }

    /**
     * Sends a password reset email using a Brevo template.
     *
     * @param to the recipient's email address
     * @param subject the subject of the email
     * @param resetUrl the password reset URL
     */
    public void sendPasswordResetEmail(String to, String subject, String resetUrl) {
        System.out.println("=================================================");
        System.out.println("=== Sending Password Reset Email START ===");
        System.out.println("=================================================");
        System.out.println("   To: " + to);
        System.out.println("   Subject: " + subject);
        System.out.println("   Template ID: 2");
        System.out.println("   Reset URL: " + resetUrl);

        try {
            System.out.println(">>> Creating SendSmtpEmail object...");
            SendSmtpEmail email = new SendSmtpEmail();

            System.out.println(">>> Setting sender...");
            email.setSender(new SendSmtpEmailSender().name("SmartCalendar Team").email("no-reply@smartcalendar.se"));

            System.out.println(">>> Setting recipient...");
            email.setTo(Collections.singletonList(new SendSmtpEmailTo().email(to)));

            System.out.println(">>> Setting subject...");
            email.setSubject(subject);

            System.out.println(">>> Setting template ID...");
            email.setTemplateId(2L);

            System.out.println(">>> Setting parameters...");
            Map<String, Object> params = new HashMap<>();
            params.put("resetUrl", resetUrl);
            email.setParams(params);
            System.out.println("   Parameters set: " + params.keySet());

            System.out.println(">>> Calling Brevo API sendTransacEmail()...");
            api.sendTransacEmail(email);

            System.out.println("=================================================");
            System.out.println("✅ Password reset email sent successfully!");
            System.out.println("=================================================");

        } catch (ApiException e) {
            System.err.println("=================================================");
            System.err.println("❌ PASSWORD RESET EMAIL SENDING FAILED (ApiException)!");
            System.err.println("=================================================");
            System.err.println("Error code: " + e.getCode());
            System.err.println("Error message: " + e.getMessage());
            System.err.println("Error response body: " + e.getResponseBody());
            System.err.println("Error response headers: " + e.getResponseHeaders());
            System.err.println("Full stack trace:");
            e.printStackTrace();
            System.err.println("=================================================");

            throw new RuntimeException("Failed to send password reset email: " + e.getMessage(), e);
        } catch (Exception e) {
            System.err.println("=================================================");
            System.err.println("❌ PASSWORD RESET EMAIL SENDING FAILED (General Exception)!");
            System.err.println("=================================================");
            System.err.println("Error type: " + e.getClass().getName());
            System.err.println("Error message: " + e.getMessage());
            System.err.println("Full stack trace:");
            e.printStackTrace();
            System.err.println("=================================================");

            throw new RuntimeException("Failed to send password reset email: " + e.getMessage(), e);
        }
    }
}
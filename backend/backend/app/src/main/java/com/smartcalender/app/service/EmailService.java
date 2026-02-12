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
        this.apiKey = apiKey;

        if (apiKey == null || apiKey.isEmpty() || apiKey.startsWith("${")) {
            throw new IllegalStateException("Brevo API key is not configured properly. Check EMAIL_API_KEY environment variable.");
        }

        try {
            ApiClient client = Configuration.getDefaultApiClient();
            client.setApiKey(apiKey);
            this.api = new TransactionalEmailsApi(client);
            System.out.println("✅ EmailService initialized successfully");
        } catch (Exception e) {
            System.err.println("❌ Failed to initialize EmailService: " + e.getMessage());
            throw new IllegalStateException("Failed to initialize Brevo email service", e);
        }
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
        try {
            SendSmtpEmail email = new SendSmtpEmail();
            email.setSender(new SendSmtpEmailSender().name("SmartCalendar Team").email("no-reply@smartcalendar.se"));
            email.setTo(Collections.singletonList(new SendSmtpEmailTo().email(to)));
            email.setSubject(subject);
            email.setTemplateId(1L);

            Map<String, Object> params = new HashMap<>();
            params.put("verificationUrl", verificationUrl);
            params.put("otp", otp);
            email.setParams(params);

            api.sendTransacEmail(email);
            System.out.println("✅ Verification email sent to: " + to);

        } catch (ApiException e) {
            System.err.println("❌ Failed to send verification email to " + to +
                             " - HTTP " + e.getCode() + ": " + e.getResponseBody());
            throw new RuntimeException("Failed to send verification email: " + e.getResponseBody(), e);
        } catch (Exception e) {
            System.err.println("❌ Failed to send verification email to " + to + ": " + e.getMessage());
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
        try {
            SendSmtpEmail email = new SendSmtpEmail();
            email.setSender(new SendSmtpEmailSender().name("SmartCalendar Team").email("no-reply@smartcalendar.se"));
            email.setTo(Collections.singletonList(new SendSmtpEmailTo().email(to)));
            email.setSubject(subject);
            email.setTemplateId(2L);

            Map<String, Object> params = new HashMap<>();
            params.put("resetUrl", resetUrl);
            email.setParams(params);

            api.sendTransacEmail(email);
            System.out.println("✅ Password reset email sent to: " + to);

        } catch (ApiException e) {
            System.err.println("❌ Failed to send password reset email to " + to +
                             " - HTTP " + e.getCode() + ": " + e.getResponseBody());
            throw new RuntimeException("Failed to send password reset email: " + e.getResponseBody(), e);
        } catch (Exception e) {
            System.err.println("❌ Failed to send password reset email to " + to + ": " + e.getMessage());
            throw new RuntimeException("Failed to send password reset email: " + e.getMessage(), e);
        }
    }
}
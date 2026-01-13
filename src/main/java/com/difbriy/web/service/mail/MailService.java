package com.difbriy.web.service.mail;

import java.util.concurrent.CompletableFuture;

public interface MailService {
    CompletableFuture<Void> sendWelcomeEmailAsync(String to);

    CompletableFuture<Void> sendPasswordResetEmail(String email, String resetLink);
}

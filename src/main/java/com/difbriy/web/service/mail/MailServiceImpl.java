package com.difbriy.web.service.mail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailServiceImpl implements MailService {
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;
    private static final String WELCOME_MAIL_SUBJECT_TEXT =
            """
                    Здравствуйте!
                    """;
    private static final String WELCOME_MAIL_TEXT =
            """
                    Благодарим вас за регистрацию в проекте MERO. Мы рады, что вы присоединились к нашему сообществу и стали частью инициативы, которая объединяет людей, стремящихся к развитию и новым возможностям.
                    
                    В ближайшее время вы получите доступ ко всем материалам и обновлениям проекта. Следите за новостями — впереди много интересного!
                    
                    С уважением, Команда MERO
                    """;

    private static final String RESET_MAIL_SUBJECT = """
            Password Reset Request
            """;
    private static final String RESET_MAIL_TEXT = """
            Click the link below to reset your password. The link will expire in 15 minutes.
            """;

    @Async("taskExecutor")
    @Override
    public CompletableFuture<Void> sendWelcomeEmailAsync(String to) {
        try {
            log.info("Attempting to send welcome email to: {}", to);
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom(fromEmail);
            mailMessage.setTo(to);
            mailMessage.setSubject(WELCOME_MAIL_SUBJECT_TEXT.trim());
            mailMessage.setText(WELCOME_MAIL_TEXT);

            mailSender.send(mailMessage);
            log.info("Welcome email successfully sent to: {}", to);
            return CompletableFuture.completedFuture(null);
        } catch (MailException e) {
            log.error("Failed to send welcome email to {}: {}", to, e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        } catch (Exception e) {
            log.error("Unexpected error while sending welcome email to {}: {}", to, e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public CompletableFuture<Void> sendPasswordResetEmail(String to, String resetLink) {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom(fromEmail);
            mailMessage.setTo(to);
            mailMessage.setSubject(RESET_MAIL_SUBJECT.trim());
            mailMessage.setText(RESET_MAIL_TEXT);
            mailSender.send(mailMessage);
            return CompletableFuture.completedFuture(null);
        } catch (MailException e) {
            log.error("Failed to send reset password to {}: {}", to, e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        } catch (Exception e) {
            log.error("Unexpected error while sending reset password email to {}: {}", to, e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }

    }
}

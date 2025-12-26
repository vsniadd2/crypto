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
    private static final String MAIL_SUBJECT_TEXT =
            """
                    Здравствуйте!
                    """;
    private static final String MAIL_TEXT =
            """
                    Благодарим вас за регистрацию в проекте MERO. Мы рады, что вы присоединились к нашему сообществу и стали частью инициативы, которая объединяет людей, стремящихся к развитию и новым возможностям.
                    
                    В ближайшее время вы получите доступ ко всем материалам и обновлениям проекта. Следите за новостями — впереди много интересного!
                    
                    С уважением, Команда MERO
                    """;

    @Async("taskExecutor")
    @Override
    public CompletableFuture<Void> sendWelcomeEmailAsync(String to) {
        return CompletableFuture.runAsync(() -> {
            try {
                log.info("Attempting to send welcome email to: {}", to);
                SimpleMailMessage mailMessage = new SimpleMailMessage();
                mailMessage.setFrom(fromEmail);
                mailMessage.setTo(to);
                mailMessage.setSubject(MAIL_SUBJECT_TEXT.trim());
                mailMessage.setText(MAIL_TEXT);

                mailSender.send(mailMessage);
                log.info("Welcome email successfully sent to: {}", to);
            } catch (MailException e) {
                log.error("Failed to send welcome email to {}: {}", to, e.getMessage(), e);
            } catch (Exception e) {
                log.error("Unexpected error while sending welcome email to {}: {}", to, e.getMessage(), e);
            }
        });
    }
}

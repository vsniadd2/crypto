package com.difbriy.web.service.mail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {
    private final JavaMailSender mailSender;
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

    public void createAndSentEmail(String to) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(to);
        mailMessage.setSubject(MAIL_SUBJECT_TEXT);
        mailMessage.setText(MAIL_TEXT);
        mailSender.send(mailMessage);
    }
}

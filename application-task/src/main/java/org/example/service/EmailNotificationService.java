package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationService {

    private final JavaMailSender mailSender;

    public void sendEmail(String from, String to, String subject, String body) {
        var message = new SimpleMailMessage();
        message.setTo(to);
        message.setFrom(from);
        message.setSubject(subject);
        message.setText(body);
        try {
            log.info("Отправка сообщения " + to);
            mailSender.send(message);
            log.info("Отправка сообщения успешно проведена {}", to);
        } catch (Exception e) {
            log.error("Ошибка при отправке email: ", e);
        }
    }

}

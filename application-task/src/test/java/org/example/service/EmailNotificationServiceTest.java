package org.example.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailNotificationServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailNotificationService emailNotificationService;

    @Test
    void testSendEmailSuccess() {
        var from = "test@example.com";
        var to = "recipient@example.com";
        var subject = "Test Subject";
        var body = "Test Body";

        emailNotificationService.sendEmail(from, to, subject, body);

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendEmailFailure() {
        var from = "test@example.com";
        var to = "recipient@example.com";
        var subject = "Test Subject";
        var body = "Test Body";

        doThrow(new RuntimeException("Mail sending failed"))
                .when(mailSender)
                .send(any(SimpleMailMessage.class));

        emailNotificationService.sendEmail(from, to, subject, body);

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

}

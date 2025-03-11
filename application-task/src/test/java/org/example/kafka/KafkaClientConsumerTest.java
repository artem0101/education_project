package org.example.kafka;

import java.util.List;
import org.example.config.MailProperties;
import org.example.dto.TaskDto;
import org.example.service.EmailNotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.support.Acknowledgment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class KafkaClientConsumerTest {

    private EmailNotificationService emailNotificationService;
    private KafkaClientConsumer kafkaClientConsumer;
    private Acknowledgment acknowledgment;

    @BeforeEach
    void setUp() {
        var mailProperties = mock(MailProperties.class);
        emailNotificationService = mock(EmailNotificationService.class);
        acknowledgment = mock(Acknowledgment.class);

        kafkaClientConsumer = new KafkaClientConsumer(mailProperties, emailNotificationService);

        when(mailProperties.getUsername()).thenReturn("sender@example.com");
        when(mailProperties.getRecipient()).thenReturn("recipient@example.com");
    }

    @Test
    void testListenerProcessesMessagesAndSendsEmails() {
        var messages = List.of(
                TaskDto.builder().title("Task1").status("IN_PROGRESS").build(),
                TaskDto.builder().title("Task2").status("COMPLETED").build()
        );

        kafkaClientConsumer.listener(messages, acknowledgment, "task-updates", "123");

        var subjectCaptor = ArgumentCaptor.forClass(String.class);
        var bodyCaptor = ArgumentCaptor.forClass(String.class);

        verify(emailNotificationService, times(2)).sendEmail(
                eq("sender@example.com"),
                eq("recipient@example.com"),
                subjectCaptor.capture(),
                bodyCaptor.capture()
        );

        var subjects = subjectCaptor.getAllValues();
        var bodies = bodyCaptor.getAllValues();

        assertEquals("Обновление задачи", subjects.get(0));
        assertEquals("Обновлён статус задачи Task1 на IN_PROGRESS", bodies.get(0));
        assertEquals("Обновление задачи", subjects.get(1));
        assertEquals("Обновлён статус задачи Task2 на COMPLETED", bodies.get(1));

        verify(acknowledgment, times(1)).acknowledge();
    }

    @Test
    void testListenerAcknowledgesEvenIfExceptionOccurs() {
        var messages = List.of(TaskDto.builder().title("Task1").status("IN_PROGRESS").build());

        doThrow(new RuntimeException("Email sending failed")).when(emailNotificationService)
                                                             .sendEmail(anyString(), anyString(), anyString(), anyString());

        assertThrows(RuntimeException.class, () -> kafkaClientConsumer.listener(messages, acknowledgment, "task-updates", "123"));

        verify(acknowledgment, times(1)).acknowledge();
    }
}

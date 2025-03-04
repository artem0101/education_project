package org.example.kafka;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.TaskDto;
import org.example.service.EmailNotificationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class KaflaClientConsumer {

    @Value("${spring.mail.username}")
    private String from;
    @Value("${email.message-recipient}")
    private String to;

    private final EmailNotificationService emailNotificationService;

    @KafkaListener(
            id = "${task.kafka.consumer.group-id}",
            topics = "${task.kafka.topic.task_status_updated}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void listener(
            @Payload List<TaskDto> messageList,
            Acknowledgment ack,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(name = KafkaHeaders.RECEIVED_KEY, required = false) String key
    ) {
        log.info("Получены сообщения: topic={}, key={}, payload={}, count={}", topic, key, messageList, messageList.size());
        try {
            messageList.forEach(dto -> {
                var message = "Обновлён статус задачи " + dto.getTitle() + " на " + dto.getStatus();
                emailNotificationService.sendEmail(from, to, "Обновление задачи", message);
            });
        } finally {
            ack.acknowledge();
        }
        log.info("Сообщения обработаны");
    }

}

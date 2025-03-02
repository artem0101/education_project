package org.example.kafka;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.TaskDto;
import org.example.service.TaskService;
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

    private final TaskService taskService;

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
        log.info("📩 Получено сообщение: topic={}, key={}, payload={}", topic, key, messageList);
        log.debug("Client consumer: Обработка новых сообщений");
        try {
            var taskDtos = messageList.stream().map(dto -> {
                dto.setTitle(key + "@" + dto.getTitle());
                return dto;
            }).toList();
            // TODO действия по результатам полученного сообщения.
        } finally {
            ack.acknowledge();
        }
        log.debug("Client consumer: Записи обработаны.");
    }

}

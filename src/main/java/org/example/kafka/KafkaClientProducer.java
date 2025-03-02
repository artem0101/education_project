package org.example.kafka;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class KafkaClientProducer {

    private final KafkaTemplate template;


    public void send(Long id) {
        try {
            template.sendDefault(UUID.randomUUID().toString(), id).get();
            template.flush();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public void sendTo(String topic, Object o) {
        try {
            template.send(topic, o).get();
            template.flush();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

}

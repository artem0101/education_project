package org.example.kafka;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class KafkaClientProducerTest {

    private KafkaTemplate<String, Object> kafkaTemplate;
    private KafkaClientProducer kafkaClientProducer;

    @BeforeEach
    void setUp() {
        kafkaTemplate = mock(KafkaTemplate.class);
        kafkaClientProducer = new KafkaClientProducer(kafkaTemplate);
    }

    @Test
    void testSendToSuccess() {
        var topic = "test-topic";
        var message = "Test message";

        kafkaClientProducer.sendTo(topic, message);

        var topicCaptor = ArgumentCaptor.forClass(String.class);
        var keyCaptor = ArgumentCaptor.forClass(String.class);
        var messageCaptor = ArgumentCaptor.forClass(Object.class);

        verify(kafkaTemplate, times(1)).send(topicCaptor.capture(), keyCaptor.capture(), messageCaptor.capture());
        verify(kafkaTemplate, times(1)).flush();

        assertEquals(topic, topicCaptor.getValue());
        assertNotNull(keyCaptor.getValue());
        assertEquals(message, messageCaptor.getValue());
    }

    @Test
    void testSendToExceptionHandling() {
        var topic = "test-topic";
        var message = "Test message";

        doThrow(new RuntimeException("Kafka error")).when(kafkaTemplate)
                                                    .send(anyString(), anyString(), any());

        kafkaClientProducer.sendTo(topic, message);

        verify(kafkaTemplate, times(1)).send(anyString(), anyString(), any());
    }

}

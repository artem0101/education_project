package org.example.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { KafkaConfig.class })
class KafkaConfigTest {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private KafkaConfig kafkaConfig;

    @Test
    void testConsumerFactory() {
        var consumerFactory = kafkaConfig.consumerListenerFactory();
        assertNotNull(consumerFactory);
    }

    @Test
    void testProducerFactory() {
        var producerFactory = kafkaConfig.producerFactory();
        assertNotNull(producerFactory);
    }

    @Test
    void testKafkaTemplate() {
        var kafkaTemplate = kafkaConfig.kafkaTemplate(kafkaConfig.producerFactory());
        assertNotNull(kafkaTemplate);
    }

    @Test
    void testKafkaListenerFactory() {
        var consumerFactory = kafkaConfig.consumerListenerFactory();
        var factory = kafkaConfig.factory(consumerFactory);
        assertNotNull(factory);
    }

}

package org.example.kafka;

import java.nio.charset.StandardCharsets;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.example.dto.TaskDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class MessageDeserializerTest {

    private MessageDeserializer<TaskDto> deserializer;

    @BeforeEach
    void setUp() {
        deserializer = spy(new MessageDeserializer<>());
    }

    @Test
    void testDeserialize() {
        var json = "{\"id\":1,\"title\":\"Test Task\",\"description\":\"Test Desc\",\"status\":\"NEW\"}";
        var data = json.getBytes(StandardCharsets.UTF_8);

        var deserializer = new MessageDeserializer<TaskDto>();
        deserializer.addTrustedPackages("org.example.dto");

        var headers = new RecordHeaders();
        var recordHeader = new RecordHeader("__TypeId__", TaskDto.class.getName().getBytes(StandardCharsets.UTF_8));
        headers.add(recordHeader);

        var task = deserializer.deserialize("test-topic", headers, data);

        assertThat(task).isNotNull();
        assertThat(task.getId()).isEqualTo(1);
        assertThat(task.getTitle()).isEqualTo("Test Task");
        assertThat(task.getStatus()).isEqualTo("NEW");
    }

    @Test
    void testDeserializeWithOutHeaders() {
        var json = "{\"id\":1,\"title\":\"Test Task\",\"description\":\"Test Desc\",\"status\":\"NEW\"}";
        var data = json.getBytes(StandardCharsets.UTF_8);

        var headers = new RecordHeaders();

        var task = deserializer.deserialize("test-topic", headers, data);

        assertThat(task).isNull();
    }


    @Test
    void testDeserializeInvalidJson() {
        var invalidData = "invalid json".getBytes(StandardCharsets.UTF_8);

        var task = deserializer.deserialize("test-topic", invalidData);

        assertThat(task).isNull();
        verify(deserializer, times(1)).deserialize(anyString(), any(byte[].class));
    }

}

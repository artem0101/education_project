package org.example.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = MailProperties.class)
@EnableConfigurationProperties(MailProperties.class)
class MailPropertiesTest {

    @Autowired
    private MailProperties mailProperties;

    @Test
    void testPropertiesBinding() {
        assertNotNull(mailProperties);
        assertEquals("smtp.example.com", mailProperties.getHost());
        assertEquals(587, mailProperties.getPort());
        assertEquals("user@example.com", mailProperties.getUsername());
        assertEquals("secret", mailProperties.getPassword());
        assertEquals("recipient@example.com", mailProperties.getRecipient());
    }

    @Test
    void testMailPropertiesMethod() {
        var properties = mailProperties.getMailProperties();

        assertNotNull(properties);
        assertEquals("true", properties.getProperty("mail.smtp.auth"));
        assertEquals("true", properties.getProperty("mail.smtp.starttls.enable"));
        assertEquals("true", properties.getProperty("mail.smtp.starttls.required"));
        assertEquals("true", properties.getProperty("mail.smtp.ssl.enable"));
    }
}

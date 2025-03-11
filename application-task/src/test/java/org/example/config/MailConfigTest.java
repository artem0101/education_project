package org.example.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {MailConfig.class, MailProperties.class})
@EnableConfigurationProperties(MailProperties.class)
class MailConfigTest {

    @Autowired
    private MailConfig mailConfig;

    @Autowired
    private MailProperties mailProperties;

    @Test
    void testJavaMailSenderBean() {
        var mailSender = (JavaMailSenderImpl) mailConfig.javaMailSender();

        assertNotNull(mailSender);
        assertEquals(mailProperties.getHost(), mailSender.getHost());
        assertEquals(mailProperties.getPort(), mailSender.getPort());
        assertEquals(mailProperties.getUsername(), mailSender.getUsername());
        assertEquals(mailProperties.getPassword(), mailSender.getPassword());

        var properties = mailSender.getJavaMailProperties();
        assertEquals("true", properties.getProperty("mail.smtp.auth"));
        assertEquals("true", properties.getProperty("mail.smtp.starttls.enable"));
        assertEquals("true", properties.getProperty("mail.smtp.starttls.required"));
        assertEquals("true", properties.getProperty("mail.smtp.ssl.enable"));
    }

}


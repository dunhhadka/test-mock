package com.example.demo.service;

import com.example.demo.email.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class EmailServiceTest {

    private JavaMailSender javaMailSender;
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        javaMailSender = mock(JavaMailSender.class);
        emailService = new EmailService(javaMailSender);
    }

    @Test
    @DisplayName("send - should send email successfully")
    void sendEmail_success() throws MessagingException {
        // Given
        String to = "test@example.com";
        String subject = "Test Subject";
        String content = "<p>Your code is 123456</p>";

        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        // When
        emailService.send(to, subject, content);

        // Then
        verify(javaMailSender, times(1)).send(mimeMessage);
    }
}

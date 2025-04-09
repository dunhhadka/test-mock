package com.example.demo.event;

import com.example.demo.email.EmailService;
import com.example.demo.entity.AccountEntity;
import com.example.demo.entity.ForgotPasswordEntity;
import com.example.demo.model.response.PasswordResponse;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

public class HandleEventTest {

    private EmailService emailService;
    private HandleEvent handleEvent;

    @BeforeEach
    void setUp() {
        emailService = mock(EmailService.class);
        handleEvent = new HandleEvent(emailService);
    }

    @Test
    void testHandleEventSendEmail() throws MessagingException {
        // Arrange
        AccountEntity account = new AccountEntity();
        account.setEmail("test@example.com");

        ForgotPasswordEntity forgotPassword = new ForgotPasswordEntity();
        forgotPassword.setCode("ABC123");
        forgotPassword.setAccountCreate(account);

        Event event = new Event(forgotPassword);

        // Act
        handleEvent.handleEventSendEmail(event);

        // Assert
        verify(emailService, times(1)).send(
                eq("test@example.com"),
                eq("Mã xác nhận quên mật khẩu."),
                eq("ABC123")
        );
    }

    @Test
    void testHandleSendPassword() throws MessagingException {
        // Arrange
        PasswordResponse passwordResponse = new PasswordResponse("user@example.com", "newSecurePassword123!");
        PasswordEvent event = new PasswordEvent(passwordResponse);

        // Act
        handleEvent.handleSendPassword(event);

        // Assert
        verify(emailService, times(1)).send(
                eq("user@example.com"),
                eq("Password của bạn được đặt lại là :"),
                eq("newSecurePassword123!")
        );
    }
}

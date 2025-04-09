package com.example.demo.event;

import com.example.demo.entity.AccountEntity;
import com.example.demo.entity.ForgotPasswordEntity;
import com.example.demo.model.response.PasswordResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import static org.mockito.Mockito.*;

public class PublisherEventTest {

    private ApplicationEventPublisher eventPublisher;
    private PublisherEvent publisherEvent;

    @BeforeEach
    void setUp() {
        eventPublisher = mock(ApplicationEventPublisher.class);
        publisherEvent = new PublisherEvent(eventPublisher);
    }

    @Test
    void testSendEmailPublishesEvent() {
        // Arrange
        AccountEntity account = new AccountEntity();
        account.setEmail("test@example.com");

        ForgotPasswordEntity forgotPassword = new ForgotPasswordEntity();
        forgotPassword.setAccountCreate(account);
        forgotPassword.setCode("XYZ123");

        // Act
        publisherEvent.sendEmail(forgotPassword);

        // Assert
        verify(eventPublisher, times(1)).publishEvent(any(Event.class));
    }

    @Test
    void testSendPasswordToEmailPublishesPasswordEvent() {
        // Arrange
        PasswordResponse response = new PasswordResponse("user@example.com", "securePass");

        // Act
        publisherEvent.sendPassWordToEmail(response);

        // Assert
        verify(eventPublisher, times(1)).publishEvent(any(PasswordEvent.class));
    }
}

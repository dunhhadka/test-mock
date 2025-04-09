package com.example.demo.handle;

import com.example.demo.exception.BaseException;
import com.example.demo.exception.CreateAccountException;
import com.example.demo.exception.EntityNotFoundException;
import com.example.demo.model.BaseResponse;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class ValidationExceptionTest {

    private ValidationException validationException;

    @BeforeEach
    void setUp() {
        validationException = new ValidationException();
    }

    @Test
    void handleValidationException_shouldReturnBadRequest() {
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(
                new FieldError("objectName", "email", "Email không hợp lệ")
        ));

        MethodArgumentNotValidException exception = new MethodArgumentNotValidException((MethodParameter) null, bindingResult);
        BaseResponse response = validationException.handleValidationException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getMessage()).isEqualTo("Email không hợp lệ");
        assertThat(response.getData()).isInstanceOf(Map.class);
    }

    @Test
    void handleLoginFailed_shouldReturnUnauthorized() {
        Exception ex = new BadCredentialsException("Invalid credentials");
        BaseResponse response = validationException.handleLoginFailed(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(response.getMessage()).contains("Đăng nhập không thành công");
        assertThat(response.getData()).isNull();
    }

    @Test
    void createAccountFailedByEmail_shouldReturnBadRequest() {
        Exception ex = new CreateAccountException("Tài khoản đã tồn tại");
        BaseResponse response = validationException.createAccountFailedByEmail(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getMessage()).isEqualTo("Tài khoản đã tồn tại");
        assertThat(response.getData()).isNull();
    }

    @Test
    void handleBaseException_shouldReturnBadRequest() {
        Exception ex = new BaseException("Lỗi cơ bản");
        BaseResponse response = validationException.handleBaseException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getMessage()).isEqualTo("Lỗi cơ bản");
    }

    @Test
    void handleSendEmailFailed_shouldReturnInternalServerError() {
        Exception ex = new MessagingException("Email lỗi");
        BaseResponse response = validationException.handleSendEmailFailed(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(response.getMessage()).isEqualTo("Gửi email không thành công.");
    }
}

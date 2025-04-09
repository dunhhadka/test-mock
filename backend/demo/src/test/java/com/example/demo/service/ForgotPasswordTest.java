package com.example.demo.service;
import com.example.demo.entity.AccountEntity;
import com.example.demo.entity.ForgotPasswordEntity;
import com.example.demo.entity.enums.Role;
import com.example.demo.event.PublisherEvent;
import com.example.demo.exception.BaseException;
import com.example.demo.jwt.JwtService;
import com.example.demo.model.BaseResponse;
import com.example.demo.model.request.ConfirmCode;
import com.example.demo.model.request.ForgotPassword;
import com.example.demo.model.response.PasswordResponse;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.ForgotPasswordRepository;
import com.example.demo.service.impl.ForgotPasswordService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Slf4j
@DataJpaTest
@Import(ForgotPasswordService.class)
@Sql(value = "/sql/import_account.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = "/sql/import_account_clean_up.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class ForgotPasswordTest extends AbstractBaseTest{

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ForgotPasswordRepository forgotPasswordRepository;

    @MockBean
    private PublisherEvent publisherEvent;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private JwtService jwtService;

    @Autowired
    private ForgotPasswordService forgotPasswordService;

    private AccountEntity accountEntity;

    @BeforeEach
    void setUp() {
        accountEntity = AccountEntity.builder()
                .userName("testUser")
                .email("test@example.com")
                .password("encodedOldPassword")
                .role(Role.DISPATCHER)
                .build();
        accountEntity.setId(1L);
        accountEntity.setIsActive(true);
    }


//    @Test
//    @DisplayName("ForgotPassword T01: Generate code successfully with existing account")
//    void generateCode_success() {
//        ForgotPassword request = new ForgotPassword();
//        request.setEmail("test@example.com");
//
//        when(passwordEncoder.encode(anyString())).thenReturn("encodedNewPassword");
//
//        BaseResponse response = forgotPasswordService.generateCode(request);
//
//        assertEquals(HttpStatus.CREATED.value(), response.getStatusCode());
//        assertEquals("Tạo mã thành công.", response.getMessage());
//
//        Optional<ForgotPasswordEntity> savedCode = forgotPasswordRepository.findByAccountCreate(accountEntity);
//        assertTrue(savedCode.isPresent());
//        assertNotNull(savedCode.get().getCode());
//
//        verify(publisherEvent, times(1)).sendEmail(any(ForgotPasswordEntity.class));
//    }

    @Test
    @DisplayName("ForgotPassword T02: Generate code with non-existing account")
    void generateCode_accountNotFound() {
        ForgotPassword request = new ForgotPassword();
        request.setEmail("unknown@example.com");

        BaseException exception = assertThrows(BaseException.class,
                () -> forgotPasswordService.generateCode(request));

        assertEquals("Tài khoản unknown@example.com không tồn tại.", exception.getMessage());
        verifyNoInteractions(publisherEvent);
    }

    @Test
    @DisplayName("ForgotPassword T03: Generate code and delete existing codes")
    void generateCode_deleteExistingCodes() {
        ForgotPassword request = new ForgotPassword();
        request.setEmail("test@example.com");

        ForgotPasswordEntity oldCode = ForgotPasswordEntity.builder()
                .code("OLD123")
                .accountCreate(accountEntity)
                .createAt(LocalDateTime.now().minusHours(1))
                .build();
        forgotPasswordRepository.save(oldCode);

        when(passwordEncoder.encode(anyString())).thenReturn("encodedNewPassword");

        BaseResponse response = forgotPasswordService.generateCode(request);

        assertEquals(HttpStatus.CREATED.value(), response.getStatusCode());
        assertEquals("Tạo mã thành công.", response.getMessage());

        assertEquals(1, forgotPasswordRepository.findAll().size());
        verify(publisherEvent, times(1)).sendEmail(any(ForgotPasswordEntity.class));
    }

    @Test
    @DisplayName("ForgotPassword T04: Confirm code successfully")
    void confirmCode_success() {
        ForgotPasswordEntity forgotPasswordEntity = ForgotPasswordEntity.builder()
                .code("VALID123")
                .accountCreate(accountEntity)
                .createAt(LocalDateTime.now().minusMinutes(1))
                .build();
        forgotPasswordRepository.save(forgotPasswordEntity);

        ConfirmCode request = new ConfirmCode();
        request.setCode("VALID123");

        when(passwordEncoder.encode(anyString())).thenReturn("encodedNewPassword");

        BaseResponse response = forgotPasswordService.confirmCode(request);

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertEquals("Mật khẩu mới của bạn được xác nhận ở email.", response.getMessage());

        Optional<AccountEntity> updatedAccount = accountRepository.findById(1L);
        assertTrue(updatedAccount.isPresent());
        assertEquals("encodedNewPassword", updatedAccount.get().getPassword());

        verify(publisherEvent, times(1)).sendPassWordToEmail(any(PasswordResponse.class));
    }

    @Test
    @DisplayName("ForgotPassword T05: Confirm code with invalid code")
    void confirmCode_invalidCode() {
        ConfirmCode request = new ConfirmCode();
        request.setCode("INVALID123");

        BaseException exception = assertThrows(BaseException.class,
                () -> forgotPasswordService.confirmCode(request));

        assertEquals("Mã không hợp lệ!", exception.getMessage());
        verifyNoInteractions(publisherEvent, passwordEncoder);
    }

    @Test
    @DisplayName("ForgotPassword T06: Confirm code with expired code")
    void confirmCode_expiredCode() {
        ForgotPasswordEntity forgotPasswordEntity = ForgotPasswordEntity.builder()
                .code("EXPIRED123")
                .accountCreate(accountEntity)
                .createAt(LocalDateTime.now().minusMinutes(10))
                .build();
        forgotPasswordRepository.save(forgotPasswordEntity);

        ConfirmCode request = new ConfirmCode();
        request.setCode("EXPIRED123");

        BaseException exception = assertThrows(BaseException.class,
                () -> forgotPasswordService.confirmCode(request));

        assertEquals("Mã đã hết hạn.", exception.getMessage());
        verifyNoInteractions(publisherEvent, passwordEncoder);
    }
}
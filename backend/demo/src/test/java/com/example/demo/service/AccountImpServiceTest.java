package com.example.demo.service;


import com.example.demo.entity.AccountEntity;
import com.example.demo.entity.EmployeeEntity;
import com.example.demo.entity.enums.Role;
import com.example.demo.entity.enums.TypeEmployee;
import com.example.demo.exception.CreateAccountException;
import com.example.demo.exception.EntityNotFoundException;
import com.example.demo.jwt.JwtService;
import com.example.demo.model.request.AccountRequest;
import com.example.demo.model.request.LoginRequest;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.EmployeeRepository;
import com.example.demo.security.CustomUserDetail;
import com.example.demo.service.impl.AccountImplService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountImplServiceTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AccountImplService accountService;

    private AccountEntity testAccount;
    private EmployeeEntity testEmployee;

    @BeforeEach
    void setUp() {
        // Guessing TypeEmployee might have MANAGER - REPLACE with actual value
        testEmployee = EmployeeEntity.builder()
                .name("Test Employee")
                .type(TypeEmployee.valueOf("MANAGER"))  // Adjust based on actual TypeEmployee enum
                .build();
        testEmployee.setId(1L);

        testAccount = AccountEntity.builder()
                .email("test@example.com")
                .userName("testuser")
                .password("encodedPassword")
                .role(Role.MANAGER)  // Using actual Role enum value
                .employee(testEmployee)
                .build();
        testAccount.setId(1L);
        testAccount.setIsActive(true);
    }

    @Test
    void getAll_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<AccountEntity> page = new PageImpl<>(Collections.singletonList(testAccount));
        when(accountRepository.findAll(pageable)).thenReturn(page);

        var response = accountService.getAll(0, 10);

        assertEquals(200, response.getStatusCode());
        assertEquals("Nhận tất cả tài khoản thành công!", response.getMessage());
        assertNotNull(response.getData());
        verify(accountRepository).findAll(pageable);
    }

    @Test
    void create_Success() {
        AccountRequest request = new AccountRequest();
        request.setEmail("new@example.com");
        request.setUsername("newuser");
        request.setPassword("password");
        request.setRole("MANAGER");  // Using actual Role enum value
        request.setEmployeeId(1L);

        when(accountRepository.existsByEmailAndIsActive(any())).thenReturn(false);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(accountRepository.existsByEmployeeAndIsActive(any(), eq(true))).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        when(accountRepository.save(any(AccountEntity.class))).thenReturn(testAccount);

        var response = accountService.create(request);

        assertEquals(201, response.getStatusCode());
        assertEquals("Đã tạo tài khoản thành công.", response.getMessage());
        verify(accountRepository).save(any(AccountEntity.class));
    }

    @Test
    void create_EmailAlreadyExists_ThrowsException() {
        AccountRequest request = new AccountRequest();
        request.setEmail("test@example.com");

        when(accountRepository.existsByEmailAndIsActive("test@example.com")).thenReturn(true);

        assertThrows(CreateAccountException.class, () -> accountService.create(request));
    }

    @Test
    void login_Success() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password");

        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(jwtService.generateToken(any())).thenReturn("token");

        var response = accountService.login(request);

        assertEquals(200, response.getStatusCode());
        assertEquals("Đăng nhập thành công.", response.getMessage());
        assertNotNull(response.getData());
    }

    @Test
    void update_Success() {
        AccountRequest request = new AccountRequest();
        request.setEmail("updated@example.com");
        request.setUsername("updateduser");
        request.setRole("MANAGER");  // Using actual Role enum value

        CustomUserDetail userDetail = mock(CustomUserDetail.class);
        when(userDetail.getAccount()).thenReturn(testAccount);
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetail, null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(accountRepository.existsByEmailAndIsActive(any())).thenReturn(false);
        when(accountRepository.save(any())).thenReturn(testAccount);

        var response = accountService.update(request, 1L);

        assertEquals(200, response.getStatusCode());
        assertEquals("Cập nhật tài khoản thành công.", response.getMessage());
    }

    @Test
    void deleteById_Success() {
        when(accountRepository.getAccountById(1L)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any())).thenReturn(testAccount);

        var response = accountService.deleteById(1L);

        assertEquals(200, response.getStatusCode());
        assertEquals("Xóa tài khoản thành công.", response.getMessage());
        assertFalse(testAccount.getIsActive());
    }

    @Test
    void getById_Success() {
        when(accountRepository.getAccountById(1L)).thenReturn(Optional.of(testAccount));

        var response = accountService.getById(1L);

        assertEquals(200, response.getStatusCode());
        assertEquals("Nhận tài khoản theo id thành công.", response.getMessage());
        assertNotNull(response.getData());
    }

    @Test
    void getById_NotFound_ThrowsException() {
        when(accountRepository.getAccountById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> accountService.getById(1L));
    }
}
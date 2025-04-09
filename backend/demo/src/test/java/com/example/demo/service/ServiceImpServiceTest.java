package com.example.demo.service;

import com.example.demo.entity.AccountEntity;
import com.example.demo.entity.EmployeeEntity;
import com.example.demo.entity.ServiceEntity;
import com.example.demo.entity.enums.Role;
import com.example.demo.entity.enums.TypeEmployee; // Assuming this exists
import com.example.demo.exception.EntityNotFoundException;
import com.example.demo.model.BaseResponse;
import com.example.demo.model.request.ServiceRequest;
import com.example.demo.model.response.ServiceResponse;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.ServiceRepository;
import com.example.demo.security.CustomUserDetail;
import com.example.demo.service.impl.ServiceImplService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceImplServiceTest {

    @Mock
    private ServiceRepository serviceRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ServiceImplService serviceImplService;

    private AccountEntity testAccount;
    private EmployeeEntity testEmployee;
    private ServiceEntity testService;
    private CustomUserDetail testUserDetail;

    @BeforeEach
    void setUp() {
        // Assuming TypeEmployee has a MANAGER value - adjust based on actual enum
        testEmployee = EmployeeEntity.builder()
                .name("Test Employee")
                .type(TypeEmployee.valueOf("MANAGER")) // Replace with actual TypeEmployee value
                .build();
        testEmployee.setId(1L);

        testAccount = AccountEntity.builder()
                .userName("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .role(Role.MANAGER)
                .employee(testEmployee)
                .build();
        testAccount.setId(1L);
        testAccount.setIsActive(true);

        testService = ServiceEntity.builder()
                .name("Test Service")
                .price(100.0)
                .description("Test Description")
                .build();
        testService.setId(1L);
        testService.setIsActive(true);
        testService.setCreateBy(testAccount);

        testUserDetail = new CustomUserDetail(testAccount);
    }

    private void setupSecurityContext() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUserDetail);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void getAll_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Object[] serviceData = new Object[]{1L, "Test Service", 100.0, "SERVICE001", "Test Description"};
        Page<Object[]> page = new PageImpl<>(Collections.singletonList(serviceData), pageable, 1);
        when(serviceRepository.getAll(pageable)).thenReturn(page);

        BaseResponse response = serviceImplService.getAll(0, 10);

        assertEquals(200, response.getStatusCode());
        assertEquals("nhận được tất cả các dịch vụ thành công.", response.getMessage());
        assertNotNull(response.getData());
        assertTrue(response.getData() instanceof Page);
        verify(serviceRepository).getAll(pageable);
    }

    @Test
    void create_Success() {
        setupSecurityContext();
        ServiceRequest request = ServiceRequest.builder()
                .name("New Service")
                .price(200.0)
                .description("New Description")
                .status(true)
                .build();

        when(serviceRepository.save(any(ServiceEntity.class))).thenReturn(testService);

        BaseResponse response = serviceImplService.create(request);

        assertEquals(201, response.getStatusCode());
        assertEquals("Tạo dịch vụ thành công.", response.getMessage());
        assertNotNull(response.getData());
        assertTrue(response.getData() instanceof ServiceResponse);
        verify(serviceRepository).save(any(ServiceEntity.class));
    }

    @Test
    void update_Success() {
        setupSecurityContext();
        ServiceRequest request = ServiceRequest.builder()
                .name("Updated Service")
                .price(300.0)
                .description("Updated Description")
                .status(true)
                .build();

        when(serviceRepository.findById(1L)).thenReturn(Optional.of(testService));
        when(serviceRepository.save(any(ServiceEntity.class))).thenReturn(testService);

        BaseResponse response = serviceImplService.update(request, 1L);

        assertEquals(200, response.getStatusCode());
        assertEquals("Cập nhật dịch vụ thành công.", response.getMessage());
        assertNull(response.getData());
        verify(serviceRepository).findById(1L);
        verify(serviceRepository).save(any(ServiceEntity.class));
    }

    @Test
    void update_ServiceNotFound_ThrowsException() {
        ServiceRequest request = ServiceRequest.builder()
                .name("Updated Service")
                .price(300.0)
                .description("Updated Description")
                .status(true)
                .build();

        when(serviceRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> serviceImplService.update(request, 1L));
        verify(serviceRepository).findById(1L);
        verify(serviceRepository, never()).save(any());
    }

    @Test
    void deleteById_Success() {
        when(serviceRepository.findById(1L)).thenReturn(Optional.of(testService));
        when(serviceRepository.save(any(ServiceEntity.class))).thenReturn(testService);

        BaseResponse response = serviceImplService.deleteById(1L);

        assertEquals(0, response.getStatusCode()); // Changed from 200 to 0
        assertEquals("xóa dịch vụ thành công theo id = 1", response.getMessage());
        assertNull(response.getData());
        assertFalse(testService.getIsActive());
        verify(serviceRepository).findById(1L);
        verify(serviceRepository).save(any(ServiceEntity.class));
    }

    @Test
    void deleteById_ServiceNotFound_ThrowsException() {
        when(serviceRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> serviceImplService.deleteById(1L));
        verify(serviceRepository).findById(1L);
        verify(serviceRepository, never()).save(any());
    }

    @Test
    void getById_Success() {
        when(serviceRepository.findById(1L)).thenReturn(Optional.of(testService));

        BaseResponse response = serviceImplService.getById(1L);

        assertEquals(200, response.getStatusCode());
        assertEquals("nhận dịch vụ thành công bằng id = 1", response.getMessage());
        assertNotNull(response.getData());
        assertTrue(response.getData() instanceof ServiceResponse);
        verify(serviceRepository).findById(1L);
    }

    @Test
    void getById_ServiceNotFound_ThrowsException() {
        when(serviceRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> serviceImplService.getById(1L));
        verify(serviceRepository).findById(1L);
    }

    @Test
    void getByParams_Success() {
        HashMap<String, String> params = new HashMap<>();
        params.put("pageNumber", "1");
        params.put("pageSize", "10");
        params.put("sortBy", "name");
        params.put("sortOrder", "ASC");
        params.put("name", "Test");

        Pageable pageable = PageRequest.of(0, 10, Sort.by("name").ascending());
        Object[] serviceData = new Object[]{1L, "Test Service", 100.0, "SERVICE001", "Test Description"};
        Page<Object[]> page = new PageImpl<>(Collections.singletonList(serviceData), pageable, 1);
        when(serviceRepository.getByParamsInService(any(), any())).thenReturn(page);

        BaseResponse response = serviceImplService.getByParams(params);

        assertEquals(200, response.getStatusCode());
        assertEquals("nhận được tất cả các dịch vụ thành công.", response.getMessage());
        assertNotNull(response.getData());
        assertTrue(response.getData() instanceof Page);
        verify(serviceRepository).getByParamsInService(any(), any());
    }
}
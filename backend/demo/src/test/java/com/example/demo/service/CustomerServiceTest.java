package com.example.demo.service;

import com.example.demo.entity.AccountEntity;
import com.example.demo.entity.CustomerEntity;
import com.example.demo.entity.enums.Role;
import com.example.demo.exception.EntityNotFoundException;
import com.example.demo.model.BaseResponse;
import com.example.demo.model.request.CustomerRequest;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.CustomerRepository;
import com.example.demo.service.impl.CustomerImplService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@DataJpaTest
@Import(CustomerImplService.class)
@Sql(value = "/sql/import_customer.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = "/sql/import_customer_clean_up.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class CustomerServiceTest /* extends AbstractBaseTest */ {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CustomerImplService customerService;

    @MockBean
    private StoreService storeService; // Mock the dependency

    private CustomerEntity customerEntity;
    private CustomerRequest customerRequest;
    private AccountEntity testAccount;

    @BeforeEach
    void setUp() {
        testAccount = AccountEntity.builder()
                .userName("testUser")
                .email("test@example.com")
                .password("encodedOldPassword")
                .role(Role.DISPATCHER)
                .build();
        testAccount.setId(1L);
        testAccount.setIsActive(true);
        accountRepository.save(testAccount);

        customerEntity = CustomerEntity.builder()
                .name("John Doe")
                .phone("1234567890")
                .email("john@example.com")
                .address("123 Street")
                .build();
        customerEntity.setId(1L);
        customerEntity.setIsActive(true);
        customerEntity.setCreateBy(testAccount);
        customerEntity.setModifyBy(testAccount);
        customerEntity.prePersist();
        customerEntity.postPersist();

        customerRequest = new CustomerRequest();
        customerRequest.setName("John Doe");
        customerRequest.setPhone("1234567890");
        customerRequest.setEmail("john@example.com");
        customerRequest.setAddress("123 Street");
    }

    @Test
    @DisplayName("Customer T01: Get all customers successfully")
    void getAll_Success() {
        customerRepository.save(customerEntity);

        BaseResponse response = customerService.getAll(0, 10);

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertEquals("Nhận tất cả danh sách khách hàng thành công!", response.getMessage());
        assertNotNull(response.getData());
    }

//    @Test
//    @DisplayName("Customer T02: Create customer successfully")
//    void create_Success() {
//        BaseResponse response = customerService.create(customerRequest);
//
//        assertEquals(HttpStatus.CREATED.value(), response.getStatusCode());
//        assertEquals("Tạo khách hàng thành công.", response.getMessage());
//        assertNotNull(response.getData());
//
//        Optional<CustomerEntity> savedCustomer = customerRepository.findByEmail("john@example.com");
//        assertTrue(savedCustomer.isPresent());
//        assertEquals("John Doe", savedCustomer.get().getName());
//        assertEquals("KH000001", savedCustomer.get().getCode());
//        assertNotNull(savedCustomer.get().getCreateDate());
//        assertNotNull(savedCustomer.get().getModifyDate());
//        assertTrue(savedCustomer.get().getIsActive());
//    }

    @Test
    @DisplayName("Customer T03: Create customer with existing phone")
    void create_PhoneExists_Failure() {
        customerRepository.save(customerEntity);

        BaseResponse response = customerService.create(customerRequest);

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());
        assertEquals("Số điện thoại đã tồn tại trong hệ thống.", response.getMessage());
        assertNull(response.getData());
    }

    @Test
    @DisplayName("Customer T05: Update customer successfully")
    void update_Success() {
        customerRepository.save(customerEntity);
        customerRequest.setName("John Updated");

        BaseResponse response = customerService.update(customerRequest, 1L);

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertEquals("Cập nhật khách hàng thành công.", response.getMessage());
        assertNotNull(response.getData());

        Optional<CustomerEntity> updatedCustomer = customerRepository.findById(1L);
        assertTrue(updatedCustomer.isPresent());
        assertEquals("John Updated", updatedCustomer.get().getName());
        assertEquals("KH000001", updatedCustomer.get().getCode());
        assertTrue(updatedCustomer.get().getModifyDate().isAfter(updatedCustomer.get().getCreateDate()));
    }

    @Test
    @DisplayName("Customer T06: Update non-existing customer")
    void update_CustomerNotFound_Failure() {
        BaseResponse response = customerService.update(customerRequest, 999L);

        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode());
        assertTrue(response.getMessage().contains("Customer"));
        assertNull(response.getData());
    }

    @Test
    @DisplayName("Customer T07: Delete customer successfully")
    void deleteById_Success() {
        customerRepository.save(customerEntity);

        BaseResponse response = customerService.deleteById(1L);

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertEquals("Xóa khách hàng thành công.", response.getMessage());

        Optional<CustomerEntity> deletedCustomer = customerRepository.findById(1L);
        assertTrue(deletedCustomer.isPresent());
        assertFalse(deletedCustomer.get().getIsActive());
    }

    @Test
    @DisplayName("Customer T08: Delete non-existing customer")
    void deleteById_NotFound_Failure() {
        assertThrows(EntityNotFoundException.class, () -> customerService.deleteById(999L));
    }

    @Test
    @DisplayName("Customer T09: Get customer by ID successfully")
    void getById_Success() {
        customerRepository.save(customerEntity);

        BaseResponse response = customerService.getById(1L);

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertEquals("Nhận thông tin chi tiết khách hàng thành công.", response.getMessage());
        assertNotNull(response.getData());
    }

    @Test
    @DisplayName("Customer T10: Get non-existing customer by ID")
    void getById_NotFound_Failure() {
        assertThrows(EntityNotFoundException.class, () -> customerService.getById(999L));
    }
}
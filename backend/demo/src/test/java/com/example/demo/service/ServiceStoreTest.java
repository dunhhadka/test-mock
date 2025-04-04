package com.example.demo.service;

import com.example.demo.entity.AccountEntity;
import com.example.demo.entity.ServiceEntity;
import com.example.demo.model.request.ServiceRequest;
import com.example.demo.model.response.ServiceResponse;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.ServiceRepository;
import com.example.demo.service.impl.ServiceImplService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, SpringExtension.class})
public class ServiceStoreTest {

    @Mock
    private ServiceRepository serviceRepository;

    @Mock
    private AccountRepository accountRepository;

    ServiceImplService service;

    @BeforeEach
    public void setUp() {
        service = new ServiceImplService(serviceRepository, accountRepository);
    }

    @Test
    void test_get_by_id() {
        var mockEntity = ServiceEntity.builder()
                .name("service1")
                .price(50.000)
                .build();
        mockEntity.setCreateBy(AccountEntity.builder().build());
        when(this.serviceRepository.findById(anyLong())).thenReturn(Optional.of(mockEntity));

        var response = service.getById(1L);

        var service = (ServiceResponse) response.getData();
        Assertions.assertEquals("service1", service.getName());
        Assertions.assertEquals(50.000, service.getPrice());
    }
}

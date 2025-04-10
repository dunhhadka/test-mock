package com.example.demo.service;

import com.example.demo.entity.StoreEntity;
import com.example.demo.model.BaseResponse;
import com.example.demo.model.request.StoreRequest;
import com.example.demo.model.response.StoreResponse;
import com.example.demo.repository.StoreRepository;
import com.example.demo.service.impl.StoreImplService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StoreServiceTest {

    private StoreRepository storeRepository;
    private StoreImplService storeService;

    @BeforeEach
    void setUp() {
        storeRepository = mock(StoreRepository.class);
        storeService = new StoreImplService(storeRepository);
    }

    // TEST 01 - Create mới khi chưa có store nào
    @Test
    void test01_create_WhenStoreListIsEmpty_ShouldCreateNewStore() {
        StoreRequest request = StoreRequest.builder()
                .name("Store A")
                .phone("123456789")
                .address("123 ABC Street")
                .email("store@example.com")
                .vat(10.0)
                .build();

        when(storeRepository.findAll()).thenReturn(Collections.emptyList());

        BaseResponse response = storeService.create(request);

        ArgumentCaptor<StoreEntity> captor = ArgumentCaptor.forClass(StoreEntity.class);
        verify(storeRepository).save(captor.capture());

        StoreEntity savedStore = captor.getValue();

        assertEquals("Store A", savedStore.getName());
        assertEquals("123456789", savedStore.getPhone());
        assertEquals("123 ABC Street", savedStore.getAddress());
        assertEquals("store@example.com", savedStore.getEmail());
        assertEquals(10.0, savedStore.getVat());
        assertTrue(savedStore.getIsActive());
        assertEquals(200, response.getStatusCode());
        assertEquals("Tạo hoặc cập nhật cửa hàng thành công.", response.getMessage());
    }

    // TEST 02 - Update store đã tồn tại
    @Test
    void test02_create_WhenStoreExists_ShouldUpdateStore() {
        StoreEntity existingStore = StoreEntity.builder()
                .name("Old Store")
                .phone("000000000")
                .address("Old Address")
                .email("old@example.com")
                .vat(5.0)
                .build();

        when(storeRepository.findAll()).thenReturn(Collections.singletonList(existingStore));
        when(storeRepository.findById(1L)).thenReturn(Optional.of(existingStore));

        StoreRequest request = StoreRequest.builder()
                .name("New Store")
                .phone("111111111")
                .address("New Address")
                .email("new@example.com")
                .vat(8.0)
                .build();

        BaseResponse response = storeService.create(request);

        ArgumentCaptor<StoreEntity> captor = ArgumentCaptor.forClass(StoreEntity.class);
        verify(storeRepository).save(captor.capture());

        StoreEntity savedStore = captor.getValue();

        assertEquals("New Store", savedStore.getName());
        assertEquals("111111111", savedStore.getPhone());
        assertEquals("New Address", savedStore.getAddress());
        assertEquals("new@example.com", savedStore.getEmail());
        assertEquals(8.0, savedStore.getVat());
        assertTrue(savedStore.getIsActive());
        assertEquals(200, response.getStatusCode());
        assertEquals("Tạo hoặc cập nhật cửa hàng thành công.", response.getMessage());
    }

    // TEST 03 - getByParams trả về đúng thông tin
    @Test
    void test03_getByParams_ShouldReturnStoreResponse() {
        StoreEntity existingStore = StoreEntity.builder()
                .name("Test Store")
                .phone("999999999")
                .address("Test Address")
                .email("test@example.com")
                .vat(12.5)
                .build();

        when(storeRepository.findById(1L)).thenReturn(Optional.of(existingStore));

        BaseResponse response = storeService.getByParams(Map.of());

        assertEquals(200, response.getStatusCode());
        assertEquals("Tạo hoặc cập nhật cửa hàng thành công.", response.getMessage());

        StoreResponse data = (StoreResponse) response.getData();
        assertNotNull(data);
        assertEquals("Test Store", data.getName());
        assertEquals("999999999", data.getPhone());
        assertEquals("Test Address", data.getAddress());
        assertEquals("test@example.com", data.getEmail());
        assertEquals(12.5, data.getVat());
    }

    // TEST 04 - getByParams khi không tìm thấy store → throw exception
    @Test
    void test04_getByParams_WhenStoreNotFound_ShouldThrowException() {
        when(storeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(java.util.NoSuchElementException.class, () -> {
            storeService.getByParams(Map.of());
        });
    }

    // TEST 05 - create() khi store tồn tại nhưng findById(1L) không có → throw exception
    @Test
    void test05_create_WhenStoreExistsButFindByIdEmpty_ShouldThrowException() {
        when(storeRepository.findAll()).thenReturn(Collections.singletonList(new StoreEntity()));
        when(storeRepository.findById(1L)).thenReturn(Optional.empty());

        StoreRequest request = StoreRequest.builder()
                .name("Fail Store")
                .phone("000000000")
                .address("Nowhere")
                .email("fail@example.com")
                .vat(1.0)
                .build();

        assertThrows(java.util.NoSuchElementException.class, () -> {
            storeService.create(request);
        });
    }
}

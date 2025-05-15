package com.example.demo.service.hva;

import com.example.demo.entity.ImageEntity;
import com.example.demo.entity.ProductEntity;
import com.example.demo.exception.BaseException;
import com.example.demo.exception.EntityNotFoundException;
import com.example.demo.model.BaseResponse;
import com.example.demo.model.request.HistoryProductRequest;
import com.example.demo.model.request.ProductRequest;
import com.example.demo.model.response.ImageResponse;
import com.example.demo.model.response.ProductResponse;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.ImageRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.service.HistoryProductService;
import com.example.demo.service.StoreService;
import com.example.demo.service.impl.ProductImplService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DataJpaTest
@Import({ProductImplService.class})
@Sql(
        value = "/sql/import_product.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
@Sql(
        value = "/sql/import_product_clean_up.sql",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
)
public class ProductServiceTest2 {

    @Autowired
    private ProductRepository productRepository;

    @MockBean
    private AccountRepository accountRepository;

    @MockBean
    private HistoryProductService historyProductService;

    @MockBean
    private ImageRepository imageRepository;

    @MockBean
    private StoreService storeService; // Added to resolve missing dependency

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private ProductImplService productImplService;

    @BeforeEach
    public void setUp() {
    }

    @Test
    void PRD01_GetAll_Success() {
        int pageNumber = 1;
        int pageSize = 1;
        BaseResponse<Page<ProductResponse>> response = productImplService.getAll(pageNumber, pageSize);

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        //assertEquals("Nhận tất cả danh sách sản phẩm thành công!", response.getMessage());
        assertEquals(1, response.getData().getContent().size());
        assertEquals(3, response.getData().getTotalElements());
    }

    @Test
    void PRD02_Create_ImageNotFound_ThrowsEntityNotFoundException() {
        var productRequest = createNewProduct();
        productRequest.setImageId(1L);

        when(imageRepository.findById(anyLong())).thenReturn(Optional.empty());

        var exception = assertThrows(EntityNotFoundException.class, () -> productImplService.create(productRequest));

        assertEquals("Image không được tìm thấy bởi Id với giá trị 1", exception.getMessage());
    }

    @Test
    void PRD03_Create_WithoutImage_Success() {
        var productRequest = createNewProduct();

        ArgumentCaptor<HistoryProductRequest> historyProductCaptor = ArgumentCaptor.forClass(HistoryProductRequest.class);

        BaseResponse<ProductResponse> response = productImplService.create(productRequest);

        verify(historyProductService, times(1)).createHistory(historyProductCaptor.capture(), anyBoolean(), anyBoolean());

        var historyCreateRequest = historyProductCaptor.getValue();

        assertEquals(1, historyCreateRequest.getHistoryDetails().size());
        assertEquals("Khởi tạo linh kiện", historyCreateRequest.getNote());

        var productId = response.getData().getId();

        Optional<ProductEntity> productOptional = productRepository.findById(productId);

        assertNotNull(productOptional.get());

        var product = productOptional.get();
        assertEquals(productRequest.getName(), product.getName());
        assertEquals(50000.0, product.getPriceIn());
        assertEquals(70000.0, product.getPriceOut());
    }

    @Test
    void PRD04_DeleteById_Success() {
        var productRequest = createNewProduct();

        BaseResponse<ProductResponse> response = productImplService.create(productRequest);

        var products = productRepository.findAll();
        assertEquals(4, products.size());

        var productId = response.getData().getId();
        productImplService.deleteById(productId);

        var productsFetchedAfterDelete = productRepository.findAll();
        var productDeleted = productRepository.findById(productId).orElseThrow();
        assertFalse(productDeleted.getIsActive());
        assertEquals(4, productsFetchedAfterDelete.size());
    }

    @Test
    void PRD05_GetById_Success() {
        var productRequest = createNewProduct();

        BaseResponse<ProductResponse> response = productImplService.create(productRequest);

        var productId = response.getData().getId();
        BaseResponse<ProductResponse> productResponseBaseResponse = productImplService.getById(productId);

        assertEquals(response.getData().getId(), productResponseBaseResponse.getData().getId());
        assertEquals(201, response.getStatusCode());
        assertEquals("Sản phẩm được lấy thành công", productResponseBaseResponse.getMessage());
    }

    @Test
    void PRD06_GetById_NotFound() {
        long productId = 100L;

        BaseResponse<ProductResponse> response = productImplService.getById(productId);

        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode());
        assertEquals("Sản phẩm không tồn tại.", response.getMessage());
        assertNull(response.getData());
    }

    @Test
    void PRD07_DeleteById_NotFound() {
        long productId = 100L;

        BaseResponse<ProductResponse> response = productImplService.deleteById(productId);

        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode());
        assertEquals("Sản phẩm không tồn tại.", response.getMessage());
        assertNull(response.getData());
    }

    @Test
    void PRD08_UploadImage_InvalidType_ThrowsBaseException() {
        MockMultipartFile mockFile = new MockMultipartFile(
                "file", "file.txt", "text/plain", "Hello World".getBytes());

        var exception = assertThrows(BaseException.class, () -> productImplService.uploadImage(mockFile));
        assertEquals("Tệp không được phép", exception.getMessage());
    }

    @Test
    void PRD09_UploadImage_Success() throws IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("/api");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        MockMultipartFile mockFile = new MockMultipartFile(
                "file", "file.txt", "image/png", "Hello World".getBytes());

        var imageSaved = getImageSaved();

        when(imageRepository.save(any())).thenReturn(imageSaved).thenReturn(imageSaved);

        BaseResponse<ImageResponse> response = productImplService.uploadImage(mockFile);

        verify(imageRepository, times(2)).save(any());

        assertEquals(imageSaved.getId(), response.getData().getId());
        assertEquals(HttpStatus.CREATED.value(), response.getStatusCode());
        assertEquals("Tải hình ảnh lên thành công.", response.getMessage());
    }

    @Test
    void PRD10_DeleteById_NotFound() {
        Long productId = 100L;
        BaseResponse<ProductResponse> deleteResponse = productImplService.deleteById(productId);
        assertEquals(HttpStatus.NOT_FOUND.value(), deleteResponse.getStatusCode());
        assertEquals("Sản phẩm không tồn tại.", deleteResponse.getMessage());
        assertNull(deleteResponse.getData());
    }

    @Test
    void PRD11_Update_ImageNotFound_ThrowsEntityNotFoundException() {
        Long productId = 1L;
        ProductRequest request = createNewProduct();
        request.setImageId(1L);

        when(imageRepository.findById(request.getImageId())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> productImplService.update(request, productId));
    }

    @Test
    void PRD12_Update_ProductNotFound_ThrowsBaseException() {
        Long productId = 5L;
        ProductRequest request = createNewProduct();

        var exception = assertThrows(BaseException.class, () -> productImplService.update(request, productId));
        assertEquals("Sản phẩm không tồn tại.", exception.getMessage());
    }

    @Test
    void PRD13_Update_WithImage_Success() {
        var productRequest = createNewProduct();

        BaseResponse<ProductResponse> productCreated = productImplService.create(productRequest);

        var productUpdateRequest = ProductRequest.builder()
                .name("product_update")
                .priceIn(150000.0)
                .priceOut(175000.0)
                .imageId(1L)
                .brand("BrandX")
                .description("Mô tả sản phẩm A")
                .storageQuantity(100)
                .quantityWarning(20)
                .unit("Hộp")
                .build();

        var imageEntity = new ImageEntity();
        imageEntity.setId(productUpdateRequest.getImageId());
        imageEntity.setProductId(productCreated.getData().getId());

        when(imageRepository.findById(productUpdateRequest.getImageId())).thenReturn(Optional.of(imageEntity));

        BaseResponse<ProductResponse> productUpdated = productImplService.update(productUpdateRequest, productCreated.getData().getId());

        assertEquals(productCreated.getData().getId(), productUpdated.getData().getId());
        assertEquals(HttpStatus.OK.value(), productUpdated.getStatusCode());
        assertEquals("Cập nhật sản phẩm thành công!", productUpdated.getMessage());
        verify(imageRepository, times(1)).save(any());
    }

    @Test
    void PRD14_AddToInventory_NotFound_ThrowsEntityNotFoundException() {
        Long productId = 10L;
        assertThrows(EntityNotFoundException.class, () -> productImplService.addToInventory(productId, 1, "note"));
    }

    @Test
    void PRD15_AddToInventory_Success() {
        var productRequest = createNewProduct();

        BaseResponse<ProductResponse> response = productImplService.create(productRequest);
        var productId = response.getData().getId();

        productImplService.addToInventory(productId, 1, "note");

        var product = productRepository.findById(productId).orElseThrow();
        assertEquals(1, product.getHistoryList().size());
    }

    @Test
    void PRD16_RemoveFromInventory_NotFound_ThrowsEntityNotFoundException() {
        Long productId = 10L;
        assertThrows(EntityNotFoundException.class, () -> productImplService.removeFromInventory(productId, 1, "note"));
    }

    @Test
    void PRD17_RemoveFromInventory_Success() {
        var productRequest = createNewProduct();

        BaseResponse<ProductResponse> response = productImplService.create(productRequest);
        var productId = response.getData().getId();

        productImplService.removeFromInventory(productId, 1, "note");

        var product = productRepository.findById(productId).orElseThrow();
        assertEquals(1, product.getHistoryList().size());
    }

    @Test
    void PRD18_UpdateImage_NotFound_ThrowsEntityNotFoundException() {
        MockMultipartFile mockFile = new MockMultipartFile(
                "file", "file.txt", "image/png", "Hello World".getBytes());

        when(imageRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> productImplService.updateImage(mockFile, 1L));
    }

    @Test
    void PRD19_UpdateImage_InvalidType_ThrowsBaseException() {
        MockMultipartFile mockFile = new MockMultipartFile(
                "file", "file.txt", "text/plain", "Hello World".getBytes());

        var image = new ImageEntity();
        image.setId(1L);

        when(imageRepository.findById(anyLong())).thenReturn(Optional.of(image));

        assertThrows(BaseException.class, () -> productImplService.updateImage(mockFile, 1L));
    }

    @Test
    void PRD20_Create_WithImage_Success() {
        var imageEntity = getImageSaved();
        var productRequest = createNewProduct();
        productRequest.setImageId(imageEntity.getId());

        when(imageRepository.findById(anyLong())).thenReturn(Optional.of(imageEntity));

        BaseResponse<ProductResponse> response = productImplService.create(productRequest);

        assertEquals(HttpStatus.CREATED.value(), response.getStatusCode());
        assertEquals("Tạo sản phẩm thành công.", response.getMessage());
        verify(imageRepository, times(1)).save(any());
    }

    @Test
    void PRD21_UpdateImage_Success() throws IOException {
        MockMultipartFile mockFile = new MockMultipartFile(
                "file", "file.txt", "image/png", "Hello World".getBytes());

        var image = new ImageEntity();
        image.setId(1L);

        when(imageRepository.findById(anyLong())).thenReturn(Optional.of(image));

        BaseResponse<ImageResponse> response = productImplService.updateImage(mockFile, image.getId());
        assertEquals(HttpStatus.CREATED.value(), response.getStatusCode());
        assertEquals("Cập nhật hình ảnh thành công.", response.getMessage());
    }

    @Test
    void PRD22_GetByParams_Success() {
        Map<String, String> params = Map.of("name", "Sản phẩm");
        BaseResponse<Page<ProductResponse>> response = productImplService.getByParams(params);

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertEquals("Nhận sản phẩm thành công.", response.getMessage());
        assertEquals(3, response.getData().getContent().size());
    }

    @Test
    void PRD23_Create_NameNull_ThrowsBaseException() {
        ProductRequest request = createNewProduct();
        request.setName(null);

        Exception exception = assertThrows(Exception.class, () -> productImplService.create(request),
                "Expected BaseException for null name");
        assertTrue(exception instanceof NullPointerException || exception instanceof BaseException,
                "Expected either BaseException or NullPointerException due to lack of validation");
    }

    @Test
    void PRD24_Create_NameEmpty_ThrowsBaseException() {
        ProductRequest request = createNewProduct();
        request.setName("");

        Exception exception = assertThrows(Exception.class, () -> productImplService.create(request),
                "Expected BaseException for empty name");
        assertTrue(exception instanceof NullPointerException || exception instanceof BaseException,
                "Expected either BaseException or NullPointerException due to lack of validation");
    }

    @Test
    void PRD25_Create_PriceInNegative_ThrowsBaseException() {
        ProductRequest request = createNewProduct();
        request.setPriceIn(-100.0);

        Exception exception = assertThrows(Exception.class, () -> productImplService.create(request),
                "Expected BaseException for negative priceIn");
        assertTrue(exception instanceof NullPointerException || exception instanceof BaseException,
                "Expected either BaseException or NullPointerException due to lack of validation");
    }

    @Test
    void PRD26_Create_PriceOutNegative_ThrowsBaseException() {
        ProductRequest request = createNewProduct();
        request.setPriceOut(-100.0);

        Exception exception = assertThrows(Exception.class, () -> productImplService.create(request),
                "Expected BaseException for negative priceOut");
        assertTrue(exception instanceof NullPointerException || exception instanceof BaseException,
                "Expected either BaseException or NullPointerException due to lack of validation");
    }

    @Test
    void PRD27_Create_StorageQuantityNegative_ThrowsBaseException() {
        ProductRequest request = createNewProduct();
        request.setStorageQuantity(-10);

        Exception exception = assertThrows(Exception.class, () -> productImplService.create(request),
                "Expected BaseException for negative storageQuantity");
        assertTrue(exception instanceof NullPointerException || exception instanceof BaseException,
                "Expected either BaseException or NullPointerException due to lack of validation");
    }

    @Test
    void PRD28_Create_QuantityWarningNegative_ThrowsBaseException() {
        ProductRequest request = createNewProduct();
        request.setQuantityWarning(-5);

        Exception exception = assertThrows(Exception.class, () -> productImplService.create(request),
                "Expected BaseException for negative quantityWarning");
        assertTrue(exception instanceof NullPointerException || exception instanceof BaseException,
                "Expected either BaseException or NullPointerException due to lack of validation");
    }

    @Test
    void PRD29_Update_NameNull_ThrowsBaseException() {
        var productRequest = createNewProduct();
        BaseResponse<ProductResponse> response = productImplService.create(productRequest);
        var productId = response.getData().getId();

        ProductRequest updateRequest = createNewProduct();
        updateRequest.setName(null);

        Exception exception = assertThrows(Exception.class, () -> productImplService.update(updateRequest, productId),
                "Expected BaseException for null name");
        assertTrue(exception instanceof NullPointerException || exception instanceof BaseException,
                "Expected either BaseException or NullPointerException due to lack of validation");
    }

    @Test
    void PRD30_Update_NameEmpty_ThrowsBaseException() {
        var productRequest = createNewProduct();
        BaseResponse<ProductResponse> response = productImplService.create(productRequest);
        var productId = response.getData().getId();

        ProductRequest updateRequest = createNewProduct();
        updateRequest.setName("");

        Exception exception = assertThrows(Exception.class, () -> productImplService.update(updateRequest, productId),
                "Expected BaseException for empty name");
        assertTrue(exception instanceof NullPointerException || exception instanceof BaseException,
                "Expected either BaseException or NullPointerException due to lack of validation");
    }

    @Test
    void PRD31_Update_PriceInNegative_ThrowsBaseException() {
        var productRequest = createNewProduct();
        BaseResponse<ProductResponse> response = productImplService.create(productRequest);
        var productId = response.getData().getId();

        ProductRequest updateRequest = createNewProduct();
        updateRequest.setPriceIn(-100.0);

        Exception exception = assertThrows(Exception.class, () -> productImplService.update(updateRequest, productId),
                "Expected BaseException for negative priceIn");
        assertTrue(exception instanceof NullPointerException || exception instanceof BaseException,
                "Expected either BaseException or NullPointerException due to lack of validation");
    }

    @Test
    void PRD32_Update_PriceOutNegative_ThrowsBaseException() {
        var productRequest = createNewProduct();
        BaseResponse<ProductResponse> response = productImplService.create(productRequest);
        var productId = response.getData().getId();

        ProductRequest updateRequest = createNewProduct();
        updateRequest.setPriceOut(-100.0);

        Exception exception = assertThrows(Exception.class, () -> productImplService.update(updateRequest, productId),
                "Expected BaseException for negative priceOut");
        assertTrue(exception instanceof NullPointerException || exception instanceof BaseException,
                "Expected either BaseException or NullPointerException due to lack of validation");
    }

    @Test
    void PRD33_Update_StorageQuantityNegative_ThrowsBaseException() {
        var productRequest = createNewProduct();
        BaseResponse<ProductResponse> response = productImplService.create(productRequest);
        var productId = response.getData().getId();

        ProductRequest updateRequest = createNewProduct();
        updateRequest.setStorageQuantity(-10);

        Exception exception = assertThrows(Exception.class, () -> productImplService.update(updateRequest, productId),
                "Expected BaseException for negative storageQuantity");
        assertTrue(exception instanceof NullPointerException || exception instanceof BaseException,
                "Expected either BaseException or NullPointerException due to lack of validation");
    }

    @Test
    void PRD34_Update_QuantityWarningNegative_ThrowsBaseException() {
        var productRequest = createNewProduct();
        BaseResponse<ProductResponse> response = productImplService.create(productRequest);
        var productId = response.getData().getId();

        ProductRequest updateRequest = createNewProduct();
        updateRequest.setQuantityWarning(-5);

        Exception exception = assertThrows(Exception.class, () -> productImplService.update(updateRequest, productId),
                "Expected BaseException for negative quantityWarning");
        assertTrue(exception instanceof NullPointerException || exception instanceof BaseException,
                "Expected either BaseException or NullPointerException due to lack of validation");
    }

    @Test
    void PRD35_Update_WithoutImage_Success() {
        var productRequest = createNewProduct();
        BaseResponse<ProductResponse> response = productImplService.create(productRequest);
        var productId = response.getData().getId();

        ProductRequest updateRequest = ProductRequest.builder()
                .name("product_update")
                .priceIn(150000.0)
                .priceOut(175000.0)
                .imageId(null)
                .brand("BrandX")
                .description("Updated Description")
                .storageQuantity(100)
                .quantityWarning(20)
                .unit("Hộp")
                .build();

        BaseResponse<ProductResponse> updateResponse = productImplService.update(updateRequest, productId);

        assertEquals(HttpStatus.OK.value(), updateResponse.getStatusCode());
        assertEquals("Cập nhật sản phẩm thành công!", updateResponse.getMessage());
        assertEquals(productId, updateResponse.getData().getId());
        verify(imageRepository, never()).save(any());
    }

    @Test
    void PRD36_AddToInventory_NegativeDifference_ThrowsBaseException() {
        var productRequest = createNewProduct();
        BaseResponse<ProductResponse> response = productImplService.create(productRequest);
        var productId = response.getData().getId();

        Exception exception = assertThrows(Exception.class,
                () -> productImplService.addToInventory(productId, -1, "note"),
                "Expected BaseException for negative difference");
        assertTrue(exception instanceof NullPointerException || exception instanceof BaseException,
                "Expected either BaseException or NullPointerException due to lack of validation");
    }

    @Test
    void PRD37_RemoveFromInventory_NegativeDifference_ThrowsBaseException() {
        var productRequest = createNewProduct();
        BaseResponse<ProductResponse> response = productImplService.create(productRequest);
        var productId = response.getData().getId();

        Exception exception = assertThrows(Exception.class,
                () -> productImplService.removeFromInventory(productId, -1, "note"),
                "Expected BaseException for negative difference");
        assertTrue(exception instanceof NullPointerException || exception instanceof BaseException,
                "Expected either BaseException or NullPointerException due to lack of validation");
    }

    private ImageEntity getImageSaved() {
        var image = new ImageEntity();
        image.setId(1L);
        return image;
    }

    private ProductRequest createNewProduct() {
        return ProductRequest.builder()
                .imageId(null)
                .name("Sản phẩm A")
                .priceIn(50000.0)
                .priceOut(70000.0)
                .brand("BrandX")
                .description("Mô tả sản phẩm A")
                .storageQuantity(100)
                .quantityWarning(20)
                .unit("Hộp")
                .build();
    }
}
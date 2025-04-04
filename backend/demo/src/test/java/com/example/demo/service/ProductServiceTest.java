package com.example.demo.service;

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
import com.example.demo.service.impl.ProductImplService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
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

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@Slf4j
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
public class ProductServiceTest extends AbstractBaseTest {

    @Autowired
    private ProductRepository productRepository;

    @Mock
    private AccountRepository accountRepository;

    @MockBean
    private HistoryProductService historyProductService;

    @MockBean
    private ImageRepository imageRepository;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private ProductImplService productImplService;

    @BeforeEach
    public void setUp() {

    }

    @Test
    @DisplayName("Product T01: Retrieve all products from database with existing data")
    public void get_all_product_test() {
        int pageNumber = 1;
        int pageSize = 1;
        BaseResponse<Page<ProductResponse>> response = productImplService.getAll(pageNumber, pageSize);

        Assertions.assertEquals(response.getStatusCode(), HttpStatus.OK.value());
        Assertions.assertEquals(1, response.getData().getContent().size());
        Assertions.assertEquals(3, response.getData().getTotalElements());
    }

    @Test
    @DisplayName("Product T02: Create a new product with throw image not found")
    public void create_product_with_image_not_found_test() {
        var productRequest = createNewProduct();
        productRequest.setImageId(1L);

        when(imageRepository.findById(anyLong())).thenReturn(Optional.empty());

        var exception = Assertions.assertThrows(EntityNotFoundException.class, () -> this.productImplService.create(productRequest));

        Assertions.assertEquals(
                String.format("%s không được tìm thấy bởi %s với giá trị %s", "Image", "Id", productRequest.getImageId()),
                exception.getMessage());
    }

    @Test
    @DisplayName("Product T03: Create a new product without image")
    public void create_product_without_image() {
        var productRequest = createNewProduct();

        ArgumentCaptor<HistoryProductRequest> historyProductCaptor = ArgumentCaptor.forClass(HistoryProductRequest.class);

        BaseResponse<ProductResponse> response = this.productImplService.create(productRequest);

        verify(historyProductService, times(1)).createHistory(historyProductCaptor.capture(), anyBoolean(), anyBoolean());

        var historyCreateRequest = historyProductCaptor.getValue();

        Assertions.assertEquals(1, historyCreateRequest.getHistoryDetails().size());
        Assertions.assertEquals("Khởi tạo linh kiện", historyCreateRequest.getNote());

        var productId = response.getData().getId();

        Optional<ProductEntity> productOptional = this.productRepository.findById(productId);

        Assertions.assertNotNull(productOptional.get());

        var product = productOptional.get();
        Assertions.assertEquals(productRequest.getName(), product.getName());
        Assertions.assertEquals(50000.0, product.getPriceIn());
        Assertions.assertEquals(70000.0, product.getPriceOut());
    }

    @Test
    @DisplayName("Product T04: Delete product by id")
    public void delete_product_by_id_test() {
        var productRequest = this.createNewProduct();

        BaseResponse<ProductResponse> response = this.productImplService.create(productRequest);

        var products = this.productRepository.findAll();
        Assertions.assertEquals(4, products.size());

        // delete
        var productId = response.getData().getId();
        this.productImplService.deleteById(productId);

        var productsFetchedAfterDelete = this.productRepository.findAll();
        var productDeleted = this.productRepository.findById(productId).orElseThrow();
        Assertions.assertFalse(productDeleted.getIsActive());
        Assertions.assertEquals(4, productsFetchedAfterDelete.size());
    }

    @Test
    @DisplayName("Product T05: Get product by id")
    public void get_product_by_id_test() {
        var productRequest = this.createNewProduct();

        BaseResponse<ProductResponse> response = this.productImplService.create(productRequest);

        // delete
        var productId = response.getData().getId();
        BaseResponse<ProductResponse> productResponseBaseResponse = this.productImplService.getById(productId);

        Assertions.assertEquals(response.getData().getId(), productResponseBaseResponse.getData().getId());
    }

    @Test
    @DisplayName("Product T06: Get product by id not found")
    public void get_product_by_id_not_found_test() {
        long productId = 100L;

        BaseResponse<ProductResponse> response = this.productImplService.getById(productId);

        Assertions.assertEquals("Sản phẩm không tồn tại.", response.getMessage());
    }

    @Test
    @DisplayName("Product T07: Delete product by id not found")
    public void delete_product_by_id_not_found_test() {
        long productId = 100L;

        BaseResponse<ProductResponse> response = this.productImplService.getById(productId);

        Assertions.assertEquals("Sản phẩm không tồn tại.", response.getMessage());
    }

    @Test
    @DisplayName("Product T08: Upload image and not allow")
    public void upload_image_for_product_and_not_allow() {
        // Tạo MockMultipartFile
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",                          // Tên trường (field name) của file
                "file.txt",                      // Tên file
                "text/plain",                    // Content Type (MIME type)
                "Hello World".getBytes()         // Nội dung file
        );

        var exception = Assertions.assertThrows(BaseException.class, () -> this.productImplService.uploadImage(mockFile));
        Assertions.assertEquals("Tệp không được phép", exception.getMessage());
    }

    @Test
    @DisplayName("Product T09: Upload image and success")
    public void upload_image_for_product_and_success() throws IOException {

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("/api");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        MockMultipartFile mockFile = new MockMultipartFile(
                "file",                          // Tên trường (field name) của file
                "file.txt",                      // Tên file
                "image/png",                    // Content Type (MIME type)
                "Hello World".getBytes()         // Nội dung file
        );

        var imageSaved = getImageSaved();

        when(imageRepository.save(any())).thenReturn(imageSaved).thenReturn(imageSaved);

        BaseResponse<ImageResponse> response = this.productImplService.uploadImage(mockFile);

        verify(imageRepository, times(2)).save(any());

        Assertions.assertEquals(imageSaved.getId(), response.getData().getId());
        Assertions.assertEquals("Tải hình ảnh lên thành công.", response.getMessage());
    }

    @Test
    @DisplayName("Delete product not exist")
    public void delete_product_not_exist() {
        Long productId = 100L;
        BaseResponse<ProductResponse> deleteResponse = this.productImplService.deleteById(productId);
        Assertions.assertEquals("Sản phẩm không tồn tại.", deleteResponse.getMessage());
        Assertions.assertEquals(HttpStatus.NOT_FOUND.value(), deleteResponse.getStatusCode());
        Assertions.assertNull(deleteResponse.getData());
    }

    @Test
    @DisplayName("Update product with image not found")
    public void update_product_with_image_not_found() {
        Long productId = 1L;
        ProductRequest request = createNewProduct();
        request.setImageId(1L);

        when(imageRepository.findById(request.getImageId())).thenReturn(Optional.empty());

        Assertions.assertThrows(EntityNotFoundException.class, () -> this.productImplService.update(request, productId));
    }

    @Test
    @DisplayName("Update product with product not exist")
    public void update_product_with_product_not_exist() {
        Long productId = 5L;
        ProductRequest request = createNewProduct();

        var exception = Assertions.assertThrows(BaseException.class, () -> this.productImplService.update(request, productId));
        Assertions.assertEquals("Sản phẩm không tồn tại.", exception.getMessage());
    }

    @Test
    @DisplayName("Update product success")
    public void update_product_success() {
        var productRequest = this.createNewProduct();

        BaseResponse<ProductResponse> productCreated = this.productImplService.create(productRequest);

        var productUpdateRequest = ProductRequest.builder()
                .name("product_update")
                .priceIn(150000.0)
                .priceOut(175000.0)
                .imageId(1L)
                .build();

        var imageEntity = new ImageEntity();
        imageEntity.setId(productUpdateRequest.getImageId());
        imageEntity.setProductId(productCreated.getData().getId());

        when(imageRepository.findById(productUpdateRequest.getImageId())).thenReturn(Optional.of(imageEntity));

        BaseResponse<ProductResponse> productUpdated = this.productImplService.update(productUpdateRequest, productCreated.getData().getId());

        Assertions.assertEquals(productCreated.getData().getId(), productUpdated.getData().getId());
        verify(imageRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Add to inventory with not exist")
    public void add_to_inventory_but_not_exist() {
        Long productId = 10L;
        Assertions.assertThrows(EntityNotFoundException.class, () -> this.productImplService.addToInventory(productId, 1, "note"));
    }

    @Test
    @DisplayName("Add to inventory successfully")
    public void add_to_inventory_success() {
        var productRequest = createNewProduct();

        BaseResponse<ProductResponse> response = this.productImplService.create(productRequest);
        var productId = response.getData().getId();

        this.productImplService.addToInventory(productId, 1, "note");

        var product = this.productRepository.findById(productId)
                .orElseThrow();

        Assertions.assertEquals(1, product.getHistoryList().size());
    }

    @Test
    @DisplayName("Remove from inventory with not exist")
    public void remove_from_inventory_but_not_exist() {
        Long productId = 10L;
        Assertions.assertThrows(EntityNotFoundException.class, () -> this.productImplService.removeFromInventory(productId, 1, "note"));
    }

    @Test
    @DisplayName("Remove from inventory successfully")
    public void remove_from_inventory_success() {
        var productRequest = createNewProduct();

        BaseResponse<ProductResponse> response = this.productImplService.create(productRequest);
        var productId = response.getData().getId();

        this.productImplService.removeFromInventory(productId, 1, "note");

        var product = this.productRepository.findById(productId)
                .orElseThrow();

        Assertions.assertEquals(1, product.getHistoryList().size());
    }

    @Test
    @DisplayName("Update image not exist")
    public void update_image_not_exist() {
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",                          // Tên trường (field name) của file
                "file.txt",                      // Tên file
                "image/png",                    // Content Type (MIME type)
                "Hello World".getBytes()         // Nội dung file
        );

        when(imageRepository.findById(anyLong())).thenReturn(Optional.empty());

        Assertions.assertThrows(EntityNotFoundException.class, () -> this.productImplService.updateImage(mockFile, 1L));
    }

    @Test
    @DisplayName("Update image not allow image type")
    public void update_image_not_allow_image_type() {
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",                          // Tên trường (field name) của file
                "file.txt",                      // Tên file
                "text/png",                    // Content Type (MIME type)
                "Hello World".getBytes()         // Nội dung file
        );

        var image = new ImageEntity();
        image.setId(1L);

        when(imageRepository.findById(anyLong())).thenReturn(Optional.of(image));

        Assertions.assertThrows(BaseException.class, () -> this.productImplService.updateImage(mockFile, 1L));
    }

    @Test
    @DisplayName("Create product with image")
    public void create_product_with_image() {
        var imageEntity = this.getImageSaved();
        var productRequest = this.createNewProduct();
        productRequest.setImageId(imageEntity.getId());

        when(imageRepository.findById(anyLong())).thenReturn(Optional.of(imageEntity));

        BaseResponse<ProductResponse> response = this.productImplService.create(productRequest);

        verify(imageRepository, times(1)).save(imageEntity);
    }

    @Test
    @DisplayName("Update image successfully")
    public void update_image_success() throws IOException {
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",                          // Tên trường (field name) của file
                "file.txt",                      // Tên file
                "image/png",                    // Content Type (MIME type)
                "Hello World".getBytes()         // Nội dung file
        );

        var image = new ImageEntity();
        image.setId(1L);

        when(imageRepository.findById(anyLong())).thenReturn(Optional.of(image));

        BaseResponse<ImageResponse> response = this.productImplService.updateImage(mockFile, image.getId());
        Assertions.assertEquals("Cập nhật hình ảnh thành công.", response.getMessage());
    }

    @Test
    @DisplayName("Get products by params")
    public void get_products_by_params() {
        Map<String, String> params = Map.of(
                "name", "Sản phẩm"
        );
        BaseResponse<Page<ProductResponse>> response = this.productImplService.getByParams(params);

        Assertions.assertEquals(3, response.getData().getContent().size());
    }

    private ImageEntity getImageSaved() {
        var image = new ImageEntity();
        image.setId(1L);
        return image;
    }


    private ProductRequest createNewProduct() {
        return ProductRequest.builder()
                .imageId(null) // ID của hình ảnh, nếu cần
                .name("Sản phẩm A")
                .priceIn(50000.0)  // Giá đầu vào
                .priceOut(70000.0) // Giá bán
                .brand("BrandX")
                .description("Mô tả sản phẩm A")
                .storageQuantity(100) // Số lượng lưu trữ
                .quantityWarning(20)  // Số lượng cảnh báo khi sắp hết hàng
                .unit("Hộp")
                .build();
    }
}

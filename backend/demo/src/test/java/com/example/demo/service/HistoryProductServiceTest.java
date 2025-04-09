package com.example.demo.service;

import com.example.demo.exception.BaseException;
import com.example.demo.exception.EntityNotFoundException;
import com.example.demo.model.BaseResponse;
import com.example.demo.model.request.HistoryDetail;
import com.example.demo.model.request.HistoryProductRequest;
import com.example.demo.model.response.HistoryProductResponse;
import com.example.demo.repository.HistoryProductRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.service.impl.HistoryProductImplService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Map;

@DataJpaTest // Chạy test với cấu hình JPA, sẽ khởi tạo EntityManager, Repository,...
@Import({HistoryProductImplService.class}) // Import service cần test
@Sql( // Import dữ liệu mẫu trước và dọn dữ liệu sau khi test
        value = "/sql/import_product.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
@Sql(
        value = "/sql/import_product_clean_up.sql",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
)
public class HistoryProductServiceTest extends AbstractBaseTest {

    @Autowired
    private HistoryProductRepository historyProductRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private HistoryProductImplService historyProductImplService;

    @Test
    @DisplayName("Get all history product test")
    public void get_all_history_product_test() {
        int pageNumber = 0;
        int pageSize = 5;

        // Gọi service lấy toàn bộ lịch sử sản phẩm
        BaseResponse<Page<HistoryProductResponse>> response =
                this.historyProductImplService.getAllHistoryProduct(pageNumber, pageSize);

        // Kiểm tra thông điệp trả về và tổng số bản ghi
        Assertions.assertEquals("Danh sách lịch sử xuất nhập kho", response.getMessage());
        Assertions.assertEquals(3, response.getData().getTotalElements());
    }

    @Test
    @DisplayName("Search history product by name and action")
    public void search_by_name_and_action() {
        int pageNumber = 0;
        int pageSize = 5;
        String productName = "Sản phẩm";
        String action = "IMPORT";
        String orderBy = "id";

        // Gọi API tìm kiếm theo tên sản phẩm và hành động
        BaseResponse<Page<HistoryProductResponse>> response =
                this.historyProductImplService.searchByNameAndAction(
                        pageNumber, pageSize, productName, action, orderBy);

        Assertions.assertEquals("Tìm kiếm và lọc lịch sử thành công!", response.getMessage());
        Assertions.assertEquals(3, response.getData().getTotalElements());
    }

    @Test
    @DisplayName("Create history product with product not found")
    public void create_history_with_product_not_found() {
        // Tạo request có chứa productId không tồn tại (4L)
        var historyProductRequest = HistoryProductRequest.builder()
                .note("note")
                .historyDetails(List.of(
                        HistoryDetail.builder().productId(1L).difference(1).build(),
                        HistoryDetail.builder().productId(4L).difference(-1).build()
                ))
                .build();

        // Mong muốn ném ra EntityNotFoundException khi productId không tồn tại
        Assertions.assertThrows(EntityNotFoundException.class,
                () -> this.historyProductImplService.createHistory(historyProductRequest, true, true));
    }

    // @Test
    // @DisplayName("Create history product with throw exception not enough quantity")
    // public void create_history_with_throw_not_enough_quantity() {
    //     // Số lượng trừ vượt quá tồn kho
    //     var historyProductRequest = HistoryProductRequest.builder()
    //             .note("note")
    //             .historyDetails(List.of(
    //                     HistoryDetail.builder().productId(3L).difference(-10).build()
    //             ))
    //             .build();
    //
    //     // Mong muốn ném ra BaseException do không đủ tồn kho
    //     Assertions.assertThrows(BaseException.class,
    //             () -> this.historyProductImplService.createHistory(historyProductRequest, true, true));
    // }

    @Test
    @DisplayName("Create history product successfully")
    public void create_history_success() {
        // Request với dữ liệu hợp lệ
        var historyProductRequest = HistoryProductRequest.builder()
                .note("note")
                .historyDetails(List.of(
                        HistoryDetail.builder().productId(2L).difference(-1).build()
                ))
                .build();

        // Gọi service tạo lịch sử
        BaseResponse response = this.historyProductImplService.createHistory(historyProductRequest, true, true);

        Assertions.assertEquals("Lịch sử hành động thành công.", response.getMessage());
    }

    @Test
    @DisplayName("Get history product by conditions")
    public void get_history_product_by_condition() {
        Map<String, String> params = Map.of(
                "productName", "Sản phẩm" // Điều kiện tìm theo tên
        );

        // Gọi service tìm kiếm theo điều kiện
        BaseResponse<Page<HistoryProductResponse>> response =
                this.historyProductImplService.getHistoryProductByConditons(params);

        Assertions.assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        Assertions.assertEquals(3, response.getData().getContent().size());
    }
}

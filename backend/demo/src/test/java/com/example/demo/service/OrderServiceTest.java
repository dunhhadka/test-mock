package com.example.demo.service;

import com.example.demo.entity.AccountEntity;
import com.example.demo.entity.EmployeeEntity;
import com.example.demo.entity.StoreEntity;
import com.example.demo.entity.enums.TypeEmployee;
import com.example.demo.exception.BaseException;
import com.example.demo.exception.EntityNotFoundException;
import com.example.demo.model.BaseResponse;
import com.example.demo.model.request.InsertOrder;
import com.example.demo.model.request.ProductOrders;
import com.example.demo.model.request.ServiceOrders;
import com.example.demo.model.response.orderReponse.BaseOrder;
import com.example.demo.model.response.orderReponse.OrderResponse;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.StoreRepository;
import com.example.demo.security.CustomUserDetail;
import com.example.demo.service.impl.HistoryProductImplService;
import com.example.demo.service.impl.OrderImplService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@DataJpaTest
@Import({OrderImplService.class})
@Sql(
        value = "/sql/import_order.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
@Sql(
        value = "/sql/clean_up_import_order.sql",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class OrderServiceTest extends AbstractBaseTest {

    @Autowired
    private OrderImplService orderImplService;

    @MockBean
    private HistoryProductImplService historyProductImplService;

    @Autowired
    private OrderRepository orderRepository;

    @Mock
    private CustomUserDetail customUserDetail;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @MockBean
    private StoreRepository storeRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    public void setUp() {
        // Tạo dữ liệu giả
        EmployeeEntity employee = new EmployeeEntity();
        employee.setId(1L);
        employee.setName("Test employee");
        employee.setType(TypeEmployee.REPAIRER);

        AccountEntity mockAccount = new AccountEntity();
        mockAccount.setEmployee(employee);

        when(customUserDetail.getAccount()).thenReturn(mockAccount);

        when(authentication.getPrincipal()).thenReturn(customUserDetail);

        when(securityContext.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);

        // store
        when(storeRepository.findById(anyLong())).thenReturn(Optional.of(StoreEntity.builder().name("store").build()));
    }


    @Test
    @DisplayName("Create order test")
    public void create_order_test() {
        var orderRequest = createOrderRequest(2);

        BaseResponse<OrderResponse> response = this.orderImplService.create(orderRequest);

        Assertions.assertEquals("tạo đơn hàng thành công.", response.getMessage());
    }

    @Test
    @DisplayName("Update order test")
    public void update_order_test() {
        var orderRequest = createOrderRequest(1);

        BaseResponse<OrderResponse> response = this.orderImplService.create(orderRequest);

        long orderId = response.getData().getId();

        entityManager.clear();

        var orderUpdateRequest = orderRequest.toBuilder()
                .motorbikeName("TOYOTA")
                .type("ORDER")
                .build();

        BaseResponse<OrderResponse> updateResponse = this.orderImplService.update(orderUpdateRequest, orderId);

        Assertions.assertEquals("TOYOTA", updateResponse.getData().getInfoCustomer().getMotorbikeName());
    }

    @Test
    @DisplayName("Delete by id")
    public void delete_by_id() {
        var orderRequest = createOrderRequest(1);

        BaseResponse<OrderResponse> response = this.orderImplService.create(orderRequest);

        this.entityManager.clear();

        BaseResponse<OrderResponse> response1 = this.orderImplService.deleteById(response.getData().getId());

        Assertions.assertEquals("Xóa đơn hàng thành công.", response1.getMessage());
    }

    @Test
    @DisplayName("Delete id not found")
    public void delete_id_not_found() {
        Assertions.assertThrows(EntityNotFoundException.class, () -> this.orderImplService.deleteById(10L));
    }

    @Test
    @DisplayName("Get by id")
    public void get_by_id() {
        var orderRequest = createOrderRequest(1);

        BaseResponse<OrderResponse> response = this.orderImplService.create(orderRequest);

        this.entityManager.clear();

        BaseResponse<OrderResponse> response1 = this.orderImplService.getById(response.getData().getId());

        Assertions.assertEquals("Nhận đơn hàng thành công.", response1.getMessage());
    }

    @Test
    @DisplayName("Get by id not found")
    public void get_by_id_not_found() {
        Assertions.assertThrows(EntityNotFoundException.class, () -> this.orderImplService.getById(10L));
    }

    @Test
    @DisplayName("To invoice")
    public void to_invoice() {
        var orderRequest = createOrderRequest(1);

        BaseResponse<OrderResponse> response = this.orderImplService.create(orderRequest);

        this.entityManager.clear();

        BaseResponse<OrderResponse> response1 = this.orderImplService.toInvoice(response.getData().getId());

        Assertions.assertEquals("Để hóa đơn thành công.", response1.getMessage());
    }

    @Test
    @DisplayName("GEt by params")
    public void get_by_params() {
        var orderRequest = createOrderRequest(1);

        BaseResponse<OrderResponse> response = this.orderImplService.create(orderRequest);

        this.entityManager.clear();

        Map<String, String> params = Map.of();

        BaseResponse<Page<BaseOrder>> response1 = this.orderImplService.getByParams(params);

        Assertions.assertEquals("Nhận đơn hàng thành công.", response1.getMessage());
    }

    @Test
    @DisplayName("To invoice with order not found")
    public void to_invoice_not_found() {
        Assertions.assertThrows(BaseException.class, () -> this.orderImplService.toInvoice(10L));
    }

    @Test
    @DisplayName("Create order with invalid product")
    public void create_order_invalid_product() {
        var orderRequest = createOrderRequest(3);

        Assertions.assertThrows(BaseException.class, () -> this.orderImplService.create(orderRequest));
    }

    @Test
    @DisplayName("Get all is null")
    public void get_all_is_null() {
        BaseResponse response = this.orderImplService.getAll(10, 10);
        Assertions.assertNull(response);
    }

    @Test
    @DisplayName("Create order with invalid service")
    public void create_order_invalid_service() {
        var orderRequest = InsertOrder.builder()
                .customerId(1L)
                .repairerId(1L)
                .motorbikeCode("001")
                .motorbikeName("YAMAHA")
                .note("NOTE")
                .services(
                        List.of(
                                ServiceOrders.builder()
                                        .id(10L)
                                        .quantity(1)
                                        .build()
                        )
                )
                .products(
                        List.of(
                                ProductOrders.builder()
                                        .id(1L)
                                        .quantity(1)
                                        .build()
                        )
                )
                .build();;

        Assertions.assertThrows(BaseException.class, () -> this.orderImplService.create(orderRequest));
    }


    private InsertOrder createOrderRequest(long productId) {
        return InsertOrder.builder()
                .customerId(1L)
                .repairerId(1L)
                .motorbikeCode("001")
                .motorbikeName("YAMAHA")
                .note("NOTE")
                .services(
                        List.of(
                                ServiceOrders.builder()
                                        .id(1L)
                                        .quantity(1)
                                        .build()
                        )
                )
                .products(
                        List.of(
                                ProductOrders.builder()
                                        .id(productId)
                                        .quantity(1)
                                        .build()
                        )
                )
                .build();
    }
}

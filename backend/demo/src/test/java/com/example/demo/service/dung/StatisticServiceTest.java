package com.example.demo.service.dung;

import com.example.demo.model.BaseResponse;
import com.example.demo.model.statistic.*;
import com.example.demo.service.AbstractBaseTest;
import com.example.demo.service.impl.StatisticService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Map;

@DataJpaTest
@Import({StatisticService.class})
@Sql(
        value = "/sql/statistic.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
@Sql(
        value = "/sql/statistic_clean_up.sql",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
)
public class StatisticServiceTest extends AbstractBaseTest {

    @Autowired
    private StatisticService statisticService;

    @Test
    @DisplayName("Statistic history product_in")
    public void statistic_history_product_in() {
        Map<String, String> params = Map.of(
                "code", "PROD002"
        );

        BaseResponse<List<StatisticProducts>> response = this.statisticService.statisticHistoryProductIn(params);

        Assertions.assertEquals("Thống kê nhập sản phẩm thành công.", response.getMessage());
    }

    @Test
    @DisplayName("Statistic history product_out")
    public void statistic_history_product_out() {
        Map<String, String> params = Map.of(
                "code", "PROD002"
        );

        BaseResponse<List<StatisticProducts>> response = this.statisticService.statisticHistoryProductOut(params);

        Assertions.assertEquals("Thống kê xuất sản phẩm thành công.", response.getMessage());
    }

    @Test
    @DisplayName("Statistic service test")
    public void statistic_service_test() {
        Map<String, String> params = Map.of();

        BaseResponse<Page<StatisticServices>> response = this.statisticService.statisticServices(params);

        Assertions.assertEquals("Thống kê dịch vụ thành công.", response.getMessage());
    }

    @Test
    @DisplayName("Statistic salary employee")
    public void statistic_salary_employee() {
        Map<String, String> params = Map.of();

        BaseResponse<Page<StatisticEmployees>> response = this.statisticService.statisticSalaryEmployee(params);

        Assertions.assertEquals("Thống kê lương nhân viên thành công.", response.getMessage());
    }

    @Test
    @DisplayName("Statistic top service")
    public void statistic_top_service() {
        Map<String, String> params = Map.of();

        BaseResponse<Page<StatisticTop>> response = this.statisticService.statisticTopService(params);

        Assertions.assertEquals("Thống kê top 10 dịch vụ được sử dụng nhiều nhất trong thời gian.", response.getMessage());
    }

    @Test
    @DisplayName("Statistic top product")
    public void statistic_top_product() {
        Map<String, String> params = Map.of();

        BaseResponse<Page<StatisticTop>> response = this.statisticService.statisticTopProduct(params);

        Assertions.assertEquals("Thống kê top 10 sản phẩm được sử dụng nhiều nhất trong thời gian.", response.getMessage());
    }

    @Test
    @DisplayName("Statistic history product_in with non-existent code")
    public void statistic_history_product_in_with_invalid_code() {
        Map<String, String> params = Map.of(
                "code", "NON_EXISTENT_CODE"
        );

        BaseResponse<List<StatisticProducts>> response = this.statisticService.statisticHistoryProductIn(params);

        Assertions.assertEquals("Thống kê nhập sản phẩm thành công.", response.getMessage());
        Assertions.assertTrue(response.getData().isEmpty());
    }

    @Test
    @DisplayName("Statistic history product_out with non-existent code")
    public void statistic_history_product_out_with_invalid_code() {
        Map<String, String> params = Map.of(
                "code", "NON_EXISTENT_CODE"
        );

        BaseResponse<List<StatisticProducts>> response = this.statisticService.statisticHistoryProductOut(params);

        Assertions.assertEquals("Thống kê xuất sản phẩm thành công.", response.getMessage());
        Assertions.assertTrue(response.getData().isEmpty());
    }

    @Test
    @DisplayName("Statistic service with date filter")
    public void statistic_service_with_date_filter() {
        Map<String, String> params = Map.of(
                "startDate", "2024-01-01",
                "endDate", "2024-12-31"
        );

        BaseResponse<Page<StatisticServices>> response = this.statisticService.statisticServices(params);

        Assertions.assertEquals("Thống kê dịch vụ thành công.", response.getMessage());
        Assertions.assertNotNull(response.getData());
    }

    @Test
    @DisplayName("Statistic top service with invalid date range")
    public void statistic_top_service_with_invalid_date_range() {
        Map<String, String> params = Map.of(
                "startDate", "2024-12-31",
                "endDate", "2024-01-01"
        );

        BaseResponse<Page<StatisticTop>> response = this.statisticService.statisticTopService(params);

        Assertions.assertEquals("Thống kê top 10 dịch vụ được sử dụng nhiều nhất trong thời gian.", response.getMessage());
        Assertions.assertTrue(response.getData().getContent().isEmpty());
    }
}

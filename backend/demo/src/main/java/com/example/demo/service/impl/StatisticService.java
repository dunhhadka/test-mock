package com.example.demo.service.impl;

import com.example.demo.model.BaseResponse;
import com.example.demo.model.params.StatisticEmployee;
import com.example.demo.model.params.StatisticProductParams;
import com.example.demo.model.params.StatisticServiceParams;
import com.example.demo.model.params.TimeParams;
import com.example.demo.model.statistic.*;
import com.example.demo.repository.EmployeeRepository;
import com.example.demo.repository.HistoryProductRepository;
import com.example.demo.repository.OrderProductsRepository;
import com.example.demo.repository.OrderServiceRepository;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StatisticService {

    private final OrderServiceRepository orderServiceRepository;
    private final EmployeeRepository employeeRepository;

    private final HistoryProductRepository historyProductRepository;

    private final OrderProductsRepository orderProductsRepository;

    public StatisticService(OrderServiceRepository orderServiceRepository, EmployeeRepository employeeRepository,
            HistoryProductRepository historyProductRepository, OrderProductsRepository orderProductsRepository) {
        this.orderServiceRepository = orderServiceRepository;
        this.employeeRepository = employeeRepository;
        this.historyProductRepository = historyProductRepository;
        this.orderProductsRepository = orderProductsRepository;
    }

    public BaseResponse statisticServices(Map<String, String> map) {
        StatisticServiceParams productParams = new StatisticServiceParams(map);
        Pageable pageable = PageRequest.of(productParams.getPageNumber() - 1, productParams.getPageSize());
        Page<Object[]> objects = orderServiceRepository.statisticService(productParams, pageable);
        List<StatisticServices> statisticServices = objects.stream().map(objects1 -> {
            StatisticServices.StatisticServicesBuilder builder = StatisticServices.builder()
                    .code((String) objects1[1])
                    .name((String) objects1[3])
                    .price((Double) objects1[4])
                    // .salaryDispatcher((Double) objects1[5])
                    // .salaryRepairer((Double) objects1[6])
                    // .profit((Double) objects1[7])
                    .income((Double) objects1[5]);

            // Kiểm tra và ép kiểu từ BigDecimal sang Long nếu cần
            if (objects1[0] instanceof BigDecimal) {
                builder.id(((BigDecimal) objects1[0]).longValue());
            } else if (objects1[0] instanceof Long) {
                builder.id((Long) objects1[0]);
            }

            if (objects1[2] instanceof BigDecimal) {
                builder.quantity(((BigDecimal) objects1[2]).longValue());
            } else if (objects1[2] instanceof Long) {
                builder.quantity((Long) objects1[2]);
            }

            return builder.build();
        }).collect(Collectors.toList());

        return BaseResponse.builder()
                .message("Thống kê dịch vụ thành công.")
                .statusCode(HttpStatus.OK.value())
                .data(new PageImpl<>(statisticServices, pageable, objects.getTotalElements()))
                .build();
    }

    // public BaseResponse statisticService(Map<String, String> map) {
    // Pageable pageable = PageRequest.of(1, 5);
    // List<Object[]> objects = orderServiceRepository.statisticService();
    // List<com.example.demo.model.statistic.StatisticService> statisticServices =
    // objects.stream().map(objects1 -> {
    // return com.example.demo.model.statistic.StatisticService.builder()
    // .id((Long) objects1[0])
    // .code((String) objects1[1])
    // .name((String) objects1[2])
    // .price((Double) objects1[3])
    // .quantityService((Long) objects1[4])
    // .profit((Double) objects1[5])
    // .build();
    // }).collect(Collectors.toList());
    // return BaseResponse.builder()
    // .message("Statistic service successfully.")
    // .statusCode(HttpStatus.OK.value())
    // .data(statisticServices)
    // .build();
    // }

    public BaseResponse statisticSalaryEmployee(Map<String, String> map) {
        StatisticEmployee statisticEmployee = new StatisticEmployee(map);
        Pageable pageable = PageRequest.of(statisticEmployee.getPageNumber() - 1, statisticEmployee.getPageSize());
        Page<Object[]> objects = employeeRepository.statisticSalaryEmployee(statisticEmployee, pageable);
        List<StatisticEmployees> statisticEmployees = objects.getContent().stream().map(objects1 -> {
            StatisticEmployees statisticEmployees1 = StatisticEmployees.builder()
                    .id((Long) objects1[0])
                    .code((String) objects1[1])
                    .name((String) objects1[2])
                    .salary((Double) objects1[3])
                    .type((String) objects1[4])
                    .incomeService((Double) objects1[5] == null ? 0 : (Double) objects1[5])
                    .build();
            statisticEmployees1.setTotal(statisticEmployees1.getIncomeService() + statisticEmployees1.getSalary());
            return statisticEmployees1;
        }).collect(Collectors.toList());
        return BaseResponse.builder()
                .message("Thống kê lương nhân viên thành công.")
                .statusCode(HttpStatus.OK.value())
                .data(new PageImpl<>(statisticEmployees, pageable, objects.getTotalElements()))
                .build();
    }

    // public BaseResponse statisticProduct(Map<String, String> map) {
    // List<Object[]> objects = historyProductRepository.statisticHistoryProduct();
    // List<StatisticProduct> statisticProducts = objects.stream().map(objects1 -> {
    // return StatisticProduct.builder()
    // .id((Long) objects1[0])
    // .code((String) objects1[1])
    // .name((String) objects1[2])
    // .priceIn((Double) objects1[3])
    // .priceOut((Double) objects1[4])
    // .quantityExport((Long) objects1[5])
    // .quantityImport((Long) objects1[6])
    // .income((Double) objects1[7])
    // .build();
    // }).collect(Collectors.toList());
    // return BaseResponse.builder()
    // .message("Statistic product successfully.")
    // .data(statisticProducts)
    // .statusCode(HttpStatus.OK.value())
    // .build();
    // }

    public BaseResponse statisticHistoryProductIn(Map<String, String> map) {
        StatisticProductParams productParams = new StatisticProductParams(map);
        Pageable pageable = PageRequest.of(productParams.getPageNumber() - 1, productParams.getPageSize());
        Page<Object[]> objectsIn = historyProductRepository.statisticHistoryProductIn(productParams, pageable);
        List<StatisticProducts> statisticProductsIn = mapToStatisticProductList(objectsIn);

        System.out.println(productParams);

        return BaseResponse.builder()
                .message("Thống kê nhập sản phẩm thành công.")
                .data(new PageImpl<>(statisticProductsIn, pageable, objectsIn.getTotalElements()))
                .statusCode(HttpStatus.OK.value())
                .build();
    }

    public BaseResponse statisticHistoryProductOut(Map<String, String> map) {
        StatisticProductParams productParams = new StatisticProductParams(map);
        Pageable pageable = PageRequest.of(productParams.getPageNumber() - 1, productParams.getPageSize());
        Page<Object[]> objectsOut = historyProductRepository.statisticHistoryProductOut(productParams, pageable);
        List<StatisticProducts> statisticProductsOut = mapToStatisticProductList(objectsOut);

        return BaseResponse.builder()
                .message("Thống kê xuất sản phẩm thành công.")
                .data(new PageImpl<>(statisticProductsOut, pageable, objectsOut.getTotalElements()))
                .statusCode(HttpStatus.OK.value())
                .build();
    }

    private List<StatisticProducts> mapToStatisticProductList(Page<Object[]> objects) {
        return objects.stream().map(object -> {
            Long id = ((Number) object[0]).longValue();
            String code = (String) object[1];
            String name = (String) object[2];
            String unit = (String) object[3];
            Double price = ((Number) object[4]).doubleValue();
            Long totalQuantity = ((Number) object[5]).longValue();
            Double income = ((Number) object[6]).doubleValue();

            return StatisticProducts.builder()
                    .id(id)
                    .code(code)
                    .name(name)
                    .unit(unit)
                    .price(price)
                    .quantity(totalQuantity)
                    .income(income)
                    .build();
        }).collect(Collectors.toList());
    }

    public BaseResponse statisticProductInYear() {
        List<Object[]> objects = historyProductRepository.statisticProductInYear();
        return BaseResponse.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Thống kê chi phí nhập linh kiện theo từng tháng thàng công.")
                .data(objects.stream().map(objects1 -> {
                    return StatisticMonth.builder()
                            .month((String) objects1[0])
                            .year((String) objects1[1])
                            .expense((Double) objects1[2]).build();
                }))
                .build();
    }

    public BaseResponse statisticProductOutInYear() {
        List<Object[]> objects = historyProductRepository.statisticProductOutInYear();
        return BaseResponse.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Thống kê doang thu xuất linh kiện theo từng tháng thàng công.")
                .data(objects.stream().map(objects1 -> {
                    return StatisticMonth.builder()
                            .month((String) objects1[0])
                            .year((String) objects1[1])
                            .expense((Double) objects1[2]).build();
                }))
                .build();
    }

    public BaseResponse statisticTopService(Map<String, String> map) {
        TimeParams timeParams = new TimeParams(map);
        List<Object[]> objects = orderServiceRepository.statisticTopInService(timeParams);
        return BaseResponse.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Thống kê top 10 dịch vụ được sử dụng nhiều nhất trong thời gian.")
                .data(objects.stream().map(objects1 -> {
                    return StatisticTop.builder()
                            .id((Long) objects1[0])
                            .name((String) objects1[1])
                            .price((Double) objects1[2])
                            .quantity(
                                    (objects1[3] instanceof BigDecimal) ? ((BigDecimal) objects1[3]).longValue() : null)
                            .build();
                }))
                .build();
    }

    public BaseResponse statisticTopProduct(Map<String, String> map) {
        TimeParams timeParams = new TimeParams(map);
        List<Object[]> objects = orderProductsRepository.statisticTopProduct(timeParams);
        return BaseResponse.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Thống kê top 10 dịch vụ được sử dụng nhiều nhất trong thời gian.")
                .data(objects.stream().map(objects1 -> {
                    return StatisticTop.builder()
                            .id((Long) objects1[0])
                            .name((String) objects1[1])
                            .price((Double) objects1[2])
                            .unit((String) objects1[3])
                            .quantity(
                                    (objects1[4] instanceof BigDecimal) ? ((BigDecimal) objects1[4]).longValue() : null)
                            .build();
                }))
                .build();
    }

    public BaseResponse statisticServiceUsageInTimeMonth() {
        List<Object[]> objects = orderServiceRepository.statisticServiceUsageInTimeMonth();
        return BaseResponse.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Thống kê doanh thu dịch vụ theo các tháng trong năm.")
                .data(objects.stream().map(objects1 -> {
                    StatisticMonthlyUsage monthlyUsage = StatisticMonthlyUsage.builder()
                            .month((String) objects1[0])
                            .year((String) objects1[1])
                            .expense((Double) objects1[2])
                            .build();
                    return monthlyUsage;
                }))
                .build();
    }
}

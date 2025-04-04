package com.example.demo.controller;

import com.example.demo.model.BaseResponse;
import com.example.demo.service.impl.StatisticService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
public class StatisticController {

    private final StatisticService statisticService;

    public StatisticController(StatisticService statisticService) {
        this.statisticService = statisticService;
    }

    @PreAuthorize("hasAuthority('MANAGER')")
    @GetMapping("/services")
    public ResponseEntity<BaseResponse> statisticService(@RequestParam Map<String, String> map) {
        BaseResponse response = statisticService.statisticServices(map);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PreAuthorize("hasAuthority('MANAGER')")
    @GetMapping("/employees-salary")
    public ResponseEntity<BaseResponse> statisticSalaryEmployees(
            @RequestParam Map<String, String> map) {
        BaseResponse response = statisticService.statisticSalaryEmployee(map);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    // @GetMapping("/products")
    // public ResponseEntity<BaseResponse> statisticProduct(
    // @RequestParam Map<String, String> map
    // ) {
    // return ResponseEntity.ok(statisticService.statisticProduct(map));
    // }

    @PreAuthorize("hasAuthority('MANAGER')")
    @GetMapping("/products-in")
    public ResponseEntity<BaseResponse> statisticHistoryProductIn(@RequestParam Map<String, String> map) {
        BaseResponse response = statisticService.statisticHistoryProductIn(map);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PreAuthorize("hasAuthority('MANAGER')")
    @GetMapping("/products-out")
    public ResponseEntity<BaseResponse> statisticHistoryProductOut(@RequestParam Map<String, String> map) {
        BaseResponse response = statisticService.statisticHistoryProductOut(map);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/products-in-year")
    public ResponseEntity<BaseResponse> statisticExpenseInYear() {
        return ResponseEntity.ok(statisticService.statisticProductInYear());
    }

    @GetMapping("/products-out-year")
    public ResponseEntity<BaseResponse> statisticExpenseOutInYear() {
        return ResponseEntity.ok(statisticService.statisticProductOutInYear());
    }

    @GetMapping("/top-service")
    public ResponseEntity<BaseResponse> statisticTopService(
            @RequestParam() Map<String, String> map) {
        return ResponseEntity.ok(statisticService.statisticTopService(map));
    }

    @GetMapping("/top-product")
    public ResponseEntity<BaseResponse> statisticTopProduct(
            @RequestParam() Map<String, String> map) {
        return ResponseEntity.ok(statisticService.statisticTopProduct(map));
    }

    @GetMapping("/services-year")
    public ResponseEntity<BaseResponse> statisticServiceUsageInTimeRange() {
        BaseResponse response = statisticService.statisticServiceUsageInTimeMonth();
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

}

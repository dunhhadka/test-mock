package com.example.demo.service;

import com.example.demo.config.SecurityConfig;
import com.example.demo.event.ListenerConfiguration;
import com.example.demo.handle.HandleAccessDenied;
import com.example.demo.handle.HandleAuthenticationEntryPoint;
import com.example.demo.jwt.JwtAuthFilter;
import com.example.demo.jwt.JwtService;
import com.example.demo.repository.StoreRepository;
import com.example.demo.schedule.ConfigSchedule;
import com.example.demo.security.CustomUserDetailService;
import lombok.extern.slf4j.Slf4j;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import javax.sql.DataSource;

@Slf4j
// Cấu hình Spring test với JUnit 5
@SpringJUnitConfig(
        initializers = ConfigDataApplicationContextInitializer.class,
        classes = AbstractBaseTest.BaseConfiguration.class // Load cấu hình test custom
)
// Ghi đè một số cấu hình trong application.properties chỉ dành cho môi trường test
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=update",
        "spring.jpa.properties.hibernate.show_sql=true",
        "spring.jpa.properties.hibernate.format_sql=true"
})
// Lớp abstract dùng làm base cho các test class khác
public abstract class AbstractBaseTest {

    // Cấu hình Spring cho các test case
    @TestConfiguration
    @ImportAutoConfiguration({
            HibernateJpaAutoConfiguration.class,        // Tự động cấu hình Hibernate JPA
            TransactionAutoConfiguration.class          // Tự động cấu hình Transaction
    })
    @Import({ // Import các bean cần thiết cho security và JWT vào context test
            SecurityConfig.class,
            StoreMockConfiguration.class,
            CustomUserDetailService.class,
            JwtAuthFilter.class,
            JwtService.class,
            HandleAuthenticationEntryPoint.class,
            HandleAccessDenied.class,
            ConfigSchedule.class,
            ListenerConfiguration.class
    })
    @EntityScan(basePackages = "com.example.demo.entity") // Scan entity để JPA hoạt động
    @EnableJpaRepositories(basePackages = "com.example.demo.repository") // Enable JPA repo
    static class BaseConfiguration {

        // Bean cấu hình datasource H2 in-memory cho test
        @Bean
        public DataSource dataSource(DataSourceProperties dataSourceProperties) {
            return dataSourceProperties
                    .initializeDataSourceBuilder()
                    .url("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
                    .username("sa")
                    .password("")
                    .build();
        }
    }

    // Cấu hình các bean mock dùng trong test
    @TestConfiguration
    static class StoreMockConfiguration {

        // Mock StoreService
        @Bean
        StoreService storeService() {
            return logAndMock(StoreService.class);
        }

        // Mock HistoryProductService
        @Bean
        HistoryProductService historyProductService() {
            return logAndMock(HistoryProductService.class);
        }
    }

    // Hàm dùng chung để tạo mock và log ra class đang được mock
    public static <T> T logAndMock(Class<T> clazz) {
        log.info("Using mock: {}", clazz.getName());
        return Mockito.mock(clazz);
    }
}

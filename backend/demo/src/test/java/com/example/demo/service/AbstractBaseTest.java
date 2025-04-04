package com.example.demo.service;

import com.example.demo.config.SecurityConfig;
import com.example.demo.handle.HandleAccessDenied;
import com.example.demo.handle.HandleAuthenticationEntryPoint;
import com.example.demo.jwt.JwtAuthFilter;
import com.example.demo.jwt.JwtService;
import com.example.demo.repository.StoreRepository;
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
@SpringJUnitConfig(
        initializers = ConfigDataApplicationContextInitializer.class,
        classes = AbstractBaseTest.BaseConfiguration.class
)
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=update",
        "spring.jpa.properties.hibernate.show_sql=true",
        "spring.jpa.properties.hibernate.format_sql=true"
})
abstract class AbstractBaseTest {

    @TestConfiguration
    @ImportAutoConfiguration({
            HibernateJpaAutoConfiguration.class,
            TransactionAutoConfiguration.class
    })
    @Import({SecurityConfig.class, StoreMockConfiguration.class,
            CustomUserDetailService.class, JwtAuthFilter.class, JwtService.class,
            HandleAuthenticationEntryPoint.class, HandleAccessDenied.class
    })
    @EntityScan(basePackages = "com.example.demo.entity")
    @EnableJpaRepositories(basePackages = "com.example.demo.repository")
    static class BaseConfiguration {

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

    @TestConfiguration
    static class StoreMockConfiguration {

        @Bean
        StoreService storeService() {
            return logAndMock(StoreService.class);
        }
    }

    public static <T> T logAndMock(Class<T> clazz) {
        log.info("Using mock: {}", clazz.getName());
        return Mockito.mock(clazz);
    }
}

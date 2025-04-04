package com.example.demo.service;

import com.example.demo.entity.HistoryEntity;
import com.example.demo.entity.enums.Action;
import com.example.demo.repository.HistoryProductRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.service.impl.HistoryProductImplService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class HistoryProductServiceTest {

    @Mock
    private HistoryProductRepository historyProductRepository;

    @Mock
    private ProductRepository productRepository;

    private HistoryProductImplService service;

    public static class Fixtures {
        public static List<HistoryEntity> entities = List.of(
                HistoryEntity.builder()
                        .difference(1)
                        .note("note")
                        .quantityLeft(2)
                        .action(Action.IMPORT)
                        .productName("product-name")
                        .difference(1)
                        .unit("unit")
                        .build(),
                HistoryEntity.builder()
                        .difference(2)
                        .quantityLeft(1)
                        .note("note")
                        .action(Action.IMPORT)
                        .productName("product-name")
                        .unit("unit")
                        .difference(1)
                        .build(),
                HistoryEntity.builder()
                        .difference(1)
                        .note("note")
                        .quantityLeft(2)
                        .action(Action.IMPORT)
                        .productName("product-name")
                        .difference(1)
                        .unit("unit")
                        .build()
        );
    }

    @BeforeEach
    public void setUp() {
        this.service = new HistoryProductImplService(
                historyProductRepository,
                productRepository
        );
    }

    @Test
    public void getAllHistoryProduct() {
        int pageNumber = 10;
        int pageSize = 1;
        Pageable pageable = Pageable.ofSize(pageSize).withPage(pageNumber);
        when(this.historyProductRepository.findAll(pageable)).thenReturn(new PageImpl<>(Fixtures.entities));

        var response = this.service.getAllHistoryProduct(pageNumber, pageSize);

        Assertions.assertEquals(Fixtures.entities.size(), ((PageImpl) response.getData()).getContent().size());
    }
}

package com.example.demo.model.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@NoArgsConstructor
@Builder
@Data
@AllArgsConstructor
public class ProductOrders {
    @NotNull(message = "id là bắt buộc.")
    @Positive(message = "id phải lớn hơn 0.")
    private Long id;

    @NotNull(message = "Số lượng là bắt buộc.")
    @Positive(message = "id phải lớn hơn 0.")
    private Integer quantity;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductOrders that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

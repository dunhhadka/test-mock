package com.example.demo.entity;

import com.example.demo.constant.CodeConstant;
import com.example.demo.entity.enums.Action;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Formula;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ProductEntity extends BaseEntity {
    private String code;
    private String name;
    private Double priceIn;
    private Double priceOut;
    private String brand;
    private String description;
    private Integer storageQuantity;
    private Integer quantityWarning;
    private String imageUrl;

    private String unit;
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<HistoryEntity> historyList = new ArrayList<>();

    @Formula("CASE WHEN storage_quantity >= quantity_warning THEN 'IN_STOCK' WHEN storage_quantity > 0 then 'LOW_STOCK' ELSE 'OUT_STOCK' END")
    private String status;

    @PostPersist
    public void postPersist() {
        String idString = String.format("%06d", this.getId());
        code = CodeConstant.CODE_PRODUCT + idString;
    }

    public void addToInventory(int difference, String note) {
        updateInventory(difference, note);
    }

    public void removeFromInventory(int difference, String note) {
        updateInventory(-difference, note);
    }

    private void updateInventory(int difference, String note) {
        int newQuantityLeft = storageQuantity + difference;
        Action action = difference > 0 ? Action.IMPORT : Action.EXPORT;

        HistoryEntity history = HistoryEntity.builder()
                .product(this)
                .difference(difference)
                .note(note)
                .quantityLeft(newQuantityLeft)
                .action(action)
                .productName(name)
                .priceIn(priceIn)
                .priceOut(priceOut)
                .build();
        storageQuantity = newQuantityLeft;
        if (this.historyList == null) this.historyList = new ArrayList<>();
        historyList.add(history);
    }
}

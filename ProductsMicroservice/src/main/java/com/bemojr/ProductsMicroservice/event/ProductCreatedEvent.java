package com.bemojr.ProductsMicroservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductCreatedEvent {
    private String productId;

    private String productName;

    private String currency;

    private BigDecimal price;

    private Integer quantity;
}

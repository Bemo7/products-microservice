package com.bemojr.ProductsMicroservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Product {
    @GeneratedValue(strategy = GenerationType.UUID)
    @Id()
    @Column(columnDefinition = "VARCHAR(36)")
    private String id;

    @Column(nullable = false,
            unique = true)
    private String title;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer quantity;
}

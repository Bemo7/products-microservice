package com.bemojr.ProductsMicroservice.dto;

import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.*;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record ProductRequest (
        @NotBlank(message = "Product name must not be blank!")
        @NotEmpty(message = "Product name must not be empty!")
        String title,

        @NotBlank(message = "Currency must not be blank!")
        @NotEmpty(message = "Currency name must not be empty!")
        String currency,

        @Digits(integer = 10, fraction = 2)
        @NotNull(message = "Price can not be null")
        BigDecimal price,

        @Min(1)
        @NotNull(message = "Quantity can not be null")
        Integer quantity
) {
}

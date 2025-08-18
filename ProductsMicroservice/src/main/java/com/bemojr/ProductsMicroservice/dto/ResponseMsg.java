package com.bemojr.ProductsMicroservice.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ResponseMsg(
        String status,
        Object message,
        LocalDateTime timestamp
) {
}

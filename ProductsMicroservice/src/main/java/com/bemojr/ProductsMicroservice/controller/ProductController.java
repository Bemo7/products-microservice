package com.bemojr.ProductsMicroservice.controller;

import com.bemojr.ProductsMicroservice.dto.ProductRequest;
import com.bemojr.ProductsMicroservice.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @PostMapping()
    public ResponseEntity<String> createProduct(
            @RequestBody() @Valid ProductRequest request
    ) throws ExecutionException, InterruptedException {
        String productId = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(productId);
    };
}

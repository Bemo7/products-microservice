package com.bemojr.ProductsMicroservice.service;

import com.bemojr.ProductsMicroservice.dto.ProductRequest;

import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutionException;

public interface ProductService {
    String createProduct(ProductRequest productRequest) throws ExecutionException, InterruptedException;
}

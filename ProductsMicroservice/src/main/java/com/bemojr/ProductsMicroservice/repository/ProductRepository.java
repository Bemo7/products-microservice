package com.bemojr.ProductsMicroservice.repository;

import com.bemojr.ProductsMicroservice.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {
    Optional<Product> findByTitle(String productName);
}

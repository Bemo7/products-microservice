package com.bemojr.ProductsMicroservice.serviceImpl;

import com.bemojr.ProductsMicroservice.dto.ProductRequest;
import com.bemojr.ProductsMicroservice.entity.Product;
import com.bemojr.ProductsMicroservice.repository.ProductRepository;
import com.bemojr.ProductsMicroservice.service.ProductService;
import com.bemojr.core.ProductCreatedEvent;
import jakarta.persistence.EntityExistsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.header.Headers;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    @Value("${application.topic.product-created-event-topic}")
    private String productCreatedEventTopic;

    private final ProductRepository productRepository;
    private final KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate;

    @Override
    @Transactional
    public String createProduct(ProductRequest productRequest) throws ExecutionException, InterruptedException {
        productRepository.findByTitle(productRequest.title()).ifPresent(
                product -> {
                    throw new EntityExistsException("Product " + productRequest.title() +  " already exists!");
                }
        );

        Product product = Product.builder()
                .title(productRequest.title())
                .price(productRequest.price())
                .currency(productRequest.currency())
                .quantity(productRequest.quantity())
                .build();

        productRepository.save(product);

        ProductCreatedEvent productCreatedEvent = ProductCreatedEvent.builder()
                .productId(product.getId())
                .productName(product.getTitle())
                .price(product.getPrice())
                .quantity(product.getQuantity())
                .currency(product.getCurrency())
                .build();

        SendResult<String, ProductCreatedEvent> result = kafkaTemplate.send(productCreatedEventTopic, productCreatedEvent).get();

        RecordMetadata recordMetadata = result.getRecordMetadata();

        Headers headers = result.getProducerRecord().headers();

        headers.forEach(
                header -> {log.info("********* Header : {} => {}", header.key(), header.value());}
        );

        log.info("********* Topic: {}", recordMetadata.topic());
        log.info("********* Partition: {}", recordMetadata.partition());
        log.info("********* Offset: {}", recordMetadata.offset());

        log.info("********* Returning product ID");

        return product.getId();
    }

    private void sendMessageAsync(ProductCreatedEvent productCreatedEvent) {
        CompletableFuture<SendResult<String, ProductCreatedEvent>> completableFuture = kafkaTemplate.send(productCreatedEventTopic, productCreatedEvent);

        completableFuture.whenComplete((result, throwable) -> {
            if (throwable != null) {
                log.error("********* Failed to send message {}", throwable.getMessage());
            } else {
                log.info("********* Message sent successfully! {}", result.getRecordMetadata());
            }
        });
    }
}

package com.bemojr.emailnotification.handler;

import com.bemojr.core.ProductCreatedEvent;
import com.bemojr.emailnotification.entity.ProcessedEvent;
import com.bemojr.emailnotification.exception.NotRetryableException;
import com.bemojr.emailnotification.exception.RetryableException;
import com.bemojr.emailnotification.repository.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@RequiredArgsConstructor
@Component
@Slf4j
@KafkaListener(topics = {"product-created-events-topic"})
public class ProductCreatedEventHandler {
    private final RestTemplate restTemplate;
    private final ProcessedEventRepository processedEventRepository;

    @Transactional
    @KafkaHandler()
    public void handle(@Payload ProductCreatedEvent productCreatedEvent, @Header("messageId") String messageId, @Header(KafkaHeaders.RECEIVED_KEY) String messageKey) {
        log.info("Received a new event:\tKey -> {} \tID -> {}\tTitle -> {}", messageKey ,productCreatedEvent.getProductId(),productCreatedEvent.getProductName());

//      Check if incoming message was already processed before
        Optional<ProcessedEvent> processedEvent = processedEventRepository.findByMessageId(messageId);
        if (processedEvent.isPresent()) {
            log.info("Found a duplicate message id: {}", processedEvent.get().getMessageId());
            return;
        }

        String url = "http://localhost:8082/response/200";

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, null, String.class);
            if (response.getStatusCode().value() == HttpStatus.OK.value()) {
                log.info("Received response from a remote service {}", response.getBody());
            }
        } catch (ResourceAccessException e) {
            log.error(e.getMessage());
            throw new RetryableException(e);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new NotRetryableException(e);
        }

//      Save a unique message id in a database table
        ProcessedEvent newProcessedEvent = ProcessedEvent.builder()
                .messageId(messageId)
                .productId(productCreatedEvent.getProductId())
                .build();

       try {
           processedEventRepository.save(newProcessedEvent);
       } catch (DataIntegrityViolationException e) {
           log.error(e.getMessage());
           throw new NotRetryableException(e);
       }
    }
}

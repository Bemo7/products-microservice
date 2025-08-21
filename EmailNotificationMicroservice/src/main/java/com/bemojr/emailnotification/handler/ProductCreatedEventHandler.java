package com.bemojr.emailnotification.handler;

import com.bemojr.core.ProductCreatedEvent;
import com.bemojr.emailnotification.exception.NotRetryableException;
import org.slf4j.*;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@KafkaListener(topics = {"product-created-events-topic"})
public class ProductCreatedEventHandler {
    private final Logger LOGGER = LoggerFactory.getLogger(ProductCreatedEventHandler.class);

    @KafkaHandler()
    public void handle(ProductCreatedEvent productCreatedEvent) {
//        if (true) throw new NotRetryableException("An error has occurred");
        LOGGER.info("Received a new event: {}", productCreatedEvent.getProductName());
    }
}

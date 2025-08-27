package com.bemojr.emailnotification;

import com.bemojr.core.ProductCreatedEvent;
import com.bemojr.emailnotification.entity.ProcessedEvent;
import com.bemojr.emailnotification.handler.ProductCreatedEventHandler;
import com.bemojr.emailnotification.repository.ProcessedEventRepository;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@EmbeddedKafka
@SpringBootTest(properties = {"spring.kafka.consumer.bootstrap-servers=${spring.embedded.kafka.brokers}"})
public class ProductCreatedEventHandlerIntegrationTest {
    @MockitoBean
    ProcessedEventRepository processedEventRepository;

    @MockitoBean
    RestTemplate restTemplate;

    @MockitoSpyBean
    ProductCreatedEventHandler productCreatedEventHandler;

    @Autowired
    private Environment environment;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Test
    void testProductCreatedEventHandler_OnProductCreated_HandlesEvent() throws ExecutionException, InterruptedException {

        // Arrange
        ProductCreatedEvent productCreatedEvent = ProductCreatedEvent.builder()
                .productId(UUID.randomUUID().toString())
                .productName("Antique Clothing")
                .currency("Naira")
                .quantity(2)
                .price(BigDecimal.valueOf(1000))
                .build();

        String messageId = UUID.randomUUID().toString();
        String messageKey = productCreatedEvent.getProductId();

        ProducerRecord<String, Object> record = new ProducerRecord<>(
                environment.getProperty("application.kafka.topic-name","product-created-events-topic"),
                messageKey,
                productCreatedEvent
        );

        record.headers().add("messageId", messageId.getBytes());
        record.headers().add(KafkaHeaders.RECEIVED_KEY, messageKey.getBytes());

        ProcessedEvent processedEvent = ProcessedEvent.builder()
                .messageId(messageId)
                .productId(productCreatedEvent.getProductId())
                .id(1L)
                .build();

        when(processedEventRepository.findByMessageId(anyString())).thenReturn(Optional.ofNullable(processedEvent));
        when(processedEventRepository.save(any(ProcessedEvent.class))).thenReturn(processedEvent);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), isNull(), eq(String.class))).thenReturn(ResponseEntity.ok().build());

        // Act
        kafkaTemplate.send(record).get();

        // Asset
        ArgumentCaptor<String> messageIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> messageKeyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<ProductCreatedEvent> messagePayloadCaptor = ArgumentCaptor.forClass(ProductCreatedEvent.class);

        verify(productCreatedEventHandler, timeout(5000).times(1)).handle(messagePayloadCaptor.capture(),
                messageIdCaptor.capture(),
                messageKeyCaptor.capture());

        assertEquals(messageId, messageIdCaptor.getValue());
        assertEquals(messageKey, messageKeyCaptor.getValue());
        assertEquals(productCreatedEvent, messagePayloadCaptor.getValue());
    }
}

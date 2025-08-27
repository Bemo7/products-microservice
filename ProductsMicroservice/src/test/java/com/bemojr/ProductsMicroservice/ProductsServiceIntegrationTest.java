package com.bemojr.ProductsMicroservice;

import com.bemojr.ProductsMicroservice.dto.ProductRequest;
import com.bemojr.ProductsMicroservice.service.ProductService;
import com.bemojr.core.ProductCreatedEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@DirtiesContext
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test") // application-test.properties
@EmbeddedKafka(partitions = 3, count = 3, controlledShutdown = true, kraft = true)
@SpringBootTest(properties = {"spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}"})
public class ProductsServiceIntegrationTest {
    @Autowired
    private ProductService productService;
    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;
    @Autowired
    private Environment environment;
    private KafkaMessageListenerContainer<String, ProductCreatedEvent> container;
    private BlockingQueue<ConsumerRecord<String, ProductCreatedEvent>> records;

    @BeforeAll
    void setUp() {
        // Create Kafka Consumer Factory
        DefaultKafkaConsumerFactory<String, Object> consumerFactory = new DefaultKafkaConsumerFactory<>(getConsumerProperties());

        // Create / Instantiate Container Properties
        ContainerProperties containerProperties = new ContainerProperties(environment.getProperty("application.kafka.topic-name"));

        // Create / Instantiate Message Listener Container
        container = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);

        // Instantiate a Blocking Queue
        records = new LinkedBlockingDeque<>();

        // Configure container message listener using lambda expression that adds each received record to the records queue
        container.setupMessageListener((MessageListener<String, ProductCreatedEvent>) records::add);

        // Start Kafka Message Listener Container
        container.start();

        // Wait for all kafka partitions to be assigned
        ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());
    }

    @Test
    void testCreateProduct_whenGivenValidProductDetails_successfullySendsKafkaMessage() throws Exception {
        // Arrange
        ProductRequest productRequest = ProductRequest.builder()
                .title("Antique Dinning Set")
                .price(BigDecimal.valueOf(770000))
                .quantity(10)
                .currency("Naira")
                .build();

        // Act
        productService.createProduct(productRequest);

        // Assert
        ConsumerRecord<String, ProductCreatedEvent> message = records.poll(3000, TimeUnit.MILLISECONDS);
        assertNotNull(message);

        boolean hasMessageIdHeader = Arrays.stream(message.headers().toArray())
                .filter(header -> header.key().equals("messageId"))
                .collect(Collectors.toSet()).isEmpty();
        assertFalse(hasMessageIdHeader);

        assertNotNull(message.key());

        ProductCreatedEvent productCreatedEvent = message.value();
        assertEquals(productRequest.price(), productRequest.price());
        assertEquals(productCreatedEvent.getProductName(), productRequest.title());
        assertEquals(productCreatedEvent.getCurrency(), productRequest.currency());
        assertEquals(productCreatedEvent.getQuantity(), productRequest.quantity());
    }

    private Map<String, Object> getConsumerProperties() {
        return Map.of(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafkaBroker.getBrokersAsString(),
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class,
                ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class,
                ConsumerConfig.GROUP_ID_CONFIG, environment.getProperty("spring.kafka.consumer.group-id","product-created-event"),
                JsonDeserializer.TRUSTED_PACKAGES, environment.getProperty("spring.kafka.consumer.properties.spring.json.trusted.packages","com.bemojr.core"),
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, environment.getProperty("spring.kafka.consumer.auto-offset-reset","earliest"));
    }

    @AfterAll
    void tearDown() {
        container.stop();
    }
}

package com.capstone.bgJobs.config;

import com.capstone.bgJobs.dto.UpdateAcknowledgement;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    // Consumer for UpdateAlertEvent
    // @Bean
    // public ConsumerFactory<String, UpdateAlertEvent> updateAlertEventConsumerFactory() {
    //     Map<String, Object> props = new HashMap<>();
    //     props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    //     props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    //     props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
    //     props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
    //     props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
    //     props.put(ConsumerConfig.GROUP_ID_CONFIG, "update-alert-group");
    //     return new DefaultKafkaConsumerFactory<>(
    //             props,
    //             new StringDeserializer(),
    //             new JsonDeserializer<>(UpdateAlertEvent.class)
    //     );
    // }

    // @Bean
    // public ConcurrentKafkaListenerContainerFactory<String, UpdateAlertEvent> updateAlertEventListenerContainerFactory() {
    //     ConcurrentKafkaListenerContainerFactory<String, UpdateAlertEvent> factory =
    //             new ConcurrentKafkaListenerContainerFactory<>();
    //     factory.setConsumerFactory(updateAlertEventConsumerFactory());
    //     return factory;
    // }

    // @Bean
    // public ConsumerFactory<String, CreateTicketEvent> createTicketEventConsumerFactory() {
    //     Map<String, Object> props = new HashMap<>();
    //     props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    //     props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    //     props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
    //     props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
    //     props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
    //     props.put(ConsumerConfig.GROUP_ID_CONFIG, "update-alert-group");
    //     return new DefaultKafkaConsumerFactory<>(
    //             props,
    //             new StringDeserializer(),
    //             new JsonDeserializer<>(CreateTicketEvent.class)
    //     );
    // }

    // @Bean
    // public ConcurrentKafkaListenerContainerFactory<String, CreateTicketEvent> createTicketEventListenerContainerFactory() {
    //     ConcurrentKafkaListenerContainerFactory<String, CreateTicketEvent> factory =
    //             new ConcurrentKafkaListenerContainerFactory<>();
    //     factory.setConsumerFactory(createTicketEventConsumerFactory());
    //     return factory;
    // }

    // @Bean
    // public ConsumerFactory<String, UpdateTicketEvent> updateTicketEventConsumerFactory() {
    //     Map<String, Object> props = new HashMap<>();
    //     props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    //     props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    //     props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
    //     props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
    //     props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
    //     props.put(ConsumerConfig.GROUP_ID_CONFIG, "update-alert-group");
    //     return new DefaultKafkaConsumerFactory<>(
    //             props,
    //             new StringDeserializer(),
    //             new JsonDeserializer<>(UpdateTicketEvent.class)
    //     );
    // }

    // @Bean
    // public ConcurrentKafkaListenerContainerFactory<String, UpdateTicketEvent> updateTicketEventListenerContainerFactory() {
    //     ConcurrentKafkaListenerContainerFactory<String, UpdateTicketEvent> factory =
    //             new ConcurrentKafkaListenerContainerFactory<>();
    //     factory.setConsumerFactory(updateTicketEventConsumerFactory());
    //     return factory;
    // }

    @Bean
    public ConsumerFactory<String, String> unifiedConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        // We use StringDeserializer for values to parse them manually
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "bgjobs-group-unified");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> unifiedListenerContainerFactory(
            KafkaTemplate<String, String> kafkaTemplate) {

        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(unifiedConsumerFactory());

        // Optional: Add error handler + DLT
        FixedBackOff backOff = new FixedBackOff(0L, 3L);
        DefaultErrorHandler errorHandler =
                new DefaultErrorHandler(new DeadLetterPublishingRecoverer(kafkaTemplate), backOff);
        factory.setCommonErrorHandler(errorHandler);

        return factory;
    }

    @Bean
    public ProducerFactory<String, String> stringProducerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, String> stringKafkaTemplate() {
        return new KafkaTemplate<>(stringProducerFactory());
    }

    // Producer for acknowledgements
    @Bean
    public ProducerFactory<String, UpdateAcknowledgement> acknowledgementProducerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, UpdateAcknowledgement> acknowledgementKafkaTemplate() {
        return new KafkaTemplate<>(acknowledgementProducerFactory());
    }
}
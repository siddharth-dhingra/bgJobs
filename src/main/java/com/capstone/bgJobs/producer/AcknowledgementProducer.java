package com.capstone.bgJobs.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.capstone.bgJobs.dto.UpdateAcknowledgement;

@Component
public class AcknowledgementProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(AcknowledgementProducer.class);

    @Value("${app.kafka.topics.job-acknowledgement}")
    private String ackTopic;

    private final KafkaTemplate<String, UpdateAcknowledgement> kafkaTemplate;

    public AcknowledgementProducer(KafkaTemplate<String, UpdateAcknowledgement> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendAcknowledgement(UpdateAcknowledgement ack) {
        kafkaTemplate.send(ackTopic, ack);
        LOGGER.info("Published PullAcknowledgement to topic {} => {}", ackTopic, ack);
    }
}
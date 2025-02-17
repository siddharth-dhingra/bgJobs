package com.capstone.bgJobs.consumer;

import com.capstone.bgJobs.dto.UpdateAlertEvent;
import com.capstone.bgJobs.model.UpdateEvent;
import com.capstone.bgJobs.service.UpdateAlertService;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;


@Component
public class UpdateAlertConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateAlertConsumer.class);

    private final UpdateAlertService updateAlertService;

    public UpdateAlertConsumer(UpdateAlertService updateAlertService) {
        this.updateAlertService = updateAlertService;
    }

    @KafkaListener(
        topics = "${app.kafka.topics.jfc-bgjobs}",
        groupId = "update-alert-group",
        containerFactory = "updateAlertEventListenerContainerFactory"
    )
    public void consumeUpdateAlertEvent(ConsumerRecord<String, UpdateAlertEvent> record) {
        UpdateAlertEvent wrapper = record.value();
        UpdateEvent event = wrapper.getPayload();
        String eventId = wrapper.getEventId();

        LOGGER.info("Received UpdateAlertEvent => eventId={}, payload={}", wrapper.getEventId(), event);

        updateAlertService.handleUpdateEvent(event, eventId);
    }
}
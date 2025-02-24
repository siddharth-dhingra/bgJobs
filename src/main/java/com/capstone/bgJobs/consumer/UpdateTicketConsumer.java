// package com.capstone.bgJobs.consumer;

// import com.capstone.bgJobs.dto.UpdateTicketEvent;
// import com.capstone.bgJobs.model.EventTypes;
// import com.capstone.bgJobs.model.UpdateTicketPayload;
// import com.capstone.bgJobs.service.UpdateTicketService;

// import org.apache.kafka.clients.consumer.ConsumerRecord;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.kafka.annotation.KafkaListener;
// import org.springframework.stereotype.Component;


// @Component
// public class UpdateTicketConsumer {

//     private static final Logger LOGGER = LoggerFactory.getLogger(UpdateAlertConsumer.class);

//     private final UpdateTicketService updateTicketService;

//     public UpdateTicketConsumer(UpdateTicketService updateTicketService) {
//         this.updateTicketService = updateTicketService;
//     }

//     @KafkaListener(
//         topics = "${app.kafka.topics.jfc-bgjobs}",
//         groupId = "bgjobs-group",
//         containerFactory = "updateTicketEventListenerContainerFactory"
//     )
//     public void consumeUpdateTicketEvent(ConsumerRecord<String, UpdateTicketEvent> record) {
//         UpdateTicketEvent wrapper = record.value();
//         System.out.println(wrapper);
//         if(wrapper.getType() != EventTypes.TICKETING_UPDATE) {
//             return;
//         }

//         UpdateTicketPayload event = wrapper.getPayload();
//         String jobId = event.getJobId();

//         LOGGER.info("Received UpdateTicketEvent => eventId={}, payload={}, type={}", wrapper.getEventId(), event, wrapper.getType());

//         updateTicketService.updateTicketStatusToDone(event.getTenantId(), event.getTicketId(), jobId);
//     }
// }
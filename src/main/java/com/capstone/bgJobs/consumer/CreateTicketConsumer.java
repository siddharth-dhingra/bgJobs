// package com.capstone.bgJobs.consumer;

// import com.capstone.bgJobs.dto.CreateTicketEvent;
// import com.capstone.bgJobs.model.CreateTicketPayload;
// import com.capstone.bgJobs.model.EventTypes;
// import com.capstone.bgJobs.service.CreateTicketService;

// import org.apache.kafka.clients.consumer.ConsumerRecord;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.kafka.annotation.KafkaListener;
// import org.springframework.stereotype.Component;


// @Component
// public class CreateTicketConsumer {

//     private static final Logger LOGGER = LoggerFactory.getLogger(UpdateAlertConsumer.class);

//     private final CreateTicketService createTicketService;

//     public CreateTicketConsumer(CreateTicketService createTicketService) {
//         this.createTicketService = createTicketService;
//     }

//     @KafkaListener(
//         topics = "${app.kafka.topics.jfc-bgjobs}",
//         groupId = "bgjobs-group",
//         containerFactory = "createTicketEventListenerContainerFactory"
//     )
//     public void consumeCreateTicketEvent(ConsumerRecord<String, CreateTicketEvent> record) {

//         CreateTicketEvent wrapper = record.value();
//         System.out.println(wrapper);
//         if(wrapper.getType() != EventTypes.TICKETING_CREATE) {
//             return;
//         }
        
//         CreateTicketPayload event = wrapper.getPayload();
//         String jobId = event.getJobId();

//         LOGGER.info("Received CreateTicketEvent => eventId={}, payload={}, type={}", wrapper.getEventId(), event, wrapper.getType());

//         createTicketService.handleCreateTicketEvent(event.getTenantId(), event.getFindingId(), event.getSummary(), event.getDescription(), jobId);
//     }
// }
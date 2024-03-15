package com.zaga.kafka.consumer;

import org.eclipse.microprofile.reactive.messaging.Incoming;

import com.zaga.entity.otelevent.OtelEvents;
import com.zaga.handler.EventCommandHandler;
import com.zaga.repo.EventRepo;

import jakarta.inject.Inject;

public class EventConsumerService {

    @Inject
    private EventCommandHandler eventCommandHandler;

    @Inject
    EventRepo eventRepo;
    
    @Incoming("event-audit-in")
      public void consumeEventDetails(OtelEvents events) {
        System.out.println("consumed Event -----------");
        eventRepo.persist(events);
     }

     @Incoming("event-in")
     public void consumeEventDTODetails(OtelEvents events) {
       System.out.println("consumed EventDTO -----------");
       eventCommandHandler.handleEventData(events);
    }

}

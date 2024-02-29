package com.zaga.kafka.consumer;

import org.eclipse.microprofile.reactive.messaging.Incoming;

import com.zaga.entity.otelevent.OtelEvents;
import com.zaga.handler.EventCommandHandler;

import jakarta.inject.Inject;

public class EventConsumerService {

    @Inject
    private EventCommandHandler eventCommandHandler;
    
    @Incoming("event-in")
      public void consumeEventDetails(OtelEvents events) {
        System.out.println("consumed Event -----------"+ events);
        eventCommandHandler.createEvents(events);
     }


}

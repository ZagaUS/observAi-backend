package com.zaga.kafka.consumer;


import org.eclipse.microprofile.reactive.messaging.Incoming;

import com.zaga.entity.otellog.OtelLog;
import com.zaga.handler.LogCommandHandler;
import com.zaga.repo.LogCommandRepo;

import jakarta.inject.Inject;

public class LogConsumerService {
    
      @Inject
      private LogCommandHandler logCommandHandler;

      @Inject
      LogCommandRepo logCommandRepo;
       
      @Incoming("logs-audit-in")
      public void consumeLogDetails(OtelLog logs) {
        System.out.println("consumed log -----------");
      logCommandRepo.persist(logs);
     }

     @Incoming("logs-in")
     public void consumeLogDTODetails(OtelLog logs) {
       System.out.println("consumed logDTO -----------");
      logCommandHandler.marshalLogData(logs);
    }
}

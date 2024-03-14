package com.zaga.kafka.consumer;


import org.eclipse.microprofile.reactive.messaging.Incoming;

import com.zaga.entity.oteltrace.OtelTrace;
import com.zaga.entity.queryentity.trace.TraceDTO;
import com.zaga.handler.TraceCommandHandler;
import com.zaga.repo.TraceCommandRepo;

import jakarta.inject.Inject;

public class TraceConsumerService {

    @Inject
    TraceCommandHandler traceCommandHandler;

    @Inject
    TraceCommandRepo traceCommandRepo;
    

    @Incoming("trace-audit-in")
    public void consumeTraceDetails(OtelTrace trace) {
        System.out.println("consumed trace ------------------");
        System.out.println("------------"+trace);
        traceCommandRepo.persist(trace);
    }


    @Incoming("trace-in")
    public void consumeTraceDTODetails(OtelTrace trace) {
        System.out.println("------------------[OTEL TRACE DTO] ------------------");
        traceCommandHandler.extractAndMapData(trace);
        System.out.println("--------------------[OTEL TRACE DTO END]------------");
    }
}
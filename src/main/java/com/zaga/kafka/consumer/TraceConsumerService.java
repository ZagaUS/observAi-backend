package com.zaga.kafka.consumer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.reactive.messaging.Incoming;

import com.zaga.entity.auth.AlertPayload;
import com.zaga.entity.auth.Rule;
import com.zaga.entity.auth.ServiceListNew;
import com.zaga.entity.oteltrace.OtelTrace;
import com.zaga.entity.queryentity.trace.TraceDTO;
import com.zaga.handler.TraceCommandHandler;
import com.zaga.kafka.alertProducer.AlertProducer;
import com.zaga.kafka.websocket.WebsocketAlertProducer;
import com.zaga.repo.ServiceListRepo;
import com.zaga.repo.TraceCommandRepo;

import jakarta.inject.Inject;
import jakarta.websocket.EncodeException;

public class TraceConsumerService {

    @Inject
    WebsocketAlertProducer sessions;

    @Inject
    TraceCommandHandler traceCommandHandler;

    @Inject
    TraceCommandRepo traceCommandRepo;

    @Inject
    ServiceListRepo serviceListRepo;

    @Inject
    AlertProducer alertProducer;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    @Incoming("trace-audit-in")
    public void consumeTraceDetails(OtelTrace trace) {
        System.out.println("consumed trace ------------------");
        System.out.println("------------" + trace);
        traceCommandRepo.persist(trace);
    }

    @Incoming("trace-in")
    public void consumeTraceDTODetails(OtelTrace trace) {
        System.out.println("------------------[OTEL TRACE DTO] ------------------");
        List<TraceDTO> traceList = traceCommandHandler.extractAndMapData(trace);
        System.out.println("--------------------[OTEL TRACE DTO END]------------");
        System.out.println("--------------------[OTEL TRACE RULE CREATION - START]------------");
        if (traceList != null) {
            if (traceList.size() > 0) {
                ServiceListNew serviceListNew = new ServiceListNew();
                for (TraceDTO traceDTO : traceList) {
                    try {
                        serviceListNew = serviceListRepo.find("serviceName = ?1", traceDTO.getServiceName())
                                .firstResult();
                        for (Rule ruleData : serviceListNew.getRules()) {
                            if ("trace".equals(ruleData.getRuleType())) {
                                LocalDateTime startDate = ruleData.getStartDateTime();
                                LocalDateTime expiryDate = ruleData.getExpiryDateTime();
                                if (startDate != null && expiryDate != null) {
                                    validateAndSendAlert(startDate, expiryDate, traceDTO, ruleData);
                                }
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("ERROR INCOMING TRACE-IN EXCEPTION   " + e.getMessage());
                    }
                }
            }
        }
        System.out.println("--------------------[OTEL TRACE RULE CREATION - END]------------");
    }

    public void validateAndSendAlert(LocalDateTime startDate, LocalDateTime expiryDate, TraceDTO traceDTO,
            Rule ruleData) {
        // this method with will check the alert condition and send it through websocket

        String startDateTimeString = startDate.format(FORMATTER);
        String expiryDateTimeString = expiryDate.format(FORMATTER);

        LocalDateTime currentDateTime = LocalDateTime.now();

        LocalDateTime startDateTime = LocalDateTime.parse(startDateTimeString, FORMATTER);
        ruleData.setStartDateTime(startDateTime);

        LocalDateTime expiryDateTime = LocalDateTime.parse(expiryDateTimeString, FORMATTER);
        ruleData.setExpiryDateTime(expiryDateTime);

        Long duration = traceDTO.getDuration();
        if (duration != null && duration != 0) {
            boolean isDurationViolation = false;
            long durationLimit = ruleData.getDuration();
            String durationConstraint = ruleData.getDurationConstraint();

            switch (durationConstraint) {
                case "greaterThan":
                    isDurationViolation = duration > durationLimit;
                    break;
                case "lessThan":
                    isDurationViolation = duration < durationLimit;
                    break;
                case "greaterThanOrEqual":
                    isDurationViolation = duration >= durationLimit;
                    break;
                case "lessThanOrEqual":
                    isDurationViolation = duration <= durationLimit;
                    break;
            }

            if (isDurationViolation && currentDateTime.isAfter(startDateTime)
                    && currentDateTime.isBefore(expiryDateTime)) {
                String serviceName = traceDTO.getServiceName();

                System.out.println(ruleData.getTracecAlertSeverityText() + " - Duration " + traceDTO.getDuration()
                        + " exceeded for this service: " + serviceName + "at" + traceDTO.getCreatedTime());

                sendAlert(new HashMap<>(),
                        ruleData.getTracecAlertSeverityText() + " - Duration " + traceDTO.getDuration()
                                + " exceeded for this service: " + serviceName + " at" + traceDTO.getCreatedTime());
                String traceAlertMessage = ruleData.getTracecAlertSeverityText() + " - Duration "
                        + traceDTO.getDuration()
                        + " exceeded for this service: " + serviceName + " at" + traceDTO.getCreatedTime();
                AlertPayload alertTracePayload = new AlertPayload();

                alertTracePayload.setServiceName(serviceName);
                alertTracePayload.setAlertMessage(traceAlertMessage);
                alertTracePayload.setTraceId(traceDTO.getTraceId());
                alertTracePayload.setCreatedTime(traceDTO.getCreatedTime());
                alertTracePayload.setType(ruleData.getRuleType());

                alertProducer.kafkaSend(alertTracePayload);

            }
        }
    }

    public void sendAlert(Map<String, String> alertPayload, String message) {
        alertPayload.put("alertMessage", message);
        alertPayload.put("alertType", "trace");
        sessions.getSessions().forEach(session -> {
            try {
                if (session == null) {
                    System.out.println("No session");
                } else {
                    session.getBasicRemote().sendObject(alertPayload);
                    System.out.println(message);
                }
            } catch (IOException | EncodeException e) {
                e.printStackTrace();
            }
        });
    }
}
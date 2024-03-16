package com.zaga.kafka.consumer;


import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.reactive.messaging.Incoming;

import com.zaga.entity.auth.Rule;
import com.zaga.entity.auth.ServiceListNew;
import com.zaga.entity.otellog.OtelLog;
import com.zaga.entity.queryentity.log.LogDTO;
import com.zaga.handler.LogCommandHandler;
import com.zaga.kafka.alertProducer.AlertProducer;
import com.zaga.kafka.websocket.WebsocketAlertProducer;
import com.zaga.repo.LogCommandRepo;
import com.zaga.repo.ServiceListRepo;

import io.vertx.core.Vertx;
import jakarta.inject.Inject;
import jakarta.websocket.EncodeException;

public class LogConsumerService {
    
      @Inject
      private LogCommandHandler logCommandHandler;

      @Inject
      LogCommandRepo logCommandRepo;

        @Inject
  private WebsocketAlertProducer sessions;

  @Inject
  ServiceListRepo serviceListRepo;

  @Inject
  AlertProducer alertLogProducer;

 

  private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(
    "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
  );
       
      @Incoming("logs-in")
      public void consumeLogDetails(OtelLog logs) {
        System.out.println("consumed log -----------");
      logCommandRepo.persist(logs);
     }

     @Incoming("logs-audit-in")
     public void consumeLogDTODetails(OtelLog logs) {
       System.out.println("consumed logDTO -----------");
      List<LogDTO> logDTOs = logCommandHandler.marshalLogData(logs);

      System.out.println("-----------------------------------------------");
       System.out.println("log sizes" + logDTOs.size());

    ServiceListNew serviceListNew = new ServiceListNew();
    for (LogDTO logDTOSingle : logDTOs) {
        try {
            System.out.println("The log service rule getting from database");
            serviceListNew = serviceListRepo.find("serviceName = ?1", logDTOSingle.getServiceName())

                    .firstResult();
                                    System.out.println("The log service rule gotten from database");
            break;
        } catch (Exception e) {
            System.out.println("ERROR " + e.getLocalizedMessage());
        }
    }

    System.out.println("Log DTO size " + logDTOs.size());

    if (!serviceListNew.equals(null)) {
        System.out.println("The log rule is entered");
        for (LogDTO logDto : logDTOs) {
            System.out.println("Log DTO's " + logDto);
            processRuleManipulation(logDto, serviceListNew);
        }
    }
  }
    public void processRuleManipulation(LogDTO logDTO, ServiceListNew serviceListNew) {
      LocalDateTime currentDateTime = LocalDateTime.now();
  
      try {
          if (!serviceListNew.getRules().isEmpty()) {
              for (Rule sData : serviceListNew.getRules()) {
                  if ("log".equals(sData.getRuleType())) {
                      LocalDateTime startDate = sData.getStartDateTime();
                      LocalDateTime expiryDate = sData.getExpiryDateTime();
  
                      if (startDate != null && expiryDate != null) {
                          String startDateTimeString = startDate.format(FORMATTER);
                          String expiryDateTimeString = expiryDate.format(FORMATTER);
  
                          LocalDateTime startDateTime = LocalDateTime.parse(startDateTimeString, FORMATTER);
                          sData.setStartDateTime(startDateTime);
  
                          LocalDateTime expiryDateTime = LocalDateTime.parse(expiryDateTimeString, FORMATTER);
                          sData.setExpiryDateTime(expiryDateTime);
  
                          String severityText = logDTO.getSeverityText();
                          // String traceId = logDTO.getTraceId();
                          System.out.println("------------------------Log Severity " + severityText);
  
                          if (severityText != null && !severityText.isEmpty()) {
                            boolean isSeverityViolation = false;
                            List<String> severityPresent = sData.getSeverityText();
                            String severityConstraint = sData.getSeverityConstraint();
                        
                            switch (severityConstraint) {
                                case "present":
                                    isSeverityViolation = severityPresent.contains(severityText);
                                    break;
                                case "notpresent":
                                    isSeverityViolation = !severityPresent.contains(severityText);
                                    break;
                                default:
                                    break;
                            }
                        
  
                              if (isSeverityViolation && currentDateTime.isAfter(startDateTime) && currentDateTime.isBefore(expiryDateTime)) {
                                  String serviceName = logDTO.getServiceName();
  
                                  // List<String> severityTextList = sData.getSeverityText();
                                  // String logSeverityText = logDTO.getSeverityText();
  
                                  // if (severityTextList.contains(logSeverityText)) {
                                  //     System.out.println("Log severity text matched");
                                      // Optionally send the alert here or perform other actions based on severity
                                      System.out.println(sData.getLogAlertSeverityText() + " - Log call exceeded for this service: " + serviceName + " at " + logDTO.getCreatedTime());
                                      sendAlert(new HashMap<>(), sData.getLogAlertSeverityText() + " - Log call exceeded for this service: " + serviceName + " at " + logDTO.getCreatedTime());
                                  // } else {
                                  //     System.out.println("No alert received");
                                  // }
                              } else {
                                  System.out.println("No Alert received for service ");
                              }
                          }
                      }
                  }
              }
          }
      } catch (Exception e) {
          System.out.println("ERROR " + e.getLocalizedMessage());
      }
  }
  
    private void sendAlert(Map<String, String> alertPayload, String message) {
      alertPayload.put("alertMessage", message);
      alertPayload.put("alertType", "log");
      sessions
        .getSessions()
        .forEach(session -> {
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


package com.zaga.kafka.consumer;

import com.zaga.entity.auth.AlertPayload;
import com.zaga.entity.auth.Rule;
import com.zaga.entity.auth.ServiceListNew;
import com.zaga.entity.otelmetric.OtelMetric;
import com.zaga.entity.queryentity.metrics.MetricDTO;
import com.zaga.handler.MetricCommandHandler;
import com.zaga.kafka.alertProducer.AlertProducer;
import com.zaga.kafka.websocket.WebsocketAlertProducer;
import com.zaga.repo.MetricCommandRepo;
import com.zaga.repo.ServiceListRepo;

import jakarta.inject.Inject;
import jakarta.websocket.EncodeException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.reactive.messaging.Incoming;

public class MetricConsumerService {

  @Inject
  MetricCommandHandler metricCommandHandler;

  @Inject
  MetricCommandRepo metricCommandRepo;

   @Inject
    private WebsocketAlertProducer sessions;

    @Inject
    ServiceListRepo serviceListRepo;

    @Inject
    AlertProducer metricAlertProducer;

     private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

  @Incoming("metric-audit-in")
  public void consumeMetricDetails(OtelMetric metrics) {
    System.out.println("consumed metric data----------------");
    if (metrics != null) {
      metricCommandRepo.persist(metrics);
    } else {
      System.out.println("Received null message. Check serialization/deserialization.");
    }

  }

    @Incoming("metric-in")
    public void consumeMetricDTODetails(OtelMetric metrics) {
      System.out.println("consumed metricDTO data----------------");
      if (metrics != null) {
         List<MetricDTO> metricDTOs =metricCommandHandler.extractAndMapData(metrics);
         System.out.println("------------------------------------");
        ServiceListNew serviceListData1 = new ServiceListNew();
        for (MetricDTO metricDTOSingle : metricDTOs) {
            System.out.println("The metric rule fetching from the data base");
            serviceListData1 = serviceListRepo.find("serviceName = ?1", metricDTOSingle.getServiceName()).firstResult();

            System.out.println("The metric rule fetched from the data base"+serviceListData1);
            break;
        }
        for (MetricDTO metricDTO : metricDTOs) {
            System.out.println("The Process rule entered");
            processRuleManipulation(metricDTO, serviceListData1);
        }
        System.out.println("---------MetricDTOs:---------- " + metricDTOs.size());
      } else {
        System.out.println("Received null message. Check serialization/deserialization.");
      }

  }



   public void processRuleManipulation(MetricDTO metricDTO, ServiceListNew serviceListData) {
        LocalDateTime currentDateTime = LocalDateTime.now();
        try {
            if (!serviceListData.getRules().isEmpty()) {
                for (Rule sData : serviceListData.getRules()) {
                    if ("metric".equals(sData.getRuleType())) {
                        LocalDateTime startDate = sData.getStartDateTime();
                        LocalDateTime expiryDate = sData.getExpiryDateTime();
                        if (startDate != null && expiryDate != null) {
                            String startDateTimeString = startDate.format(FORMATTER);
                            String expiryDateTimeString = expiryDate.format(FORMATTER);

                            LocalDateTime startDateTime = LocalDateTime.parse(startDateTimeString, FORMATTER);
                            sData.setStartDateTime(startDateTime);

                            LocalDateTime expiryDateTime = LocalDateTime.parse(expiryDateTimeString, FORMATTER);
                            sData.setExpiryDateTime(expiryDateTime);
                            Double cpuLimit = sData.getCpuLimit();
                            Double cpuUsage = metricDTO.getCpuUsage();
                            Double cpuLimitMilliCores = cpuLimit * 1000;
                            Integer memoryUsage = metricDTO.getMemoryUsage();

                            Map<String, String> alertPayload = new HashMap<>();

                            if (cpuUsage != null && memoryUsage != null && cpuUsage != 0 && memoryUsage != 0) {
                                boolean isCpuViolation = false;
                                boolean isMemoryViolation = false;
                                // double cpuLimit = sData.getCpuLimit();
                                Integer memoryLimit = sData.getMemoryLimit();
                                String memoryConstraint = sData.getMemoryConstraint();
                                String cpuConstraint = sData.getCpuConstraint();
                            
                                switch (cpuConstraint) {
                                    case "greaterThan":
                                        isCpuViolation = cpuUsage > cpuLimitMilliCores;
                                        break;
                                    case "lessThan":
                                        isCpuViolation = cpuUsage < cpuLimitMilliCores;
                                        break;
                                    case "greaterThanOrEqual":
                                        isCpuViolation = cpuUsage >= cpuLimitMilliCores;
                                        break;
                                    case "lessThanOrEqual":
                                        isCpuViolation = cpuUsage <= cpuLimitMilliCores;
                                        break;
                                }
                            
                                switch (memoryConstraint) {
                                    case "greaterThan":
                                        isMemoryViolation = memoryUsage > memoryLimit;
                                        break;
                                    case "lessThan":
                                        isMemoryViolation = memoryUsage < memoryLimit;
                                        break;
                                    case "greaterThanOrEqual":
                                        isMemoryViolation = memoryUsage >= memoryLimit;
                                        break;
                                    case "lessThanOrEqual":
                                        isMemoryViolation = memoryUsage <= memoryLimit;
                                        break;
                                }
                                
                                AlertPayload alertPayload2 = new AlertPayload();

                                if (isCpuViolation && currentDateTime.isAfter(startDateTime) && currentDateTime.isBefore(expiryDateTime)) {
                                    // System.out.println("OUT");
                                    // String cpuSeverity = calculateSeverity(cpuUsage, cpuLimitMilliCores);
                                    System.out.println(sData.getCpuAlertSeverityText() + " - CPU Usage " + Math.ceil(cpuUsage) + " peaked in this service " + metricDTO.getServiceName());
                                    sendAlert(alertPayload,"" + sData.getCpuAlertSeverityText() + "- CPU Usage " + Math.ceil(cpuLimitMilliCores)
                                            + "  peaked in this service " + metricDTO.getServiceName());
                                    System.out.println("peaked in this service------------ " + alertPayload);
                                    System.out.println("--------------------------"+metricDTO.getServiceName()+metricDTO.getCpuUsage()+metricDTO.getMemoryUsage());
                                    String cpuAlertMessage = sData.getCpuAlertSeverityText() + "- CPU Usage " + Math.ceil(cpuUsage) + " peaked in this service " + metricDTO.getServiceName();

                                    alertPayload2.setServiceName(metricDTO.getServiceName());
                                    alertPayload2.setCreatedTime(metricDTO.getDate());
                                    alertPayload2.setType(sData.getRuleType());
                                    alertPayload2.setAlertMessage(cpuAlertMessage);
                                    metricAlertProducer.kafkaSend(alertPayload2);
                                }
                            
                                if (isMemoryViolation && currentDateTime.isAfter(startDateTime) && currentDateTime.isBefore(expiryDateTime)) {
                                    // System.out.println("OUT");
                                    // String memorySeverity = calculateSeverity(memoryUsage, memoryLimit);
                                sendAlert(alertPayload,"" + sData.getMemoryAlertSeverityText() + " - Memory Usage " + memoryUsage + " peaked in this service "
                                            + metricDTO.getServiceName() + "at" + metricDTO.getDate());
                                    System.out.println(sData.getMemoryAlertSeverityText() + " Alert - Memory Usage " + memoryUsage + " peaked in this service " + metricDTO.getServiceName());
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
        alertPayload.put("alertType", "metric");
        sessions.getSessions().forEach(session -> {
            try {
                if (session == null) {
                    System.out.println("No session");
                } else {
                    System.out.println("Message sent to session " + session);
                    session.getBasicRemote().sendObject(alertPayload);
                    System.out.println("Message Metric sent");
                }
            } catch (IOException | EncodeException e) {
                e.printStackTrace();
            }
        });
    }

  public static void main(String[] args) {
    // Gson gson = new Gson();

    // File file = new File("./src/main/java/com/zaga/kafka/consumer/MetricSample.json");

    // try (Reader reader1 = new FileReader(file)) {

    //   OtelMetric apmMetric = gson.fromJson(reader1, OtelMetric.class);

    //   MetricCommandHandler metricCommandHandler = new MetricCommandHandler();

    //   List<MetricDTO> apmMetricDTOs = metricCommandHandler.extractAndMapData(apmMetric);

    //   System.out.println("---------------------------------MAIN METHOD------------------------------");

    //   System.out.println(apmMetricDTOs.size());

    //   for (MetricDTO metricDTO : apmMetricDTOs) {
    //     // System.out.println("Metric DTOS " + metricDTO.toString());
    //     metricCommandHandler.processRuleManipulation(metricDTO);
    //     // break;
    //   }

    // } catch (IOException e) {
    //   System.out.println("ERROR " + e.getLocalizedMessage());
    // }
  }

}

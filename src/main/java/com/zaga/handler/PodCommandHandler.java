package com.zaga.handler;

import com.zaga.entity.pod.OtelPodMetric;
import com.zaga.entity.pod.ResourceMetric;
import com.zaga.entity.pod.ScopeMetrics;
import com.zaga.entity.pod.scopeMetric.Metric;
import com.zaga.entity.pod.scopeMetric.gauge.Gauge;
import com.zaga.entity.pod.scopeMetric.gauge.GaugeDataPoint;
import com.zaga.entity.queryentity.pod.PodMetricDTO;
import com.zaga.repo.PodCommandRepo;
import com.zaga.repo.PodMetricDTORepo;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@ApplicationScoped
public class PodCommandHandler {

  @Inject
  PodCommandRepo podCommandRepo;

  @Inject
  PodMetricDTORepo podMetricDTORepo;

  public void createPodMetric(OtelPodMetric metrics) {
    System.out.println("----infra pod metrics--------"+metrics);
    podCommandRepo.persist(metrics);

    List<PodMetricDTO> metricDTOs = extractAndMapData(metrics);
    System.out.println("------------------------------------------Infra PODMetricDTOs:-------------------------------------- " + metricDTOs.size());
  }


public List<PodMetricDTO> extractAndMapData(OtelPodMetric metrics) {
  List<PodMetricDTO> podMetricsList = new ArrayList<>();

  try {
      for (ResourceMetric resourceMetric : metrics.getResourceMetrics()) {
          String podName = getPodName(resourceMetric);
          String namespaceName = getNamespaceName(resourceMetric);

          if (podName != null) {
              PodMetricDTO podMetricDTO = new PodMetricDTO();
              podMetricDTO.setPodName(podName);
              podMetricDTO.setNamespaceName(namespaceName);

              for (ScopeMetrics scopeMetric : resourceMetric.getScopeMetrics()) {
                  Date createdTime = null;
                  Double cpuUsage = null;
                  Long memoryUsage = null;

                  String name = scopeMetric.getScope().getName();

                  if (name != null && name.contains("otelcol/kubeletstatsreceiver")) {
                      List<Metric> metricsList = scopeMetric.getMetrics();

                      for (Metric metric : metricsList) {
                          String metricName = metric.getName();
                          Gauge metricGauge = metric.getGauge();

                          if (metricGauge != null) {
                              List<GaugeDataPoint> gaugeDataPoints = metricGauge.getDataPoints();

                              for (GaugeDataPoint gaugeDataPoint : gaugeDataPoints) {
                                  String startTimeUnixNano = gaugeDataPoint.getTimeUnixNano();
                                  createdTime = convertUnixNanoToLocalDateTime(startTimeUnixNano);
                              }
                          }

                          if ("k8s.pod.cpu.utilization".equals(metricName) && metricGauge != null) {
                              List<GaugeDataPoint> dataPoints = metricGauge.getDataPoints();
                              if (dataPoints != null && !dataPoints.isEmpty()) {
                                  cpuUsage = dataPoints.get(0).getAsDouble();
                              }
                          }

                        //   if ("k8s.pod.memory.usage".equals(metricName) && metricGauge != null) {
                        //     List<GaugeDataPoint> dataPoints = metricGauge.getDataPoints();
                        //     if (dataPoints != null && !dataPoints.isEmpty()) {
                        //         memoryUsage = Long.parseLong(dataPoints.get(0).getAsInt());
                        //     }
                        // }

                      //   if ("k8s.pod.memory.usage".equals(metricName) && metricGauge != null) {
                      //     List<GaugeDataPoint> dataPoints = metricGauge.getDataPoints();
                      //     if (dataPoints != null && !dataPoints.isEmpty()) {
                      //         memoryUsage = Long.parseLong(dataPoints.get(0).getAsInt());
                      //         memoryUsage = memoryUsage / (1024 * 1024);
                      //     }
                      // }

                      if ("k8s.pod.memory.usage".equals(metricName) && metricGauge != null) {
                        List<GaugeDataPoint> dataPoints = metricGauge.getDataPoints();
                        if (dataPoints != null && !dataPoints.isEmpty()) {
                            String memoryUsageAsString = dataPoints.get(0).getAsInt();
                            if (memoryUsageAsString != null) {
                                memoryUsage = Long.parseLong(memoryUsageAsString);
                                // Convert memory usage from bytes to MB
                                memoryUsage = memoryUsage / (1024 * 1024);
                            }
                        }
                    }
                    


                      }
                  }

                  podMetricDTO.setDate(createdTime != null ? createdTime : new Date());
                  podMetricDTO.setCpuUsage(cpuUsage != null ? cpuUsage : 0.0);
                  System.out.println("----createdTime-----"+createdTime);
                  System.out.println("cpu usage-----------"+cpuUsage);
                  podMetricDTO.setMemoryUsage(memoryUsage != null ? memoryUsage : 0L);
                  System.out.println("----memoryUsage----"+memoryUsage);
              }

              podMetricsList.add(podMetricDTO);
          }
      }

      podMetricDTORepo.persist(podMetricsList);
      System.out.println("Aggregated-----------------------" + podMetricsList.size());

  } catch (Exception e) {
      e.printStackTrace();
  }

  return podMetricsList;
}


 
//   public List<PodMetricDTO> extractAndMapData(OtelPodMetric metrics) {
//     List<PodMetricDTO> podMetricDTOs = new ArrayList<>();

//     try {
//         Map<String, PodMetricDTO> podMetricsMap = new HashMap<>();

//         for (ResourceMetric resourceMetric : metrics.getResourceMetrics()) {
//             String podName = getPodName(resourceMetric);
//             if (podName != null) {
//                 PodMetricDTO podMetricDTO = podMetricsMap.computeIfAbsent(podName, k -> new PodMetricDTO());
//                 podMetricDTO.setPodName(podName);

//                 List<MetricDTO> metricDTOs = podMetricDTO.getMetrics();

//                 for (ScopeMetrics scopeMetric : resourceMetric.getScopeMetrics()) {
//                     Date createdTime = null;
//                     Double cpuUsage = null;
//                     Long memoryUsage = 0L;

//                     String name = scopeMetric.getScope().getName();

//                     if (name != null && name.contains("otelcol/kubeletstatsreceiver")) {
//                         List<Metric> metricsList = scopeMetric.getMetrics();

//                         for (Metric metric : metricsList) {
//                             String metricName = metric.getName();

//                             if (metric.getGauge() != null) {
//                                 Gauge metricGauge = metric.getGauge();
//                                 List<GaugeDataPoint> gaugeDataPoints = metricGauge.getDataPoints();

//                                 for (GaugeDataPoint gaugeDataPoint : gaugeDataPoints) {
//                                     String startTimeUnixNano = gaugeDataPoint.getTimeUnixNano();
//                                     createdTime = convertUnixNanoToLocalDateTime(startTimeUnixNano);

//                                     if (isCpuMetric(metricName)) {
//                                         cpuUsage = gaugeDataPoint.getAsDouble();
//                                     }

//                                     // Assuming the memory metric is also present in GaugeDataPoint, adjust as needed
//                                     String memoryValue = gaugeDataPoint.getAsInt();
//                                     if (isMemoryMetric(metricName)) {
//                                         long currentMemoryUsage = Long.parseLong(memoryValue);
//                                         memoryUsage += currentMemoryUsage;
//                                     }
//                                 }
//                             }

//                             Long memoryUsageInMb = memoryUsage / (1024 * 1024);

//                             MetricDTO metricDTO = new MetricDTO();
//                             metricDTO.setDate(createdTime);
//                             metricDTO.setMemoryUsage(memoryUsageInMb);
//                             metricDTO.setCpuUsage(cpuUsage);

//                             metricDTOs.add(metricDTO);
//                         }
//                     }
//                 }
//             }
//         }

//         podMetricDTOs.addAll(podMetricsMap.values());
//         podMetricDTORepo.persist(podMetricDTOs);
//     } catch (Exception e) {
//         e.printStackTrace(); // Handle the exception appropriately
//     }

//     return podMetricDTOs;
// }
 



// s
  //     private boolean isSupportedMetric(String metricName) {
  //     return Set.of(
  //         "otelcol/kubeletstatsreceiver").contains(metricName);
  // }

  private boolean isMemoryMetric(String metricName) {
    return Set.of("k8s.pod.memory.usage").contains(metricName);
  }

  private boolean isCpuMetric(String metricName) {
    return Set.of("k8s.pod.cpu.utilization").contains(metricName);
  }
  
  private String getNamespaceName(ResourceMetric resourceMetric) {
    return resourceMetric
      .getResource()
      .getAttributes()
      .stream()
      .filter(attribute -> "k8s.namespace.name".equals(attribute.getKey()))
      .findFirst()
      .map(attribute -> attribute.getValue().getStringValue())
      .orElse(null);
  }

  private String getPodName(ResourceMetric resourceMetric) {
    return resourceMetric
      .getResource()
      .getAttributes()
      .stream()
      .filter(attribute -> "k8s.pod.name".equals(attribute.getKey()))
      .findFirst()
      .map(attribute -> attribute.getValue().getStringValue())
      .orElse(null);
  }

  private static Date convertUnixNanoToLocalDateTime(String startTimeUnixNano) {
    long observedTimeMillis = Long.parseLong(startTimeUnixNano) / 1_000_000;

    Instant instant = Instant.ofEpochMilli(observedTimeMillis);

    ZoneId istZone = ZoneId.of("Asia/Kolkata");
    LocalDateTime istDateTime = LocalDateTime.ofInstant(instant, istZone);

    return Date.from(istDateTime.atZone(istZone).toInstant());
  }
}

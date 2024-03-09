package com.zaga.handler;

import com.zaga.entity.node.OtelNode;
import com.zaga.entity.node.ResourceMetrics;
import com.zaga.entity.node.ScopeMetric;
import com.zaga.entity.node.scopeMetrics.Metrics;
import com.zaga.entity.node.scopeMetrics.gauge.Gauge;
import com.zaga.entity.node.scopeMetrics.gauge.GaugeDataPoints;
import com.zaga.entity.queryentity.node.NodeMetricDTO;
import com.zaga.repo.NodeDTORepo;
import com.zaga.repo.NodeMetricRepo;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@ApplicationScoped
public class NodeCommandHandler {

    @Inject
    NodeMetricRepo nodeCommandRepo;

    @Inject
    NodeDTORepo nodeMetricDTORepo;

    public void createNodeMetric(OtelNode metrics) {
        System.out.println("------Infra Node Metrics-------"+ metrics);
        nodeCommandRepo.persist(metrics);

        List<NodeMetricDTO> metricDTOs = extractAndMapNodeData(metrics);
        System.out.println("------------------------------------------Infra NodeMetricDTOs:-------------------------------------- " + metricDTOs.size());
    }

    public List<NodeMetricDTO> extractAndMapNodeData(OtelNode metrics) {
        List<NodeMetricDTO> metricDTOs = new ArrayList<>();
    
        try {
            for (ResourceMetrics resourceMetric : metrics.getResourceMetrics()) {
                String nodeName = getNodeName(resourceMetric);
                if (nodeName != null) {
                    // NodeMetricDTO nodeMetricDTO = new NodeMetricDTO();
                  
    
                    for (ScopeMetric scopeMetric : resourceMetric.getScopeMetrics()) {
                        Date createdTime = null;
                        Double cpuUsage = null;
                        Long memoryUsage = 0L;
    
                        String name = scopeMetric.getScope().getName();
    
                        if (name != null && name.contains("otelcol/kubeletstatsreceiver")) {
                            List<Metrics> metricsList = scopeMetric.getMetrics();
    
                            for (Metrics metric : metricsList) {
                                String metricName = metric.getName();
    
                                if (metric.getGauge() != null) {
                                    Gauge metricGauge = metric.getGauge();
                                    List<GaugeDataPoints> gaugeDataPoints = metricGauge.getDataPoints();
    
                                    for (GaugeDataPoints gaugeDataPoint : gaugeDataPoints) {
                                        String startTimeUnixNano = gaugeDataPoint.getTimeUnixNano();
                                        createdTime = convertUnixNanoToLocalDateTime(startTimeUnixNano);
    
                                        // if (isCpuMetric(metricName)) {
                                        //     cpuUsage = gaugeDataPoint.getAsDouble();
                                        // }
                                        if ("k8s.node.cpu.usage".equals(metricName) && metric.getGauge() != null) {
                                            List<GaugeDataPoints> dataPoints = metric.getGauge().getDataPoints();
                                            if (dataPoints != null && !dataPoints.isEmpty()) {
                                                cpuUsage = dataPoints.get(0).getAsDouble();
                                            }
                                        }

                                        if ("k8s.node.memory.usage".equals(metricName) && metric.getGauge() != null) {
                                            List<GaugeDataPoints> dataPoints = metric.getGauge().getDataPoints();
                                            if (dataPoints != null && !dataPoints.isEmpty()) {
                                                String memoryUsageAsString = dataPoints.get(0).getAsInt();
                                                if (memoryUsageAsString != null) {
                                                    memoryUsage = Long.parseLong(memoryUsageAsString);
                                                    // Convert memory usage from bytes to MB
                                                    memoryUsage = memoryUsage / (1024 * 1024);
                                                }
                                            }
                                        }
                                        // Assuming the memory metric is also present in GaugeDataPoint, adjust as needed
                                        // String memoryValue = gaugeDataPoint.getAsInt();
                                        // if (isMemoryMetric(metricName)) {
                                        //     long currentMemoryUsage = Long.parseLong(memoryValue);
                                        //     memoryUsage += currentMemoryUsage;
                                        // }
                                    }
                                }
                            }
                        }
    
                        NodeMetricDTO metricDTO = new NodeMetricDTO();
                        metricDTO.setNodeName(nodeName);
                        metricDTO.setDate(createdTime != null ? createdTime : new Date());
                        metricDTO.setMemoryUsage(memoryUsage);
                        metricDTO.setCpuUsage(cpuUsage != null ? cpuUsage : 0.0);
    
                        metricDTOs.add(metricDTO);
                        
                    }
                }
            }
            nodeMetricDTORepo.persist(metricDTOs);
    
        } catch (Exception e) {
            e.printStackTrace();
        }
    
        return metricDTOs;
    }
    private boolean isMemoryMetric(String metricName) {
        return Set.of("k8s.node.memory.usage").contains(metricName);
    }

    private boolean isCpuMetric(String metricName) {
        return Set.of("k8s.node.cpu.utilization").contains(metricName);
    }

    private String getNodeName(ResourceMetrics resourceMetric) {
        return resourceMetric
                .getResource()
                .getAttributes()
                .stream()
                .filter(attribute -> "k8s.node.name".equals(attribute.getKey()))
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

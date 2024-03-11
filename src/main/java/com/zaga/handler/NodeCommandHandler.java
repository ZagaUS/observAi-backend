package com.zaga.handler;

import com.zaga.entity.clusterutilization.OtelClusterUutilization;
import com.zaga.entity.clusterutilization.ResourceMetric;
import com.zaga.entity.clusterutilization.ScopeMetric;
import com.zaga.entity.clusterutilization.scopeMetric.Metric;
import com.zaga.entity.clusterutilization.scopeMetric.MetricGauge;
import com.zaga.entity.clusterutilization.scopeMetric.gauge.GaugeDataPoint;
import com.zaga.entity.queryentity.node.NodeMetricDTO;
import com.zaga.repo.ClusterUtilizationRepo;
import com.zaga.repo.NodeDTORepo;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@ApplicationScoped
public class NodeCommandHandler {

    @Inject
    ClusterUtilizationRepo cluster_utilizationRepo;

    @Inject
    NodeDTORepo nodeMetricDTORepo;
    

    public void createNodeMetric(OtelClusterUutilization cluster_utilization) {
        System.out.println("------Infra Node Metrics-------"+ cluster_utilization);
        cluster_utilizationRepo.persist(cluster_utilization);

        List<NodeMetricDTO> metricDTOs = extractAndMapNodeData(cluster_utilization);
        System.out.println("------------------------------------------Infra NodeMetricDTOs:-------------------------------------- " + metricDTOs.size());
    }

    public List<NodeMetricDTO> extractAndMapNodeData(OtelClusterUutilization metrics) {
        List<NodeMetricDTO> metricDTOs = new ArrayList<>();
    
        try {
            for (ResourceMetric resourceMetric : metrics.getResourceMetrics()) {
                String nodeName = getNodeName(resourceMetric);
                String clusterName = getClusterName(resourceMetric);
                if (nodeName != null) {
                    // NodeMetricDTO nodeMetricDTO = new NodeMetricDTO();
                  
    
                    for (ScopeMetric scopeMetric : resourceMetric.getScopeMetrics()) {
                        Date createdTime = null;
                        Double cpuUsage = null;
                        Long memoryUsage = 0L;
    
                        String name = scopeMetric.getScope().getName();
                        if (name != null && name.contains("otelcol/kubeletstatsreceiver")) {
                            List<Metric> metricsList = scopeMetric.getMetrics();
    
                            for (Metric metric : metricsList) {
                                String metricName = metric.getName();
    
                                if (metric.getGauge() != null) {
                                    MetricGauge metricGauge = metric.getGauge();
                                    List<GaugeDataPoint> gaugeDataPoints = metricGauge.getDataPoints();
    
                                    for (GaugeDataPoint gaugeDataPoint : gaugeDataPoints) {
                                        String startTimeUnixNano = gaugeDataPoint.getTimeUnixNano();
                                        createdTime = convertUnixNanoToLocalDateTime(startTimeUnixNano);
    
                                        if (isCpuMetric(metricName)) {
                                            String cpuData = gaugeDataPoint.getAsDouble();
                                            cpuUsage = Double.parseDouble(cpuData);
                                        } else if (isMemoryMetric(metricName)) {
                                            memoryUsage += Long.parseLong(gaugeDataPoint.getAsInt());
                                            memoryUsage = memoryUsage / (1024 * 1024);
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
                        metricDTO.setClusterName(clusterName);
                        System.out.println("------Node clusterName----"+clusterName);
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
        return metricName.equals("k8s.node.cpu.usage");
    }
    

    private String getClusterName(ResourceMetric resourceMetric) {
        return resourceMetric
                .getResource()
                .getAttributes()
                .stream()
                .filter(attribute -> "k8s.cluster.name".equals(attribute.getKey()))
                .findFirst()
                .map(attribute -> attribute.getValue().getStringValue())
                .orElse(null);
    }

    private String getNodeName(ResourceMetric resourceMetric) {
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

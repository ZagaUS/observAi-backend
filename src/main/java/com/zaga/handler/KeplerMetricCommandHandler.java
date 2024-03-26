package com.zaga.handler;

import com.zaga.entity.kepler.KeplerMetric;
import com.zaga.entity.kepler.ResourcekeplerMetric;
import com.zaga.entity.kepler.ScopeMetric;
import com.zaga.entity.kepler.scopeMetric.Metric;
import com.zaga.entity.kepler.scopeMetric.MetricGauge;
import com.zaga.entity.kepler.scopeMetric.MetricHistogram;
import com.zaga.entity.kepler.scopeMetric.MetricSum;
import com.zaga.entity.kepler.scopeMetric.gauge.GaugeDataPoint;
import com.zaga.entity.kepler.scopeMetric.gauge.GaugeDataPointAttribute;
import com.zaga.entity.kepler.scopeMetric.gauge.GaugeDataPointAttributeValue;
import com.zaga.entity.kepler.scopeMetric.histogram.HistogramDataPoint;
import com.zaga.entity.kepler.scopeMetric.histogram.HistogramDataPointAttribute;
import com.zaga.entity.kepler.scopeMetric.histogram.HistogramDataPointAttributeValue;
import com.zaga.entity.kepler.scopeMetric.sum.SumDataPoint;
import com.zaga.entity.kepler.scopeMetric.sum.SumDataPointAttribute;
import com.zaga.entity.kepler.scopeMetric.sum.SumDataPointAttributeValue;
import com.zaga.entity.queryentity.kepler.KeplerMetricDTO;
import com.zaga.entity.queryentity.kepler.Resource;
import com.zaga.repo.KeplerMetricDTORepo;
import com.zaga.repo.KeplerMetricRepo;

import io.vertx.core.Vertx;
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

@ApplicationScoped
public class KeplerMetricCommandHandler {

  @Inject
  KeplerMetricRepo keplerMetricRepo;

  @Inject
  KeplerMetricDTORepo KeplerMetricDTORepo;

  @Inject 
  Vertx vertx;

  public enum KeplerMetricsNames {
    kepler_container_joules_total("CONT1001"),
    kepler_container_core_joules_total("CONT1002"),
    kepler_container_dram_joules_total("CONT1003"),
    kepler_container_uncore_joules_total("CONT1004"),
    kepler_container_package_joules_total("CONT1005"),
    kepler_container_other_joules_total("CONT1006"),
    kepler_container_gpu_joules_total("CONT1007"),
    kepler_container_energy_stat("CONT1008"),
    kepler_node_core_joules_total("NODE2001"),
    kepler_node_uncore_joules_total("NODE2002"),
    kepler_node_dram_joules_total("NODE2003"),
    kepler_node_package_joules_total("NODE2004"),
    kepler_node_other_host_components_joules_total("HOST3001"),
    kepler_node_gpu_joules_total("NODE2006"),
    kepler_node_platform_joules_total("NODE2007"),
    kepler_node_energy_stat("NODE2008");

    String metricsName;

    KeplerMetricsNames(String metricsName) {
      this.metricsName = metricsName;
    }

    public String getMetricsName() {
      return metricsName;
    }
  }

  public enum KeplerTypeNames {
    kepler_container_joules_total("TOTAL"),
    kepler_container_core_joules_total("CORE"),
    kepler_container_dram_joules_total("DRAM"),
    kepler_container_uncore_joules_total("UNCORE"),
    kepler_container_package_joules_total("PKG"),
    kepler_container_other_joules_total("OTHER"),
    kepler_container_gpu_joules_total("GPU"),
    kepler_container_energy_stat("ENERGYSTAT"),
    kepler_node_core_joules_total("CORE"),
    kepler_node_uncore_joules_total("UNCORE"),
    kepler_node_dram_joules_total("DRAM"),
    kepler_node_package_joules_total("PKG"),
    kepler_node_other_host_components_joules_total("TOTAL"),
    kepler_node_gpu_joules_total("GPU"),
    kepler_node_platform_joules_total("TOTAL"),
    kepler_node_energy_stat("ENERGYSTAT"),;

    String KeplerType;

    KeplerTypeNames(String KeplerType) {
      this.KeplerType = KeplerType;
    }

    public String getKeplerType() {
      return KeplerType;
    }
  }


public void createKeplerMetric(KeplerMetric metric) {
  System.out.println("==========[KEPLER METRIC DTO MAPPING]==========");
  List<KeplerMetricDTO> metricDTOs = extractAndMapData(metric);

  if (!metricDTOs.isEmpty()) {

    System.out.println("keplerMetricDTO-------------"+metricDTOs.size());
    for (KeplerMetricDTO keplerMetDTO : metricDTOs) {
      KeplerMetricDTORepo.persist(keplerMetDTO);

    }
  }
}

  public List<KeplerMetricDTO> extractAndMapData(KeplerMetric keplerMetric) {
    List<KeplerMetricDTO> keplerMetricDTOLst = new ArrayList<>();

    List<ResourcekeplerMetric> resourceMetrics = keplerMetric.getResourceMetrics();

    for (ResourcekeplerMetric resourceMetric : resourceMetrics) {
      List<ScopeMetric> scopeMetrics = resourceMetric.getScopeMetrics();

      for (ScopeMetric scopeMetric : scopeMetrics) {
        List<Metric> metrics = scopeMetric.getMetrics();

        for (Metric metric : metrics) {
          String metricName = metric.getName();

          KeplerMetricsNames metricsEnum = null;
          // String typeName = metricsEnum.getMetricsName();

          try {
            metricsEnum = KeplerMetricsNames.valueOf(metricName);
          } catch (Exception ex) {
          }

          if (metricsEnum != null) {
            if (metric.getSum() != null) {
              keplerMetricDTOLst.addAll(
                  processSumMetric(metric.getSum(), metricName));
            } else if (metric.getGauge() != null) {
              keplerMetricDTOLst.addAll(
                  processGaugeMetric(metric.getGauge(), metricName));
            } else if (metric.getHistogram() != null) {
              keplerMetricDTOLst.addAll(
                  processHistogramMetric(metric.getHistogram(), metricName));
            }
          } else {
          }
        }
      }
    }

    return keplerMetricDTOLst;
  }

  private List<KeplerMetricDTO> processSumMetric(
      MetricSum metricSum,
      String metricName) {
    List<KeplerMetricDTO> keplerMetricDTOList = new ArrayList<>();

    List<SumDataPoint> sumDataPoints = metricSum.getDataPoints();
    String type = "";
    String kepType = "";

    for (KeplerMetricsNames metricsEnum : KeplerMetricsNames.values()) {
      String matchName = metricsEnum.toString();
      String typeName = metricsEnum.getMetricsName();

      if (matchName.equals(metricName)) {
        if (typeName.startsWith("CONT")) {
          // System.out.println("CONTAINER " + metricsEnum);
          type = "pod";
        } else if (typeName.startsWith("HOST")) {
          // System.out.println("HOST " + metricsEnum);
          type = "host";
        } else if (typeName.startsWith("NODE")) {
          // System.out.println("NODE " + metricsEnum);
          type = "node";
        }
        break;
      }
    }

    for (KeplerTypeNames keplerTypeEnum : KeplerTypeNames.values()) {
      String matchTypeName = keplerTypeEnum.toString();
      String keplerName = keplerTypeEnum.getKeplerType();

      if (matchTypeName.equals(metricName)) {
        if (keplerName.startsWith("DRAM")) {
          // keplerMetricDTO.setKeplerType("DRAM");
          kepType = "DRAM";
        } else if (keplerName.startsWith("CORE")) {
          // keplerMetricDTO.setKeplerType("CORE");
          kepType = "CORE";
        } else if (keplerName.startsWith("UNCORE")) {
          // keplerMetricDTO.setKeplerType("UNCORE");
          kepType = "UNCORE";
        } else if (keplerName.startsWith("OTHER")) {
          // keplerMetricDTO.setKeplerType("OTHER");
          kepType = "OTHER";
        } else if (keplerName.startsWith("GPU")) {
          // keplerMetricDTO.setKeplerType("GPU");
          kepType = "GPU";
        } else if (keplerName.startsWith("PKG")) {
          // keplerMetricDTO.setKeplerType("PKG");
          kepType = "PKG";
        } else if (keplerName.startsWith("TOTAL")) {
          // keplerMetricDTO.setKeplerType("TOTAL");
          kepType = "TOTAL";
        } else if (keplerName.startsWith("ENERGYSTAT")) {
          // keplerMetricDTO.setKeplerType("ENERGYSTAT");
          kepType = "ENERGYSTAT";
        }
      }
    }
    Date createdTime = null;
    String node = null;
    String podName = null;
    String containerNamespace = null;
    String containerName = null;
    String combinedKey = "";
    Map<String, Double> podNameAsDoubleMap = new HashMap<>();
    Map<String, KeplerMetricDTO> podNameToDTO = new HashMap<>();
    for (SumDataPoint sumDataPoint : sumDataPoints) {
      StringBuffer keys = new StringBuffer();
      String dobulevle = sumDataPoint.getAsDouble();
      double usage = 0;

      if (dobulevle != null) {
        usage = Double.parseDouble(dobulevle);
        // System.out.println("Usage: -----------------" + usage);
      }

      String startTime = sumDataPoint.getTimeUnixNano();
      if (startTime != null) {
        createdTime = convertUnixNanoToLocalDateTime(startTime);
      }

      List<SumDataPointAttribute> sumAtt = sumDataPoint.getAttributes();

      for (SumDataPointAttribute sumDataPointAttribute : sumAtt) {
        String keyValue = sumDataPointAttribute.getKey();

        String attvalue = null;
        SumDataPointAttributeValue gAttValue = sumDataPointAttribute.getValue();

        if (gAttValue != null) {
          attvalue = gAttValue.getStringValue();
        }

        if ("pod_name".equals(keyValue)) {
          podName = attvalue;
        } else if ("container_namespace".equals(keyValue)) {
          containerNamespace = attvalue;
        } else if ("container_name".equals(keyValue)) {
          containerName = attvalue;
        } else if ("exported_instance".equals(keyValue)) {
          node = attvalue;
        }
      }

      combinedKey = podName + "-" + containerNamespace;
      // Check if the podName exists in the map
      if (podNameAsDoubleMap.containsKey(combinedKey)) {
        // If exists, add the current asDouble to the existing value
        double currentSum = podNameAsDoubleMap.get(combinedKey);
        podNameAsDoubleMap.put(combinedKey, currentSum + usage);

        // Update power usage for the existing KeplerMetricDTO
        KeplerMetricDTO existingDTO = podNameToDTO.get(podName);
        existingDTO.setPowerConsumption(existingDTO.getPowerConsumption() + usage);
      } else {
        if (containerNamespace != null) {
          keys.append(containerNamespace);
        }
        if (podName != null) {
          keys.append("/");
          keys.append(podName);
        }
        if (node != null) {
          keys.append(node);
        }
        if (kepType != null) {
          keys.append("/");
          keys.append(kepType);
        }
        // If doesn't exist, add the podName and its asDouble to the map
        podNameAsDoubleMap.put(combinedKey, usage);

        Resource resource = new Resource(
            containerNamespace,
            podName,
            null,
            null,
            metricName,
            node);

        KeplerMetricDTO keplerMetricDTO = new KeplerMetricDTO();

        keplerMetricDTO.setDate(createdTime);
        keplerMetricDTO.setPowerConsumption(usage); // Your initial power consumption value
        keplerMetricDTO.setResource(resource);
        keplerMetricDTO.setType(type);
        keplerMetricDTO.setKeplerType(kepType);
        keplerMetricDTO.setServiceName(
            keys.toString().isEmpty() ? metricName : keys.toString());

        // Add the new KeplerMetricDTO to the map
        podNameToDTO.put(podName, keplerMetricDTO);
      }

    }

    for (Map.Entry<String, KeplerMetricDTO> entry : podNameToDTO.entrySet()) {
      String podName1 = entry.getKey();
      KeplerMetricDTO dto = entry.getValue();

      System.out.println("Pod Name: " + podName1);
      keplerMetricDTOList.add(dto);
      System.out.println("----------------------------------");
    }

    return keplerMetricDTOList;
  }

  private List<KeplerMetricDTO> processGaugeMetric(
      MetricGauge metricGauge,
      String metricName) {
    List<KeplerMetricDTO> keplerMetricDTOList = new ArrayList<>();
    List<GaugeDataPoint> gaugeDataPointLst = metricGauge.getDataPoints();

    KeplerMetricDTO keplerMetricDTO = new KeplerMetricDTO();

    System.out.println("METRIC: " + metricName);
    for (KeplerMetricsNames metricsEnum : KeplerMetricsNames.values()) {
      String matchName = metricsEnum.toString();
      String typeName = metricsEnum.getMetricsName();
      String type = "";
      // System.out.println("CONTAINER " + matchName + " " + metricName);

      if (matchName.equals(metricName)) {
        // System.out.println("CONTAINER " + metricsEnum);
        // keplerMetricDTO.setType(type);
        if (typeName.startsWith("CONT")) {
          System.out.println("CONTAINER " + metricsEnum);
          keplerMetricDTO.setType("container");
          type = "pod";
        } else if (typeName.startsWith("HOST")) {
          System.out.println("HOST " + metricsEnum);
          keplerMetricDTO.setType("host");
          type = "host";
        } else if (typeName.startsWith("NODE")) {
          System.out.println("NODE " + metricsEnum);
          keplerMetricDTO.setType("node");
          type = "node";
        }
        break;
      }
    }
    for (KeplerTypeNames keplerTypeEnum : KeplerTypeNames.values()) {
      String matchTypeName = keplerTypeEnum.toString();
      String keplerName = keplerTypeEnum.getKeplerType();
      String kepType = "";
      if (matchTypeName.equals(metricName)) {
        if (keplerName.startsWith("DRAM")) {
          keplerMetricDTO.setKeplerType("DRAM");
          kepType = "DRAM";
        } else if (keplerName.startsWith("CORE")) {
          keplerMetricDTO.setKeplerType("CORE");
          kepType = "CORE";
        } else if (keplerName.startsWith("UNCORE")) {
          keplerMetricDTO.setKeplerType("UNCORE");
          kepType = "UNCORE";
        } else if (keplerName.startsWith("OTHER")) {
          keplerMetricDTO.setKeplerType("OTHER");
          kepType = "OTHER";
        } else if (keplerName.startsWith("GPU")) {
          keplerMetricDTO.setKeplerType("GPU");
          kepType = "GPU";
        } else if (keplerName.startsWith("PKG")) {
          keplerMetricDTO.setKeplerType("PKG");
          kepType = "PKG";
        } else if (keplerName.startsWith("TOTAL")) {
          keplerMetricDTO.setKeplerType("TOTAL");
          kepType = "TOTAL";
        } else if (keplerName.startsWith("ENERGYSTAT")) {
          keplerMetricDTO.setKeplerType("ENERGYSTAT");
          kepType = "ENERGYSTAT";
        }
      }
    }

    for (GaugeDataPoint gaugeDataPoint : gaugeDataPointLst) {
      StringBuffer keys = null;
      String dobulevle = gaugeDataPoint.getAsDouble();
      // String type = "Gauge";
      double usage = 0;
      Date createdTime = null;
      // long observedTimeMillis = 0;

      if (dobulevle != null) {
        usage = Double.parseDouble(dobulevle);
        System.out.println("Usage: -----------------" + usage);
      }

      String startTimeStm = gaugeDataPoint.getTimeUnixNano();
      if (startTimeStm != null) {
        createdTime = convertUnixNanoToLocalDateTime(startTimeStm);
      }

      // String time = gaugeDataPoint.getTimeUnixNano();

      List<GaugeDataPointAttribute> gaugeDataPointAttributes = gaugeDataPoint.getAttributes();

      for (GaugeDataPointAttribute gAtt : gaugeDataPointAttributes) {
        if (keys == null) {
          keys = new StringBuffer();
        }

        String keyValue = gAtt.getKey();
        String attvalue = null;
        GaugeDataPointAttributeValue gAttValue = gAtt.getValue();

        if (gAttValue != null) {
          attvalue = gAttValue.getStringValue();
        } else {
          attvalue = null;
        }

        if (keyValue != null && attvalue != null) {
          keys.append("( ");
          keys.append(keyValue);
          keys.append(":");
          keys.append(attvalue);
          keys.append(" )");
        }
      }

      keplerMetricDTO.setDate(createdTime);
      keplerMetricDTO.setPowerConsumption(usage);
      keplerMetricDTO.setServiceName(
          keys != null && !keys.equals("") ? keys.toString() : metricName);

      keplerMetricDTOList.add(keplerMetricDTO);
    }

    return keplerMetricDTOList;
  }

  private List<KeplerMetricDTO> processHistogramMetric(
      MetricHistogram histogram,
      String metricName) {
    List<KeplerMetricDTO> keplerMetricDTOList = new ArrayList<>();
    List<HistogramDataPoint> histogramDataPoints = histogram.getDataPoints();

    KeplerMetricDTO keplerMetricDTO = new KeplerMetricDTO();

    for (KeplerMetricsNames metricsEnum : KeplerMetricsNames.values()) {
      String matchName = metricsEnum.toString();
      String typeName = metricsEnum.getMetricsName();
      String type = "";
      // System.out.println("CONTAINER " + matchName + " " + metricName);

      if (matchName.equals(metricName)) {
        // System.out.println("CONTAINER " + metricsEnum);
        // keplerMetricDTO.setType(type);
        if (typeName.startsWith("CONT")) {
          keplerMetricDTO.setType("container");
          type = "pod";
        } else if (typeName.startsWith("HOST")) {
          keplerMetricDTO.setType("host");
          type = "host";
        } else if (typeName.startsWith("NODE")) {
          keplerMetricDTO.setType("node");
          type = "node";
        }
        break;
      }
    }
    for (KeplerTypeNames keplerTypeEnum : KeplerTypeNames.values()) {
      String matchTypeName = keplerTypeEnum.toString();
      String keplerName = keplerTypeEnum.getKeplerType();
      String kepType = "";
      if (matchTypeName.equals(metricName)) {
        if (keplerName.startsWith("DRAM")) {
          keplerMetricDTO.setKeplerType("DRAM");
          kepType = "DRAM";
        } else if (keplerName.startsWith("CORE")) {
          keplerMetricDTO.setKeplerType("CORE");
          kepType = "CORE";
        } else if (keplerName.startsWith("UNCORE")) {
          keplerMetricDTO.setKeplerType("UNCORE");
          kepType = "UNCORE";
        } else if (keplerName.startsWith("OTHER")) {
          keplerMetricDTO.setKeplerType("OTHER");
          kepType = "OTHER";
        } else if (keplerName.startsWith("GPU")) {
          keplerMetricDTO.setKeplerType("GPU");
          kepType = "GPU";
        } else if (keplerName.startsWith("PKG")) {
          keplerMetricDTO.setKeplerType("PKG");
          kepType = "PKG";
        } else if (keplerName.startsWith("TOTAL")) {
          keplerMetricDTO.setKeplerType("TOTAL");
          kepType = "TOTAL";
        } else if (keplerName.startsWith("ENERGYSTAT")) {
          keplerMetricDTO.setKeplerType("ENERGYSTAT");
          kepType = "ENERGYSTAT";
        }
      }
    }

    for (HistogramDataPoint histogramDataPoint : histogramDataPoints) {
      StringBuffer keys = null;
      // String type = "Histogram";

      String startTime = histogramDataPoint.getTimeUnixNano();
      Date createdTime = convertUnixNanoToLocalDateTime(startTime);

      // String time = histogramDataPoint.getTimeUnixNano();

      List<HistogramDataPointAttribute> histogramDataPointAttributes = histogramDataPoint.getAttributes();

      for (HistogramDataPointAttribute histogramDataPointAttribute : histogramDataPointAttributes) {
        if (keys == null) {
          keys = new StringBuffer();
        }

        String keyValue = histogramDataPointAttribute.getKey();
        String attvalue = null;

        HistogramDataPointAttributeValue gAttValue = histogramDataPointAttribute.getValue();

        if (gAttValue != null) {
          attvalue = gAttValue.getStringValue();
        } else {
          attvalue = null;
        }

        if (keyValue != null && attvalue != null) {
          keys.append("( ");
          keys.append(keyValue);
          keys.append(":");
          keys.append(attvalue);
          keys.append(" )");
        }
      }

      keplerMetricDTO.setDate(createdTime);
      double usage = 0;
      keplerMetricDTO.setPowerConsumption(usage);
      // keplerMetricDTO.setObservedTimeMillis(observedTimeMillis);
      keplerMetricDTO.setServiceName(
          keys != null ? keys.toString() : metricName);
      keplerMetricDTOList.add(keplerMetricDTO);
    }

    return keplerMetricDTOList;
  }

  private static Date convertUnixNanoToLocalDateTime(String startTimeUnixNano) {
    long observedTimeMillis = Long.parseLong(startTimeUnixNano) / 1_000_000;

    Instant instant = Instant.ofEpochMilli(observedTimeMillis);

    ZoneId istZone = ZoneId.of("Asia/Kolkata");
    LocalDateTime istDateTime = LocalDateTime.ofInstant(instant, istZone);

    return Date.from(istDateTime.atZone(istZone).toInstant());
  }

  public void addDTO() {
    // KeplerMetricDTO keplerMetricDTO = new KeplerMetricDTO();

    // keplerMetricDTO.setDate(createdTime);
    // keplerMetricDTO.setPowerConsumption(usage);
    // keplerMetricDTO.setObservedTimeMillis(observedTimeMillis);
    // keplerMetricDTO.setServiceName(key == null ? metricName : key);
    // keplerMetricDTO.setType(type);
    // keplerMetricDTOLst.add(keplerMetricDTO);

}
}
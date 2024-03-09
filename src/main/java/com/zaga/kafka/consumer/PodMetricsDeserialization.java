package com.zaga.kafka.consumer;

import org.apache.kafka.common.serialization.Deserializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaga.entity.pod.OtelPodMetric;

public class PodMetricsDeserialization implements Deserializer<OtelPodMetric>{

    private static final ThreadLocal<ObjectMapper> objectMapperThreadLocal = ThreadLocal.withInitial(ObjectMapper::new);

    private ObjectMapper getObjectMapper() {
        return objectMapperThreadLocal.get();
    }
   @Override
public OtelPodMetric deserialize(String topic, byte[] data) {
    if (data == null || data.length == 0) {
        return null; // or throw an exception based on your use case
    }

    try {
        // System.out.println("the infra pod metric data is not null");
        return getObjectMapper().readValue(data, OtelPodMetric.class);
    } catch (Exception e) {
        throw new RuntimeException("Error deserializing JSON", e);
    }
}
    
}

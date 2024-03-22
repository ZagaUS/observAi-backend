package com.zaga.kafka.consumer;

import org.apache.kafka.common.serialization.Deserializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaga.entity.clusterutilization.OtelClusterUutilization;

public class ClusterUtilizationDeserialization implements Deserializer<OtelClusterUutilization>{
      private final ObjectMapper objectMapper;

      public ClusterUtilizationDeserialization(){
        this.objectMapper=new ObjectMapper();
      }
      
 @Override
    public OtelClusterUutilization deserialize(String topic, byte[] data) {
      // System.out.println("------------------[KAFKA/CONSUMER/DESERIALIZATION]---------------- " + data);
      try {
         return objectMapper.readValue(data, OtelClusterUutilization.class);
       } catch (Exception e) {
          throw new RuntimeException("Error deserializing JSON", e);
       }
      }
}

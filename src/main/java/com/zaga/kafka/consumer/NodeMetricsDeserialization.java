package com.zaga.kafka.consumer;

import org.apache.kafka.common.serialization.Deserializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaga.entity.clusterutilization.OtelClusterUutilization;

public class NodeMetricsDeserialization implements Deserializer<OtelClusterUutilization>{
             
   private final ObjectMapper objectMapper;

   public NodeMetricsDeserialization(){
    this.objectMapper= new ObjectMapper();
   }
    @Override
    public OtelClusterUutilization deserialize(String topic, byte[] data) {
      if (data == null || data.length == 0) {
         return null;
     }
 
     try {
         // System.out.println("the infra node metric data is not null");
         return objectMapper.readValue(data, OtelClusterUutilization.class);
       } catch (Exception e) {
          throw new RuntimeException("Error deserializing JSON", e);
       }
    }
    
}

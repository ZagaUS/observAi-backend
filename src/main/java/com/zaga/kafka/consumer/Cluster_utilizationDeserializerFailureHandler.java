package com.zaga.kafka.consumer;


import org.apache.kafka.common.header.Headers;

import com.zaga.entity.cluster_utilization.OtelCluster_utilization;

import io.smallrye.common.annotation.Identifier;
import io.smallrye.reactive.messaging.kafka.DeserializationFailureHandler;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@Identifier("cluster_utilization-failure-fallback")
public class Cluster_utilizationDeserializerFailureHandler implements DeserializationFailureHandler<OtelCluster_utilization>{
    
  @Override
  public OtelCluster_utilization handleDeserializationFailure(
    final String topic,
    final boolean isKey,
    final String deserializer,
    final byte[] data,
    final Exception exception,
    final Headers headers
  ) {
    // Log the deserialization failure with the relevant information
    System.err.println("Deserialization failed for message in topic: " + topic);
    System.err.println("Exception: " + exception.getMessage());

    // Return null to skip the problematic message
    // and continue processing the next message.
    return null;
  }
    
}

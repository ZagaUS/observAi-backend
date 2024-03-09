package com.zaga.kafka.consumer;

import org.eclipse.microprofile.reactive.messaging.Incoming;

import com.zaga.entity.clusterutilization.OtelClusterUutilization;
import com.zaga.handler.ClusterUtilizationHandler;
import com.zaga.handler.NodeCommandHandler;

import jakarta.inject.Inject;

public class ClusterUtilizationConsumer {
    @Inject
   private ClusterUtilizationHandler cluster_utilizationHandler;

     @Inject
NodeCommandHandler nodeCommandHandler;

     @Incoming("cluser_utilization-in")
      public void consumeClusterUtilizationDetails(OtelClusterUutilization cluster_utilization) {
        System.out.println("consumed cluster_utilization -----------");
        
        cluster_utilizationHandler.createClusterUtilization(cluster_utilization);

        nodeCommandHandler.createNodeMetric(cluster_utilization);
     }
}

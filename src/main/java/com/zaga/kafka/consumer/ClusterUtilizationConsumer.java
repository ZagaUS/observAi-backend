package com.zaga.kafka.consumer;

import org.eclipse.microprofile.reactive.messaging.Incoming;

import com.zaga.entity.clusterutilization.OtelClusterUutilization;
import com.zaga.handler.ClusterUtilizationHandler;
import com.zaga.repo.ClusterUtilizationRepo;

import jakarta.inject.Inject;


public class ClusterUtilizationConsumer {
    @Inject
   private ClusterUtilizationHandler cluster_utilizationHandler;

   @Inject
   ClusterUtilizationRepo clusterUtilizationRepo;

  
     @Incoming("cluser_utilization-audit-in")
      public void consumeClusterUtilizationDetails(OtelClusterUutilization cluster_utilization) {
        System.out.println("consumed cluster_utilization -----------"+cluster_utilization);      
        clusterUtilizationRepo.persist(cluster_utilization);

        
    }

    @Incoming("cluser_utilization-in")
    public void consumeClusterUtilizationDTODetails(OtelClusterUutilization cluster_utilization) {
      System.out.println("consumed cluster_utilizationDTO -----------"+cluster_utilization);        
      cluster_utilizationHandler.extractAndMapClusterData(cluster_utilization);
  }
}

package com.zaga.kafka.consumer;

import org.eclipse.microprofile.reactive.messaging.Incoming;

import com.zaga.entity.clusterutilization.OtelClusterUutilization;
import com.zaga.handler.NodeCommandHandler;
import com.zaga.repo.ClusterUtilizationRepo;
import com.zaga.repo.NodeDTORepo;
import jakarta.inject.Inject;

public class NodeMetricConsumerService {
    
    
    @Inject
    ClusterUtilizationRepo cluster_utilizationRepo;

    @Inject
    NodeCommandHandler nodeCommandHandler;

    @Inject
    NodeDTORepo nodeDTORepo;

  @Incoming("node-in")
   public void consumePodMetricDetails(OtelClusterUutilization node) {
    System.out.println("consumed infra Node metric data----------------"+node);
    if (node != null) {
      nodeCommandHandler.createNodeMetric(node);
      cluster_utilizationRepo.persist(node);

    } else {
      System.out.println("Received null message. Check serialization/deserialization.");
    }
  }

}

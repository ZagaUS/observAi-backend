// package com.zaga.kafka.consumer;

// import org.eclipse.microprofile.reactive.messaging.Incoming;

// import com.zaga.entity.node.OtelNode;
// import com.zaga.handler.NodeCommandHandler;
// import com.zaga.repo.NodeDTORepo;
// import com.zaga.repo.NodeMetricRepo;
// import jakarta.inject.Inject;

// public class NodeMetricConsumerService {
    
    
//     @Inject
//     NodeMetricRepo repo;

//     @Inject
//     NodeCommandHandler nodeCommandHandler;

//     @Inject
//     NodeDTORepo nodeDTORepo;

//   // @Incoming("node-in")
//   // @Incoming("cluser_utilization-in")
//   public void consumePodMetricDetails(OtelNode node) {
//     System.out.println("consumed infra Node metric data----------------"+node);
//     if (node != null) {
//       nodeCommandHandler.createNodeMetric(node);
//       repo.persist(node);

//     } else {
//       System.out.println("Received null message. Check serialization/deserialization.");
//     }
//   }

// }

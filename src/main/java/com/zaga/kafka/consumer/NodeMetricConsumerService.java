package com.zaga.kafka.consumer;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Incoming;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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

  @Inject 
  @ConfigProperty(name = "cluster.otel.replica.url")
  String destinationUrl;

  @Incoming("node-in")
   public void consumePodMetricDetails(OtelClusterUutilization node) {
    System.out.println("consumed infra Node metric data----------------"+node);
    if (node != null) {
      nodeCommandHandler.createNodeMetric(node);
      // cluster_utilizationRepo.persist(node);
      replicateNodeDTOData(node);
    } else {
      System.out.println("Received null message. Check serialization/deserialization.");
    }
  }

  private void replicateNodeDTOData(OtelClusterUutilization data) {
    System.out.println("------*********Sending NodeDTO JSON data: " + data);
      // String destinationIndiaUrl = "https://api.zagaopenshift.zagaopensource.com:6443/cluster_utilization/create_cluster_audit";
      // String destinationIndiaUrl = "http://localhost:8080/nodeMetrics/create_NodeDTO";
      try {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("resourceMetrics", gson.toJsonTree(data.getResourceMetrics()));
          HttpClient client = HttpClient.newHttpClient();
          HttpRequest request = HttpRequest.newBuilder()
                  .uri(new URI(destinationUrl + "/nodeMetrics/create_NodeDTO"))
                  .header("Content-Type", "application/json")
                  .POST(HttpRequest.BodyPublishers.ofString(jsonObject.toString()))
                  .build();
          client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                  .thenAccept(response -> System.out.println("Replication response code: " + response.statusCode()))
                  .exceptionally(e -> {
                      e.printStackTrace();
                      return null;
                  });
      } catch (Exception e) {
          e.printStackTrace();
      }
  }

}

package com.zaga.kafka.consumer;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.eclipse.microprofile.reactive.messaging.Incoming;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.zaga.entity.clusterutilization.OtelClusterUutilization;
import com.zaga.handler.ClusterUtilizationHandler;
import com.zaga.repo.ClusterUtilizationRepo;

import jakarta.inject.Inject;


public class ClusterUtilizationConsumer {
    @Inject
   private ClusterUtilizationHandler cluster_utilizationHandler;

   @Inject
   ClusterUtilizationRepo clusterUtilizationRepo;

  
    //  @Incoming("cluser_utilization-audit-in")
    //   public void consumeClusterUtilizationDetails(OtelClusterUutilization cluster_utilization) {
    //     System.out.println("consumed cluster_utilization -----------"+cluster_utilization);      
    //     clusterUtilizationRepo.persist(cluster_utilization);

        
    // }

// @Incoming("cluser_utilization-audit-in")
    public void consumeClusterUtilizationDetails(OtelClusterUutilization cluster_utilization) {
        System.out.println("Consumed cluster_utilization: " + cluster_utilization);
        clusterUtilizationRepo.persist(cluster_utilization);
        replicateData(cluster_utilization);
    }

    private void replicateData(OtelClusterUutilization data) {
      System.out.println("------*********Sending JSON data: " + data);
        // String destinationIndiaUrl = "https://api.zagaopenshift.zagaopensource.com:6443/cluster_utilization/create_cluster_audit";
        String destinationIndiaUrl = "http://localhost:8080/cluster_utilization/create_cluster_audit";
        try {
          // Gson gson = new Gson();
          // JsonElement jsonElement = gson.toJsonTree(data);
          // JsonObject jsonObject = jsonElement.getAsJsonObject();
          Gson gson = new Gson();
          JsonObject jsonObject = new JsonObject();
          jsonObject.add("resourceMetrics", gson.toJsonTree(data.getResourceMetrics()));
  
          System.out.println(jsonObject);
  
          System.out.println(jsonObject);
          // System.out.println(jsonElement);
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(destinationIndiaUrl))
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


    // @Incoming("cluser_utilization-in")
    public void consumeClusterUtilizationDTODetails(OtelClusterUutilization cluster_utilization) {
      System.out.println("consumed cluster_utilizationDTO -----------"+cluster_utilization);        
      cluster_utilizationHandler.extractAndMapClusterData(cluster_utilization);
      replicateDTOData(cluster_utilization);
  }



  private void replicateDTOData(OtelClusterUutilization data) {
    System.out.println("------*********Sending DTO JSON data: " + data);
      // String destinationIndiaUrl = "https://api.zagaopenshift.zagaopensource.com:6443/cluster_utilization/create_cluster_audit";
      String destinationIndiaUrl = "http://localhost:8080/cluster_utilization/create_clusterDTO";
      try {
        // Gson gson = new Gson();
        // JsonObject jsonObject = new JsonObject();
        // jsonObject.add("resourceMetrics", gson.toJsonTree(data.getResourceMetrics()));
 Gson gson = new Gson();
          JsonElement jsonElement = gson.toJsonTree(data);
          JsonObject jsonObject = jsonElement.getAsJsonObject();
        System.out.println("--------"+jsonObject);
       System.out.println(jsonElement);
          HttpClient client = HttpClient.newHttpClient();
          HttpRequest request = HttpRequest.newBuilder()
                  .uri(new URI(destinationIndiaUrl))
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

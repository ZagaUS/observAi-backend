package com.zaga.kafka.consumer;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
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
    if (node != null) {
      nodeCommandHandler.createNodeMetric(node);
      // cluster_utilizationRepo.persist(node);
      replicateNodeDTOData(node);
    } else {
      System.out.println("Received null message. Check serialization/deserialization.");
    }
  }

  private void replicateNodeDTOData(OtelClusterUutilization data) {
    try {
      // Gson gson = new Gson();
      // JsonObject jsonObject = new JsonObject();
      // jsonObject.add("resourceMetrics",
      // gson.toJsonTree(data.getResourceMetrics()));
      // HttpClient client = HttpClient.newHttpClient();
      // HttpRequest request = HttpRequest.newBuilder()
      // .uri(new URI(destinationUrl + "/nodeMetrics/create_NodeDTO"))
      // .header("Content-Type", "application/json")
      // .POST(HttpRequest.BodyPublishers.ofString(jsonObject.toString()))
      // .build();
      // client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
      // .thenAccept(response -> System.out.println("Replication response code: " +
      // response.statusCode()))
      // .exceptionally(e -> {
      // e.printStackTrace();
      // return null;
      // });

      // CHANGES BY SURENDAR
      Gson gson = new Gson();
      JsonElement jsonElement = gson.toJsonTree(data);
      StringEntity entity = new StringEntity(jsonElement.toString());
      CloseableHttpClient httpClient = HttpClientBuilder.create()
          .build();
      String postUrl = destinationUrl + "/nodeMetrics/create_NodeDTO";
      HttpPost postRequest = new HttpPost(postUrl);
      postRequest.setHeader("Accept", "application/json");
      postRequest.setHeader("Content-Type", "application/json");
      postRequest.setEntity(entity);
      CloseableHttpResponse response = httpClient.execute(postRequest);

      HttpEntity responseEntity = response.getEntity();
      if (responseEntity != null) {
        System.out.println("=======[RESPONSE ENTITY]======= " + responseEntity);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}

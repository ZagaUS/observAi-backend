package com.zaga.kafka.consumer;

import org.apache.http.HttpEntity;
import org.apache.http.auth.AuthScope;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

// import java.net.URI;
// import java.net.http.HttpClient;
// import java.net.http.HttpRequest;
// import java.net.http.HttpResponse;

import org.eclipse.microprofile.config.inject.ConfigProperty;
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

  @Inject
  @ConfigProperty(name = "cluster.otel.replica.url")
  String destinationUrl;

  @Incoming("cluser_utilization-audit-in")
  public void consumeClusterUtilizationDetails(OtelClusterUutilization cluster_utilization) {
    clusterUtilizationRepo.persist(cluster_utilization);
    replicateData(cluster_utilization);
  }

  private void replicateData(OtelClusterUutilization data) {
    try {
      Gson gson = new Gson();
      JsonObject jsonObject = new JsonObject();
      jsonObject.add("resourceMetrics", gson.toJsonTree(data.getResourceMetrics()));

      System.out.println(jsonObject);

      System.out.println(jsonObject);
      // HttpClient client = HttpClient.newHttpClient();
      // HttpRequest request = HttpRequest.newBuilder()
      // .uri(new URI(destinationUrl + "/cluster_utilization/create_cluster_audit"))
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

      JsonElement jsonElement = gson.toJsonTree(data);
      StringEntity entity = new StringEntity(jsonElement.toString());
      CloseableHttpClient httpClient = HttpClientBuilder.create()
          .build();
      String postUrl = destinationUrl + "/cluster_utilization/create_cluster_audit";
      HttpPost postRequest = new HttpPost(postUrl);
      postRequest.setHeader("Accept", "application/json");
			postRequest.setHeader("Content-Type", "application/json");
      postRequest.setEntity(entity);
      CloseableHttpResponse response = httpClient.execute(postRequest);

      HttpEntity responseEntity = response.getEntity();
      if(responseEntity != null){
        System.out.println("=======[RESPONSE ENTITY]======= " + responseEntity);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Incoming("cluser_utilization-in")
  public void consumeClusterUtilizationDTODetails(OtelClusterUutilization cluster_utilization) {
    cluster_utilizationHandler.extractAndMapClusterData(cluster_utilization);
    replicateDTOData(cluster_utilization);
  }

  private void replicateDTOData(OtelClusterUutilization data) {
    try {
      Gson gson = new Gson();
      // JsonElement jsonElement = gson.toJsonTree(data);
      // JsonObject jsonObject = jsonElement.getAsJsonObject();
      // System.out.println("--------" + jsonObject);
      // System.out.println(jsonElement);
      // HttpClient client = HttpClient.newHttpClient();
      // HttpRequest request = HttpRequest.newBuilder()
      // .uri(new URI(destinationUrl + "/cluster_utilization/create_clusterDTO"))
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
      JsonElement jsonElement = gson.toJsonTree(data);
      StringEntity entity = new StringEntity(jsonElement.toString());
      CloseableHttpClient httpClient = HttpClientBuilder.create()
          .build();
      String postUrl = destinationUrl + "/cluster_utilization/create_clusterDTO";
      HttpPost postRequest = new HttpPost(postUrl);
      postRequest.setHeader("Accept", "application/json");
			postRequest.setHeader("Content-Type", "application/json");
      postRequest.setEntity(entity);
      CloseableHttpResponse response = httpClient.execute(postRequest);

      HttpEntity responseEntity = response.getEntity();
      if(responseEntity != null){
        System.out.println("=======[RESPONSE ENTITY DTO]======= " + responseEntity);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}

package com.zaga.kafka.consumer;

import java.net.URI;
import java.net.URISyntaxException;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.zaga.entity.pod.OtelPodMetric;
import com.zaga.handler.PodCommandHandler;
import com.zaga.repo.PodCommandRepo;
import com.zaga.repo.PodMetricDTORepo;

import jakarta.inject.Inject;

public class PodMetricsConsumerService {

  @Inject
  PodCommandHandler podCommandHandler;
  @Inject
  PodCommandRepo podCommandRepo;
  @Inject
  PodMetricDTORepo podMetricDTORepo;

  @Inject
  @ConfigProperty(name = "cluster.otel.replica.url")
  String destinationUrl;

  @Incoming("pod-audit-in")
  public void consumePodMetricDetails(OtelPodMetric podMetrics) {
    if (podMetrics != null) {
      podCommandRepo.persist(podMetrics);
      reallocateData(podMetrics);
    } else {
      System.out.println("Received null message. Check serialization/deserialization.");
    }
  }

  private void reallocateData(OtelPodMetric podMetrics) {
    // HttpClient client = HttpClient.newHttpClient();
    // try {
    // String json = new ObjectMapper().writeValueAsString(podMetrics);

    // HttpRequest request = HttpRequest.newBuilder()
    // .uri(new URI(destinationUrl+"/podMetrics/create"))
    // .header("Content-Type", "application/json")
    // .POST(HttpRequest.BodyPublishers.ofString(json))
    // .build();

    // client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
    // .thenAccept(response -> System.out.println("response code: " +
    // response.statusCode()))
    // .exceptionally(e -> {
    // e.printStackTrace();
    // return null;
    // });
    // } catch (JsonProcessingException | URISyntaxException e) {
    // e.printStackTrace();
    // }
    try {
      // CHANGES BY SURENDAR
      Gson gson = new Gson();
      JsonElement jsonElement = gson.toJsonTree(podMetrics);
      StringEntity entity = new StringEntity(jsonElement.toString());
      CloseableHttpClient httpClient = HttpClientBuilder.create()
          .build();
      String postUrl = destinationUrl + "/podMetrics/create";
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

  @Incoming("pod-in")
  public void consumePodDTOMetricDetails(OtelPodMetric podMetrics) {
    if (podMetrics != null) {
      podCommandHandler.extractAndMapData(podMetrics);
      relocateData(podMetrics);
      // podCommandRepo.persist(podMetrics);
    } else {
      System.out.println("Received null message. Check serialization/deserialization.");
    }

  }

  private void relocateData(OtelPodMetric podMetrics) {
    // HttpClient client = HttpClient.newHttpClient();
    // try {
    // String json = new ObjectMapper().writeValueAsString(podMetrics);

    // HttpRequest request = HttpRequest.newBuilder()
    // .uri(new URI(destinationUrl+"/podMetrics/create_PodDTO"))
    // .header("Content-Type", "application/json")
    // .POST(HttpRequest.BodyPublishers.ofString(json))
    // .build();

    // client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
    // .thenAccept(response -> System.out.println(" response code: " +
    // response.statusCode()))
    // .exceptionally(e -> {
    // e.printStackTrace();
    // return null;
    // });
    // } catch (JsonProcessingException | URISyntaxException e) {
    // e.printStackTrace();
    // }

    try {
      // CHANGES BY SURENDAR
      Gson gson = new Gson();
      JsonElement jsonElement = gson.toJsonTree(podMetrics);
      StringEntity entity = new StringEntity(jsonElement.toString());
      CloseableHttpClient httpClient = HttpClientBuilder.create()
          .build();
      String postUrl = destinationUrl + "/podMetrics/create_PodDTO";
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

  // private void reallocateData(OtelPodMetric podMetrics){

  // HttpClient client = HttpClient.newHttpClient();
  // try{
  // HttpRequest request = HttpRequest.newBuilder()
  // .uri(new URI("http://localhost:8081/podMetrics/create"))
  // .header("Content-Type", "application/json")
  // .POST(HttpRequest.BodyPublishers.ofString(podMetrics.toString()))
  // .build();
  // client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
  // .thenAccept(response -> System.out.println("Replication response code: " +
  // response.statusCode()))
  // .exceptionally(e -> {
  // e.printStackTrace();
  // return null;
  // });
  // } catch (Exception e) {
  // e.printStackTrace();
  // }
}

// Client client = ClientBuilder.newClient();
// Response response = client.
// target("http://localhost:8081/podMetrics/create")
// //
// target("http://api.zagaopenshift.zagaopensource.com:6443/podMetrics/create-audit")
// // .request(MediaType.APPLICATION_JSON)
// .request()
// .post(Entity.entity(podMetrics, MediaType.APPLICATION_JSON));

// System.out.println("Response status: " + response.getStatus());
// String responseBody = response.readEntity(String.class);
// System.out.println("Response entity: " + responseBody);

// response.close();

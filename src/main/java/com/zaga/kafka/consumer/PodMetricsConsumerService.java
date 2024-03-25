package com.zaga.kafka.consumer;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

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

  private static final Logger LOG = Logger.getLogger(PodMetricsConsumerService.class);

  @Inject
  Logger log;

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
    CloseableHttpClient httpClient = null;
    try {
      // CHANGES BY SURENDAR
      Gson gson = new Gson();
      JsonElement jsonElement = gson.toJsonTree(podMetrics);
      String jsonBody = new ObjectMapper().writeValueAsString(podMetrics);
      StringEntity entity = new StringEntity(jsonBody.toString());
      RequestConfig requestConfig = RequestConfig.custom()
          // Set connection timeout to 5 seconds
          .setConnectTimeout(5000)
          .build();

      httpClient = HttpClients.custom()
          .setDefaultRequestConfig(requestConfig)
          .build();
      String postUrl = destinationUrl + "/podMetrics/create";
      HttpPost postRequest = new HttpPost(postUrl);
      postRequest.setHeader("Accept", "application/json");
      postRequest.setHeader("Content-Type", "application/json");
      postRequest.setEntity(entity);
      CloseableHttpResponse response = httpClient.execute(postRequest);

      HttpEntity responseEntity = response.getEntity();
      int statusCode = response.getStatusLine().getStatusCode();
      if (statusCode == 200) {
        log.debug("from replicateDTOData method - Request successful. Status code: " + statusCode);
        if (responseEntity != null) {

          log.debug("from replicateDTOData method - Http response from replicateData method" + responseEntity);
        }
      } else {
        log.debug("from replicateDTOData method - Request failed. Status code: " + statusCode);
      }
    } catch (Exception e) {
      log.error("An error occurred  while executing replicateDTOData", e);
    } finally {
      if (httpClient != null) {
        try {
          httpClient.close();
        } catch (IOException e) {
          log.error("An error occurred  while executing replicateDTOData", e);
        }
      }
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
    CloseableHttpClient httpClient = null;
    try {
      // CHANGES BY SURENDAR
      Gson gson = new Gson();
      JsonElement jsonElement = gson.toJsonTree(podMetrics);
      String jsonBody = new ObjectMapper().writeValueAsString(podMetrics);
      StringEntity entity = new StringEntity(jsonBody.toString());
      RequestConfig requestConfig = RequestConfig.custom()
          // Set connection timeout to 5 seconds
          .setConnectTimeout(5000)
          .build();

      httpClient = HttpClients.custom()
          .setDefaultRequestConfig(requestConfig)
          .build();
      String postUrl = destinationUrl + "/podMetrics/create_PodDTO";
      HttpPost postRequest = new HttpPost(postUrl);
      postRequest.setHeader("Accept", "application/json");
      postRequest.setHeader("Content-Type", "application/json");
      postRequest.setEntity(entity);
      CloseableHttpResponse response = httpClient.execute(postRequest);

      HttpEntity responseEntity = response.getEntity();
      int statusCode = response.getStatusLine().getStatusCode();
      if (statusCode == 200) {
        log.debug("from replicateDTOData method - Request successful. Status code: " + statusCode);
        if (responseEntity != null) {

          log.debug("from replicateDTOData method - Http response from replicateData method" + responseEntity);
        }
      } else {
        log.debug("from replicateDTOData method - Request failed. Status code: " + statusCode);
      }
    } catch (Exception e) {
      log.error("An error occurred  while executing replicateDTOData", e);
    } finally {
      if (httpClient != null) {
        try {
          httpClient.close();
        } catch (IOException e) {
          log.error("An error occurred  while executing replicateDTOData", e);
        }
      }
    }

  }
}

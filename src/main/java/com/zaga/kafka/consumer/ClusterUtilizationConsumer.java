package com.zaga.kafka.consumer;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.auth.AuthScope;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;

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

import org.jboss.logging.Logger;

public class ClusterUtilizationConsumer {

  private static final Logger LOG = Logger.getLogger(ClusterUtilizationConsumer.class);

  @Inject
  Logger log;

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
    CloseableHttpClient httpClient = null;

    try {
      Gson gson = new Gson();
      JsonObject jsonObject = new JsonObject();
      jsonObject.add("resourceMetrics", gson.toJsonTree(data.getResourceMetrics()));

      JsonElement jsonElement = gson.toJsonTree(data);
      StringEntity entity = new StringEntity(jsonElement.toString());

      RequestConfig requestConfig = RequestConfig.custom()
          // Set connection timeout to 5 seconds
          .setConnectTimeout(5000)
          .build();

      httpClient = HttpClients.custom()
          .setDefaultRequestConfig(requestConfig)
          .build();

      String postUrl = destinationUrl + "/cluster_utilization/create_cluster_audit";
      HttpPost postRequest = new HttpPost(postUrl);
      postRequest.setHeader("Accept", "application/json");
      postRequest.setHeader("Content-Type", "application/json");
      postRequest.setEntity(entity);

      CloseableHttpResponse response = httpClient.execute(postRequest);

      HttpEntity responseEntity = response.getEntity();
      int statusCode = response.getStatusLine().getStatusCode();

      if (statusCode == 200) {
        log.debug("from replicateData method - Request successful. Status code: " + statusCode);
        if (responseEntity != null) {

          log.debug("from replicateData method - Http response from replicateData method" + responseEntity);
        }
      } else {
        log.debug("from replicateData method - Request failed. Status code: " + statusCode);
      }

    } catch (Exception e) {

      log.error("An error occurred  while executing replicateData", e);

    } finally {
      if (httpClient != null) {
        try {
          httpClient.close();
        } catch (IOException e) {
          log.error("An error occurred  while executing replicateData", e);
        }
      }
    }
  }

  @Incoming("cluser_utilization-in")
  public void consumeClusterUtilizationDTODetails(OtelClusterUutilization cluster_utilization) {
    cluster_utilizationHandler.extractAndMapClusterData(cluster_utilization);
    replicateDTOData(cluster_utilization);
  }

  private void replicateDTOData(OtelClusterUutilization data) {

    CloseableHttpClient httpClient = null;

    try {
      Gson gson = new Gson();
      JsonElement jsonElement = gson.toJsonTree(data);
      StringEntity entity = new StringEntity(jsonElement.toString());

      RequestConfig requestConfig = RequestConfig.custom()
          // Set connection timeout to 5 seconds
          .setConnectTimeout(5000)
          .build();

      httpClient = HttpClients.custom()
          .setDefaultRequestConfig(requestConfig)
          .build();

      String postUrl = destinationUrl + "/cluster_utilization/create_clusterDTO";
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

package com.zaga.kafka.consumer;

import java.io.IOException;
import java.net.URI;
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.zaga.entity.otelevent.OtelEvents;
import com.zaga.handler.EventCommandHandler;
import com.zaga.repo.EventRepo;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class EventConsumerService {

  private static final Logger LOG = Logger.getLogger(EventConsumerService.class);

  @Inject
  Logger log;

  @Inject
  private EventCommandHandler eventCommandHandler;

  @Inject
  EventRepo eventRepo;

  @Inject
  @ConfigProperty(name = "cluster.otel.replica.url")
  String destinationUrl;

  @Incoming("event-audit-in")
  public void consumeEventDetails(OtelEvents events) {
    eventRepo.persist(events);
    replicateDataForEvents(events);
  }

  @Incoming("event-in")
  public void consumeEventDTODetails(OtelEvents events) {
    eventCommandHandler.handleEventData(events);
    replicateData(events);
  }

  public void replicateData(OtelEvents eventsData) {
    CloseableHttpClient httpClient = null;
    try {
      // CHANGES BY SURENDAR
      Gson gson = new Gson();
      JsonElement jsonElement = gson.toJsonTree(eventsData);
      String jsonBody = new ObjectMapper().writeValueAsString(eventsData);
      StringEntity entity = new StringEntity(jsonBody.toString());
      RequestConfig requestConfig = RequestConfig.custom()
          // Set connection timeout to 5 seconds
          .setConnectTimeout(5000)
          .build();

      httpClient = HttpClients.custom()
          .setDefaultRequestConfig(requestConfig)
          .build();
      String postUrl = destinationUrl + "/events/create_event_Dto";
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

  public void replicateDataForEvents(OtelEvents eventsData) {
    CloseableHttpClient httpClient = null;
    try {
      // CHANGES BY SURENDAR
      Gson gson = new Gson();
      JsonElement jsonElement = gson.toJsonTree(eventsData);
      String jsonBody = new ObjectMapper().writeValueAsString(eventsData);
      StringEntity entity = new StringEntity(jsonBody.toString());

      RequestConfig requestConfig = RequestConfig.custom()
          // Set connection timeout to 5 seconds
          .setConnectTimeout(5000)
          .build();

      httpClient = HttpClients.custom()
          .setDefaultRequestConfig(requestConfig)
          .build();
      String postUrl = destinationUrl + "/events/create_events";
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

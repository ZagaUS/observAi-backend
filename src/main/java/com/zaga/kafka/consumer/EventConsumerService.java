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

  @Inject
  private EventCommandHandler eventCommandHandler;

  @Inject
  EventRepo eventRepo;

  @Inject
  @ConfigProperty(name = "cluster.otel.replica.url")
  String destinationUrl;

  @Incoming("event-audit-in")
  public void consumeEventDetails(OtelEvents events) {
    System.out.println("consumed Event -----------");
    eventRepo.persist(events);
    replicateDataForEvents(events);
    System.out.println("consumed Event ----ended-------");
  }

  @Incoming("event-in")
  public void consumeEventDTODetails(OtelEvents events) {
    System.out.println("consumed EventDTO -----------");
    eventCommandHandler.handleEventData(events);
    replicateData(events);
    System.out.println("consumed EventDTO --ended---------");

  }

  public void replicateData(OtelEvents eventsData) {
    try {
      // // String url = "http://localhost:8081/event/create_event_Dto";
      // String jsonBody = new ObjectMapper().writeValueAsString(eventsData);

      // System.out.println("Sending request to: " + destinationUrl);
      // System.out.println("Request body: " + jsonBody);

      // HttpClient client = HttpClient.newHttpClient();
      // HttpRequest request = HttpRequest.newBuilder()
      // .uri(new URI(destinationUrl + "/events/create_event_Dto"))
      // .header("Content-Type", "application/json")
      // .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
      // .build();

      // client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
      // .thenAccept(response -> {
      // System.out.println("Replication response code: " + response.statusCode());
      // // Optionally handle response body here
      // })
      // .exceptionally(e -> {
      // e.printStackTrace();
      // return null;
      // });

      // CHANGES BY SURENDAR
      Gson gson = new Gson();
      JsonElement jsonElement = gson.toJsonTree(eventsData);
      StringEntity entity = new StringEntity(jsonElement.toString());
      CloseableHttpClient httpClient = HttpClientBuilder.create()
          .build();
      String postUrl = destinationUrl + "/events/create_event_Dto";
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

  public void replicateDataForEvents(OtelEvents eventsData) {
    try {
      // String jsonBody = new ObjectMapper().writeValueAsString(eventsData);

      // System.out.println("Sending request to: " + destinationUrl);
      // System.out.println("Request body: " + jsonBody);

      // HttpClient client = HttpClient.newHttpClient();
      // HttpRequest request = HttpRequest.newBuilder()
      // .uri(new URI(destinationUrl + "/events/create_events"))
      // .header("Content-Type", "application/json")
      // .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
      // .build();

      // client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
      // .thenAccept(response -> {
      // System.out.println("Replication response code: " + response.statusCode());
      // // Optionally handle response body here
      // })
      // .exceptionally(e -> {
      // e.printStackTrace();
      // return null;
      // });

      // CHANGES BY SURENDAR
      Gson gson = new Gson();
      JsonElement jsonElement = gson.toJsonTree(eventsData);
      StringEntity entity = new StringEntity(jsonElement.toString());
      CloseableHttpClient httpClient = HttpClientBuilder.create()
          .build();
      String postUrl = destinationUrl + "/events/create_events";
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

package com.zaga.controller;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.zaga.entity.otelevent.OtelEvents;
import com.zaga.handler.EventCommandHandler;
import com.zaga.kafka.consumer.EventConsumerService;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/event")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EventController {
    @Inject
    EventCommandHandler handler;

     @Inject
     EventConsumerService eventConsumerService;

    @Inject 
    @ConfigProperty(name = "cluster.url")
    String clusterUrl;




    @POST
    @Path("/create_event")
    public Response createEvent (OtelEvents event){
      try {
         handler.handleEventData(event);

        return Response.status(200).entity(event).build();

      } catch (Exception e) {
        return Response.status(500).entity(e.getMessage()).build();
        
      }

    }

    //  @POST
    // @Path("/create_event_cluster")
    // public Response createEventCluster(OtelEvents event) {
    //     try {

    //      handler.handleEventData(event);
    //      System.out.println("cluster name " + handler.getClusterName(null));

    //         // Call the other API using HttpClient
    //         HttpClient client = HttpClient.newHttpClient();
    //         HttpRequest request = HttpRequest.newBuilder()
    //                 .uri(URI.create("https://api.zagaobservability.zagaopensource.com:6443"))
    //                 .GET()
    //                 .build();

    //         CompletableFuture<HttpResponse<String>> response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
            
    //         // Wait for the response
    //         HttpResponse<String> apiResponse = response.join();
            
    //         // Handle the response as needed
    //         // int statusCode = apiResponse.statusCode();
    //         // String responseBody = apiResponse.body();

    //         // You can process the responseBody JSON or return it in the response
    //         return Response.status(200).entity(event).build();
         

        
    //     } catch (Exception e) {
    //         return Response.status(500).entity(e.getMessage()).build();
    //     }
    


    //   }


    @POST
    @Path("/create_events")
    public void createEvents (OtelEvents event){
      String Url = "https://api.zagaobservability.zagaopensource.com:6443/event/create_events";
      replicateData(event,Url);
         eventConsumerService.consumeEventDTODetails(event);
    }

    private void replicateData(OtelEvents eventsData, String destinationUrl) {
              try {
                  HttpClient client = HttpClient.newHttpClient();
                  HttpRequest request = HttpRequest.newBuilder()
                          .uri(new URI(destinationUrl))
                          .POST(HttpRequest.BodyPublishers.ofString(eventsData.toString()))
                          .build();
                  client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
                  System.out.println("----------event data-------"+eventsData);
              } catch (Exception e) {
                  e.printStackTrace();
              }
          }





    }


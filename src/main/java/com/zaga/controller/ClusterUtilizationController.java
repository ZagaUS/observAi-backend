package com.zaga.controller;


import com.zaga.entity.clusterutilization.OtelClusterUutilization;
import com.zaga.handler.ClusterUtilizationHandler;
import com.zaga.repo.ClusterUtilizationRepo;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/cluster_utilization")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ClusterUtilizationController {
    
    @Inject
    ClusterUtilizationHandler cluster_utilizationHandler;

    @Inject
    ClusterUtilizationRepo cluster_utilizationRepo;


    @POST
    @Path("/create_clusterUtilization")
    public Response createEvent (OtelClusterUutilization cluster_utilization){
      try {
        cluster_utilizationHandler.createClusterUtilization(cluster_utilization);
        return Response.status(200).entity(cluster_utilization).build();

      } catch (Exception e) {
        return Response.status(500).entity(e.getMessage()).build();
        
      }
    }
  
    

    
    @POST
    @Path("/create_cluster_audit")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createClusterAudit(OtelClusterUutilization clusterUtilization) {
      try {
        cluster_utilizationRepo.persist(clusterUtilization);
        return Response.status(200).entity(clusterUtilization).build();

      } catch (Exception e) {
        return Response.status(500).entity(e.getMessage()).build();
        
      }
    }


    @POST
    @Path("/create_clusterDTO")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createClusterDTO(OtelClusterUutilization clusterUtilization) {
      try {
        System.out.println("Received JSON data: " + clusterUtilization);
        cluster_utilizationHandler.extractAndMapClusterData(clusterUtilization);
        System.out.println("---------------------DTO"+clusterUtilization);
        return Response.status(200).entity(clusterUtilization).build();

      } catch (Exception e) {
        return Response.status(500).entity(e.getMessage()).build();
        
      }
    }
    
 
    
    // @POST
    // @Path("/create_cluster")
    // public void consumeClusterUtilization(OtelClusterUutilization clusterUtilization) {
    //     System.out.println("###############Consumed cluster_utilization------" + clusterUtilization);
    //     String destinationIndiaUrl = "https://api.zagaopenshift.zagaopensource.com:6443/cluster_utilization/create_cluster";
    //     replicateData(clusterUtilization, destinationIndiaUrl);
    //     System.out.println("------------Data replicated to " + destinationIndiaUrl);
    //     clusterUtilizationConsumer.consumeClusterUtilizationDetails(clusterUtilization);
    //     clusterUtilizationConsumer.consumeClusterUtilizationDTODetails(clusterUtilization);
    // }
    
    // private void replicateData(OtelClusterUutilization data, String destinationIndiaUrl) {
    //     try {
    //         HttpClient client = HttpClient.newHttpClient();
    //         HttpRequest request = HttpRequest.newBuilder()
    //                 .uri(new URI(destinationIndiaUrl))
    //                 .header("Content-Type", "application/json") 
    //                 .POST(HttpRequest.BodyPublishers.ofString(data.toString()))
    //                 .build();
    //         client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
    //                 .thenAccept(response -> System.out.println("Replication response code: " + response.statusCode()))
    //                 .exceptionally(e -> {
    //                     e.printStackTrace();
    //                     return null;
    //                 });
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //     }
    // }
    
    


}

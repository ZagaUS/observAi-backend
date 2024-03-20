package com.zaga.controller;




import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.zaga.entity.pod.OtelPodMetric;
import com.zaga.handler.PodCommandHandler;
import com.zaga.kafka.consumer.PodMetricsConsumerService;
import com.zaga.repo.PodCommandRepo;
import com.zaga.repo.PodMetricDTORepo;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/podMetrics")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PodController {

@Inject
PodCommandRepo podCommandRepo;

@Inject
PodCommandHandler podCommandHandler;

@Inject
PodMetricDTORepo podMetricDTORepo;

@Inject
PodMetricsConsumerService podMetricsConsumerService;

@Inject
@ConfigProperty(name ="cluster.url")
String clusterurl;


@POST
@Path("/create")
public Response createPodMetric(OtelPodMetric podMetric) {
    podCommandRepo.persist(podMetric);
    podCommandHandler.extractAndMapData(podMetric);
    return Response.status(Response.Status.CREATED).entity(podMetric).build();
}



// @POST
// @Path("/create-pod")
// public Response createPodMetrics(OtelPodMetric podMetric) {
//     String Url= "https://api.zagaobservability.zagaopensource.com:6443/podMetrics/create-pod";
//     replicateData(podMetric, Url);


// }

//     private void replicateData(OtelPodMetric podData, String destinationUrl) {
//         try {
//             HttpClient client = HttpClient.newHttpClient();
//             HttpRequest request = HttpRequest.newBuilder()
//                     .uri(new URI(destinationUrl))
//                     .POST(HttpRequest.BodyPublishers.ofString(podData.toString()))
//                     .build();
//             client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
//             System.out.println("##########################-------"+podData);
//         } catch (Exception e) {
//             e.printStackTrace();
//         }
//     }


    @POST
    @Path("/create_pod")
    public void createEvents (OtelPodMetric metric){
      String Url = "https://api.zagaobservability.zagaopensource.com:6443/podMetrics/create_pod";
      replicateData(metric,Url);
         podMetricsConsumerService.consumePodMetricDetails(metric);
    }

    private void replicateData(OtelPodMetric metric, String destinationUrl) {
              try {
                  HttpClient client = HttpClient.newHttpClient();
                  HttpRequest request = HttpRequest.newBuilder()
                          .uri(new URI(destinationUrl))
                          .POST(HttpRequest.BodyPublishers.ofString(metric.toString()))
                          .build();
                  client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
                     System.out.println("**********start"+metric);   
              } catch (Exception e) {
                  e.printStackTrace();
              }
          }

}

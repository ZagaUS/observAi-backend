package com.zaga.kafka.consumer;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URISyntaxException;
import java.util.List;


import org.eclipse.microprofile.reactive.messaging.Incoming;

import com.zaga.entity.kepler.KeplerMetric;
import com.zaga.entity.pod.OtelPodMetric;
import com.zaga.entity.queryentity.pod.PodMetricDTO;
import com.zaga.handler.KeplerMetricCommandHandler;
import com.zaga.handler.PodCommandHandler;
import com.zaga.repo.KeplerMetricRepo;

import jakarta.inject.Inject;

import com.google.gson.Gson;

public class KeplerConsumerService {

    @Inject
    KeplerMetricCommandHandler keplerMetricCommandHandler;

    @Inject
    KeplerMetricRepo keplerMetricRepo;

    // @Incoming("kepler-audit-in")
    public void consumeKeplerDetails(KeplerMetric keplerMetric) { 
        if (keplerMetric != null) {
            System.out.println("consumed Kepler data ------------------");
            keplerMetricRepo.persist(keplerMetric);
        } else {
            System.out.println("Received null message. Check serialization/deserialization.");
        }
    }


    // @Incoming("kepler-in")
    public void consumeKeplerDTODetails(KeplerMetric keplerMetric) { 
        if (keplerMetric != null) {
            System.out.println("consumed KeplerDTO data ------------------");
            keplerMetricCommandHandler.extractAndMapData(keplerMetric);
        } else {
            System.out.println("Received null message. Check serialization/deserialization.");
        }
    }

    // public static void main(String[] ar) throws URISyntaxException {

    //     Gson gson = new Gson();

    //     File file = new File("pod.json");

    //     try (Reader reader1 = new FileReader(file)) {

    //         OtelPodMetric keplerMetric = gson.fromJson(reader1, OtelPodMetric.class);

    //         PodCommandHandler podCommandHandler = new PodCommandHandler();

    //         List<PodMetricDTO> podMetricDTOlst = podCommandHandler.extractAndMapData(keplerMetric);

    //         System.out.println(podMetricDTOlst);

    //         for (PodMetricDTO podMetricDTO : podMetricDTOlst) {

    //             System.out.println(podMetricDTO.toString());

    //         }
    //         System.out.println(podMetricDTOlst.size());

    //         // keplerMetricCommandHandler.createKeplerMetric(keplerMetric);

    //     } catch (IOException e) {
    //         System.out.println("ERROR " + e.getLocalizedMessage());
    //     }

    // }

}

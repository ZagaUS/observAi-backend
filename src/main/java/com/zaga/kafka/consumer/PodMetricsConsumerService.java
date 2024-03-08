package com.zaga.kafka.consumer;

import org.eclipse.microprofile.reactive.messaging.Incoming;

import com.zaga.entity.pod.OtelPodMetric;
import com.zaga.handler.PodCommandHandler;
import com.zaga.repo.PodCommandRepo;

import jakarta.inject.Inject;

public class PodMetricsConsumerService {
  
    @Inject
    PodCommandHandler podCommandHandler;
    PodCommandRepo podCommandRepo;

  @Incoming("pod-in")
  public void consumePodMetricDetails(OtelPodMetric podMetrics) {
    System.out.println("consumed infra podmetric data----------------"+podMetrics);
    if (podMetrics != null) {
      podCommandHandler.createPodMetric(podMetrics);
    podCommandRepo.persist(podMetrics);
    } else {
      System.out.println("Received null message. Check serialization/deserialization.");
    }
  }



}

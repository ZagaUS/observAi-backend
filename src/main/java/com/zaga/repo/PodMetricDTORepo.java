package com.zaga.repo;

import com.zaga.entity.queryentity.pod.PodMetricDTO;

import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PodMetricDTORepo implements PanacheMongoRepository<PodMetricDTO>{
    
}

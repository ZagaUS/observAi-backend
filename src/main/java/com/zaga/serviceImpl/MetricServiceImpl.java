package com.zaga.serviceImpl;

import com.zaga.entity.otelmetric.MetricMain;
import com.zaga.repo.MetricRepo;
import com.zaga.service.MetricService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class MetricServiceImpl implements MetricService{

    @Inject
    MetricRepo metricRepo;


    @Override
    public void createProduct(MetricMain metric) {
        metricRepo.persist(metric);
    }
    
}

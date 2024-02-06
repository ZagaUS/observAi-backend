package com.zaga.entity.queryentity.pod;

import java.util.ArrayList;
import java.util.List;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@MongoEntity(collection = "PodMetricDTO", database = "OtelPodMetrics")
public class PodMetricDTO extends PanacheMongoEntity{
    private List<MetricDTO> metrics = new ArrayList<>();
    private String podName;
}

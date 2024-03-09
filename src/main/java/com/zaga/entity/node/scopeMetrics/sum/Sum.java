package com.zaga.entity.node.scopeMetrics.sum;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class Sum {
    @JsonProperty("dataPoints")
    private List<SumDataPoints> dataPoints;
    @JsonIgnore
    @JsonProperty("isMonotonic")
    private boolean isMonotonic;
    @JsonIgnore
    @JsonProperty("aggregationTemporality")
    private int aggregationTemporality;
}

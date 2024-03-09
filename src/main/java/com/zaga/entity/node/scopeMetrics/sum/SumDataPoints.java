package com.zaga.entity.node.scopeMetrics.sum;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class SumDataPoints {
    @JsonProperty("asDouble")
    private Double asDouble;
    @JsonProperty("asInt")
    private String asInt;
    @JsonProperty("timeUnixNano")
    private String timeUnixNano;
    @JsonProperty("startTimeUnixNano")
    private String startTimeUnixNano;
}

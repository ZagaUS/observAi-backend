package com.zaga.entity.node;

import java.util.List;

import lombok.*;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResourceMetrics {
    private Resource resource;
    private List<ScopeMetric> scopeMetrics;
    private String schemaUrl;
}

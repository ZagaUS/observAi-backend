package com.zaga.entity.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Environments {
    private String clusterUsername;
    private String clusterPassword;
    private String hostUrl;
    private String clusterType;
}

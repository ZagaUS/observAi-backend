package com.zaga.controller;

import com.zaga.entity.clusterutilization.OtelClusterUutilization;
// import com.zaga.entity.node.OtelNode;
import com.zaga.handler.ClusterUtilizationHandler;
import com.zaga.handler.NodeCommandHandler;
import com.zaga.repo.ClusterUtilizationRepo;
import com.zaga.repo.NodeDTORepo;
// import com.zaga.repo.NodeMetricRepo;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/nodeMetrics")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)

public class NodeController {
    
// @Inject
// NodeMetricRepo repo;

 @Inject
NodeCommandHandler nodeCommandHandler;

// @Inject
// NodeDTORepo nodeDTORepo; 

 @Inject
    ClusterUtilizationHandler cluster_utilizationHandler;

    @Inject
    ClusterUtilizationRepo cluster_utilizationRepo;



    @POST
    @Path("/create")
    public Response createNodeMetrics(OtelClusterUutilization cluster_utilization){
        // cluster_utilizationRepo.persist(cluster_utilization);
        nodeCommandHandler.extractAndMapNodeData(cluster_utilization);
        return Response.status(Response.Status.CREATED).entity(cluster_utilization).build();

    }

}

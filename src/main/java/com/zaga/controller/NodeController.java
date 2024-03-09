package com.zaga.controller;

import com.zaga.entity.node.OtelNode;
import com.zaga.handler.NodeCommandHandler;
import com.zaga.repo.NodeDTORepo;
import com.zaga.repo.NodeMetricRepo;

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
    
@Inject
NodeMetricRepo repo;

@Inject
NodeCommandHandler nodeCommandHandler;

@Inject
NodeDTORepo nodeDTORepo; 

    @POST
    @Path("/create")
    public Response createNodeMetrics(OtelNode node){
        repo.persist(node);
        nodeCommandHandler.extractAndMapNodeData(node);
        return Response.status(Response.Status.CREATED).entity(node).build();

    }

}

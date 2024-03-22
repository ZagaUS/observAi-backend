package com.zaga.controller;

import com.zaga.entity.otelevent.OtelEvents;
import com.zaga.handler.EventCommandHandler;


import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/event")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EventController {
    @Inject
    EventCommandHandler handler;

      @POST
    @Path("/create_event")
    public Response createEvent (OtelEvents event){
      try {
         handler.handleEventData(event);

        return Response.status(200).entity(event).build();

      } catch (Exception e) {
        return Response.status(500).entity(e.getMessage()).build();
        
      }

    }

    }


package com.zaga.controller;

import com.zaga.entity.otelevent.OtelEvents;
import com.zaga.handler.EventCommandHandler;
import com.zaga.repo.EventRepo;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/events")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EventController {
  
    @Inject
    EventCommandHandler handler;

    @Inject
    EventRepo eventRepo;

    
    @POST
    @Path("/create_event_Dto")
    public Response createEvent (OtelEvents event){
      try {
        System.out.println("test create");
         handler.handleEventData(event);
        return Response.status(200).entity(event).build();
      } catch (Exception e) {
        return Response.status(500).entity(e.getMessage()).build();   
      }

    }


    @POST
    @Path("/create_events")
    public Response createEvents (OtelEvents event){
        try{
        eventRepo.persist(event);
        return Response.status(200).entity(event).build();
        }
        catch(Exception e){
          return Response.status(500).entity(event).build();
    }
  }


    }


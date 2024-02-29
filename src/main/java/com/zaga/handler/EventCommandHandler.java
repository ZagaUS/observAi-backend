package com.zaga.handler;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.zaga.entity.otelevent.OtelEvents;
import com.zaga.entity.otelevent.Resource;
import com.zaga.entity.otelevent.ResourceLogs;
import com.zaga.entity.otelevent.ScopeLogs;
import com.zaga.entity.otelevent.scopeLogs.LogRecords;
import com.zaga.entity.queryentity.events.EventsDTO;
import com.zaga.repo.EventRepo;
import com.zaga.repo.EventsDTORepo;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;


@ApplicationScoped
public class EventCommandHandler {
    
    @Inject
    EventRepo eventRepo ;

    @Inject
    EventsDTORepo eventsDTORepo;

    public OtelEvents createEvents (OtelEvents events) {
        eventRepo.persist(events);
        List<EventsDTO> eventsList = handleEventData(events);
        return events;
    }
    public List<EventsDTO> handleEventData(OtelEvents events) {
        List<EventsDTO> eventsDTOList = new ArrayList<>();
    List<ResourceLogs> resourceLogsList = events.getResourceLogs();

    for (ResourceLogs resourceLogs : resourceLogsList) {
        Resource resource = resourceLogs.getResource();
        String nodeName = getNodeName(resource);
        String objectKind = getObjectKind(resource);
        String objectName = getObjectName(resource);
        String clusterName = getClusterName(resource); 

        List<ScopeLogs> scopeLogsList = resourceLogs.getScopeLogs();
        for (ScopeLogs scopeLogs : scopeLogsList) {
            String timeUnixNano = getTimeUnixNano(scopeLogs);

            List<LogRecords> logRecordsList = scopeLogs.getLogRecords();
            Set<String> uniqueBodies = new HashSet<>(); 
            for (LogRecords logRecord : logRecordsList) {
                
                String severityText = logRecord.getSeverityText();
                String spanId = logRecord.getSpanId();
                String traceId = logRecord.getTraceId();
                String body = logRecord.getBody().getStringValue();  
                System.out.println("scopeLogs------"+ scopeLogs);
                Date createdTime = new Date(Long.parseLong(timeUnixNano) / 1_000_000); 
               
                if (uniqueBodies.add(body)) {
                   
                    EventsDTO eventsDTO = new EventsDTO();
                    eventsDTO.setSeverityText(severityText);
                    eventsDTO.setCreatedTime(createdTime);
                    System.out.println("-------CreatedTime------"+eventsDTO.getCreatedTime());
                    eventsDTO.setNodeName(nodeName);
                    eventsDTO.setObjectKind(objectKind);
                    eventsDTO.setObjectName(objectName);
                    eventsDTO.setClusterName(clusterName);
                    
                    List<ScopeLogs> singleScopeLogsList = new ArrayList<>();
                    singleScopeLogsList.add(scopeLogs);
                    eventsDTO.setScopeLogs(singleScopeLogsList);

                    eventsDTORepo.persist(eventsDTO);
                    eventsDTOList.add(eventsDTO);
                }
            }
        }
    }
    return eventsDTOList;
}

    private String getNodeName(Resource resource) {
        return resource.getAttributes()
                .stream()
                .filter(attribute -> "k8s.node.name".equals(attribute.getKey()))
                .findFirst()
                .map(attribute -> attribute.getValue().getStringValue())
                .orElse(null);
    }

    private String getObjectKind(Resource resource) {
        return resource.getAttributes()
                .stream()
                .filter(attribute -> "k8s.object.kind".equals(attribute.getKey()))
                .findFirst()
                .map(attribute -> attribute.getValue().getStringValue())
                .orElse(null);
    }

    private String getObjectName(Resource resource) {
        return resource.getAttributes()
                .stream()
                .filter(attribute -> "k8s.object.name".equals(attribute.getKey()))
                .findFirst()
                .map(attribute -> attribute.getValue().getStringValue())
                .orElse(null);
    }

    private String getClusterName(Resource resource) {
        return resource.getAttributes()
                .stream()
                .filter(attribute -> "k8s.cluster.name".equals(attribute.getKey()))
                .findFirst()
                .map(attribute -> attribute.getValue().getStringValue())
                .orElse(null);
    }

    private String getTimeUnixNano(ScopeLogs scopeLogs) {
        return scopeLogs.getLogRecords().get(0).getTimeUnixNano();
    }
}

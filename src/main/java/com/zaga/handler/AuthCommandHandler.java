package com.zaga.handler;

import com.zaga.entity.auth.UserCredentials;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import com.zaga.entity.auth.Environments;
import com.zaga.entity.auth.ServiceListNew;
import com.zaga.repo.AuthRepo;
import com.zaga.repo.ServiceListRepo;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
public class AuthCommandHandler {

  @Inject
  AuthRepo authRepo;

  @Inject
  ServiceListRepo serviceListRepo;

  public Response getUserInfo(final UserCredentials credentials) {
    try {
      UserCredentials userCreds = authRepo
          .find("username = ?1 and password = ?2", credentials.getUsername(), credentials.getPassword())
          .firstResult();
      if (userCreds != null) {
        return Response.status(200).entity(userCreds).build();
      } else {
        return Response
            .status(Response.Status.UNAUTHORIZED)
            .entity("Username or password is incorrect")
            .build();
      }
    } catch (Exception e) {
      e.printStackTrace();
      return Response.serverError().build();
    }
  }





  


public Response saveUserInfo(final UserCredentials credentials) {
    System.out.println("----------------Saving user info just entered---------------");

    UserCredentials existingUser = authRepo
            .find("username", credentials.getUsername())
            .firstResult();

    if (existingUser != null) {
        // User already exists, append the new environments to the existing user
        System.out.println("User found. Appending new environments.");

        AtomicReference<List<Environments>> existingEnvironments = new AtomicReference<>(
                existingUser.getEnvironments() != null ? new ArrayList<>(existingUser.getEnvironments()) : new ArrayList<>()
        );

        // Increment clusterId for each new environment
        credentials.getEnvironments().forEach(environment -> {
            Long maxClusterId = existingEnvironments.get().stream()
                    .map(Environments::getClusterId)
                    .max(Long::compare)
                    .orElse(0L);
            environment.setClusterId(maxClusterId + 1);
        });

        existingEnvironments.get().addAll(credentials.getEnvironments());
        existingUser.setEnvironments(existingEnvironments.get());

        authRepo.update(existingUser);  // Update the existing user
        System.out.println("User environments updated successfully.");
        return Response
                .status(200)
                .entity("User environments updated successfully")
                .build();
    } else {
        // User does not exist, persist the new user
        System.out.println("User credentials not found. Registering a new user.");

        AtomicReference<List<Environments>> existingEnvironments = new AtomicReference<>(
                credentials.getEnvironments() != null ? new ArrayList<>(credentials.getEnvironments()) : new ArrayList<>()
        );

        // Increment clusterId for each new environment
        credentials.getEnvironments().forEach(environment -> {
            Long maxClusterId = existingEnvironments.get().stream()
                    .map(Environments::getClusterId)
                    .max(Long::compare)
                    .orElse(0L);
            environment.setClusterId(maxClusterId + 1);
        });

        authRepo.persist(credentials);
        return Response
                .status(201)
                .entity("User registered successfully")
                .build();
    }
}

public Response updateUserInfo(UserCredentials userInfo) {
  UserCredentials existingUser = authRepo
          .find("username", userInfo.getUsername())
          .firstResult();

  if (existingUser != null) {
      List<Environments> existingEnvironments = existingUser.getEnvironments() != null ?
              new ArrayList<>(existingUser.getEnvironments()) :
              new ArrayList<>();

      userInfo.getEnvironments().forEach(environment -> {
          Optional<Environments> existingEnvironment = existingEnvironments.stream()
                  .filter(e -> e.getClusterId() == environment.getClusterId())
                  .findFirst();

          if (existingEnvironment.isPresent()) {
              // Update the existing environment with the new data
              Environments existingEnv = existingEnvironment.get();
              existingEnv.setClusterUsername(environment.getClusterUsername());
              existingEnv.setClusterPassword(environment.getClusterPassword());
              existingEnv.setHostUrl(environment.getHostUrl());
              existingEnv.setClusterType(environment.getClusterType());
              existingEnv.setRoles(environment.getRoles());
              
              // Update other properties as needed

              System.out.println("Environment with clusterId " + environment.getClusterId() + " updated successfully.");
          } else {
              // Increment clusterId for new environment
              Long maxClusterId = existingEnvironments.stream()
                      .map(Environments::getClusterId)
                      .max(Long::compare)
                      .orElse(0L);
              environment.setClusterId(maxClusterId + 1);

              existingEnvironments.add(environment);

              System.out.println("New environment with clusterId " + environment.getClusterId() + " added successfully.");
          }
      });

      existingUser.setEnvironments(existingEnvironments);
      authRepo.update(existingUser);  // Update the existing user

      System.out.println("User environments updated successfully.");
      return Response
              .status(200)
              .entity("User environments updated successfully")
              .build();
  }

  return Response.status(200).entity(userInfo.getEnvironments()).build();
}

 


  public UserCredentials getUserInfoByUsername(String username) {
    return authRepo.find("username", username).firstResult();
  }
  // public static void updateUserPassword(String username, String newPassword) {
  // update("username", username)
  // .set("password", newPassword)
  // .execute();
  // }

  // public Response addServiceList(final ServiceList serviceList) {
  // try {
  // ServiceList serviceData = serviceListRepo
  // .find("serviceName = ?1", serviceList.getServiceName())
  // .firstResult();
  // // System.out.println("------Register service---- " +
  // serviceData.getServiceName());
  // if (serviceData == null) {
  // serviceListRepo.persist(serviceList);
  // return Response
  // .status(201)
  // .entity("Service registered successfully")
  // .build();
  // } else {
  // return Response
  // .status(Response.Status.NOT_FOUND)
  // .entity("Service Already exists")
  // .build();
  // }
  // } catch (Exception e) {
  // e.printStackTrace();
  // return Response.status(501).entity(e).build();
  // }
  // }

  public Response getServiceList(final UserCredentials userCredentials) {
    try {
      java.util.List<ServiceListNew> serviceData = serviceListRepo
          .find("roles in ?1", userCredentials.getRoles())
          .list();
          System.out.println("serviceData--------"+serviceData);
      if (serviceData != null) {
        return Response.status(201).entity(serviceData).build();
      } else {
        return Response
            .status(Response.Status.NOT_FOUND)
            .entity("Data not found for this role")
            .build();
      }
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }
  // public UserCredentials getUserInfoByUsername(String username) {
  // return authRepo.find("username", username).firstResult();
  // }

  public UserCredentials updateUserCredentials(UserCredentials userCredentials) {
    // UserCredentials userData = authRepo.getUser(userCredentials.getUsername());
    // System.out.println(userData);
    UserCredentials existingUser = authRepo.getUser(userCredentials.getUsername());
    if (existingUser == null) {
      throw new WebApplicationException("User Not Found ", 500);
    }
    UserCredentials user = userCredentials;
    // System.out.println(user);
    // user.setId(existingUser.getId());
    user.setRoles(existingUser.getRoles());
    user.setUsername(existingUser.getUsername());
    UserCredentials.update(user);
    System.out.println(user);
    return userCredentials;
  }




  
}

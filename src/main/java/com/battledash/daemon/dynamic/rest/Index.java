package com.battledash.daemon.dynamic.rest;

import com.battledash.daemon.dynamic.models.APIResponse;
import org.glassfish.jersey.server.ManagedAsync;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

@Path("/")
public class Index {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ManagedAsync
    public APIResponse<String> index(@Context ContainerRequestContext data) {
        return new APIResponse<>(true, "Hello there, https://youtu.be/frszEJb0aOo");
    }

}

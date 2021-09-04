package com.battledash.daemon.dynamic.rest;

import com.battledash.daemon.dynamic.DaemonNode;
import com.battledash.daemon.dynamic.models.APIResponse;
import org.glassfish.jersey.server.ManagedAsync;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

@Path("/cache")
public class Cache {

    @POST
    @Path("/update")
    @Produces(MediaType.APPLICATION_JSON)
    @ManagedAsync
    public APIResponse<Void> update(@Context ContainerRequestContext data) throws IOException, NoSuchAlgorithmException, InterruptedException {
        DaemonNode.getInstance().getCacheUpdater().update();
        return new APIResponse<>(true);
    }

}

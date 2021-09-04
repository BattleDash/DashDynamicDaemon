package com.battledash.daemon.dynamic.rest;

import com.battledash.daemon.dynamic.errors.BadRequestException;
import com.battledash.daemon.dynamic.models.MiniServer;
import com.battledash.daemon.dynamic.models.Server;
import com.battledash.daemon.dynamic.utils.ServerUtils;
import com.battledash.daemon.dynamic.DaemonNode;
import com.battledash.daemon.dynamic.Env;
import com.battledash.daemon.dynamic.errors.NotFoundException;
import com.battledash.daemon.dynamic.models.APIResponse;
import org.glassfish.jersey.server.ManagedAsync;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Path("/")
public class Servers {
    private static final Logger log = LoggerFactory.getLogger(Servers.class);

    @GET
    @Path("/servers")
    @Produces(MediaType.APPLICATION_JSON)
    @ManagedAsync
    public APIResponse<List<Server>> getServers(@Context ContainerRequestContext data) {
        return new APIResponse<>(true, new ArrayList<>(Server.SERVERS));
    }

    @GET
    @Path("/servers/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @ManagedAsync
    public APIResponse<Server> getServer(@Context ContainerRequestContext data, @PathParam("id") String id) {
        Server server = Server.getById(id);
        if (server == null) throw new NotFoundException("Server not found.");
        return new APIResponse<>(true, server);
    }

    @POST
    @Path("/servers/{id}/ws/custom")
    @Produces(MediaType.APPLICATION_JSON)
    @ManagedAsync
    public APIResponse<Void> sendCustomPayload(@Context ContainerRequestContext data, @PathParam("id") String id, String body) {
        Server server = Server.getById(id);
        if (server == null) throw new NotFoundException("Server not found.");
        if (server.getWs() == null || !server.getWs().isConnected()) throw new BadRequestException("Server socket not open.");
        server.getWs().sendData("CUSTOM_PAYLOAD", new JSONObject(body).getString("message"));
        return new APIResponse<>(true);
    }

    @POST
    @Path("/server/mini/spawn")
    @Produces(MediaType.APPLICATION_JSON)
    @ManagedAsync
    public APIResponse<Server> spawnMiniServer(@Context ContainerRequestContext data, String body)
            throws IOException, InterruptedException, NoSuchAlgorithmException {
        if (Env.DEBUG) {
            log.info("Received Request to spawn server, data: {}", body);
        }
        DaemonNode.getInstance().getCacheUpdater().update();
        JSONObject json = new JSONObject(body);
        Server server = new MiniServer(json.getString("type"), ServerUtils.getUnusedPort(),
                json.getJSONObject("metadata").toMap().entrySet()
                        .stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        server.init();
        return new APIResponse<>(true, server);
    }

    @DELETE
    @Path("/servers/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @ManagedAsync
    public APIResponse<Void> deleteServer(@Context ContainerRequestContext data, @PathParam("id") String id) throws IOException {
        Server server = Server.getById(id);
        if (server == null) throw new NotFoundException("Server not found.");
        server.destroy();
        return new APIResponse<>(true);
    }

}

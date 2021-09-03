package com.battledash.daemon.dynamic.socket;

import com.battledash.daemon.dynamic.models.APIResponse;
import com.battledash.daemon.dynamic.models.Server;
import com.battledash.daemon.dynamic.models.ServerState;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import lombok.SneakyThrows;
import org.glassfish.grizzly.GrizzlyFuture;
import org.glassfish.grizzly.http.HttpRequestPacket;
import org.glassfish.grizzly.websockets.DataFrame;
import org.glassfish.grizzly.websockets.DefaultWebSocket;
import org.glassfish.grizzly.websockets.ProtocolHandler;
import org.glassfish.grizzly.websockets.WebSocketListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

/**
 * WebSocket server that is connected to from
 * game servers to transmit metadata, state, and
 * join tickets. Created by {@link DaemonNodeSocket#createSocket}.
 */
public class DaemonServerWebSocket extends DefaultWebSocket {
    private static final Logger log = LoggerFactory.getLogger(DaemonServerWebSocket.class);

    private final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public DaemonServerWebSocket(ProtocolHandler protocolHandler, HttpRequestPacket request, WebSocketListener... listeners) {
        super(protocolHandler, request, listeners);
    }

    @Getter
    private Server server;

    /**
     * Handles open connection to the socket instance.
     * Tries to identify the server based on connection parameters,
     * and sends server metadata like map names and node instances.
     */
    @SneakyThrows
    @Override
    public void onConnect() {
        super.onConnect();
        log.info("Got new Socket Connection");
        String[] split = getUpgradeRequest().getRequestURI().split("/ws/");
        if (split.length < 2) {
            sendErrorAndClose("Invalid Server ID.");
            return;
        }
        String serverId = split[1];
        if ((server = Server.getById(serverId)) == null) {
            sendErrorAndClose("Server does not exist.");
            return;
        }
        if (server.getWs() != null) {
            sendErrorAndClose("Server already has a socket connection.");
            return;
        }
        server.setWs(this);
        server.setState(ServerState.IDENTIFIED);
        sendData("IDENTIFIED", new JsonClasses.ServerData(server.getId()));
        server.sendMetadata();
        log.info("IDENTIFIED Server {} {}s after startup and sent metadata", server,
                (Instant.now().toEpochMilli() - server.getStartedAt()) / 1000);
    }

    public GrizzlyFuture<DataFrame> sendErrorAndClose(String error) {
        GrizzlyFuture<DataFrame> errorFuture = sendData("ERROR", error);
        close(1011, error); // 1011 = SERVER_ERROR
        return errorFuture;
    }

    public <T> GrizzlyFuture<DataFrame> sendData(String type, T data) {
        return send(GSON.toJson(new JsonClasses.WebsocketData(type, new Gson().toJsonTree(data))));
    }

    @Override
    public void onMessage(String text) {
        super.onMessage(text);
        if (server == null || server.getWs() != this) {
            sendErrorAndClose("A message was received before Server was identified.");
            close();
            return;
        }
        server.onMessage(GSON.fromJson(text, JsonClasses.WebsocketData.class));
    }

    @Override
    public String toString() {
        return "DaemonServerWebSocket{" +
                "server=" + server +
                '}';
    }
}

package com.battledash.daemon.dynamic.socket;

import org.glassfish.grizzly.http.HttpRequestPacket;
import org.glassfish.grizzly.websockets.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DaemonNodeSocket extends WebSocketApplication {
    private static final Logger log = LoggerFactory.getLogger(DaemonNodeSocket.class);

    @Override
    public WebSocket createSocket(ProtocolHandler handler, HttpRequestPacket requestPacket, WebSocketListener... listeners) {
        return new DaemonServerWebSocket(handler, requestPacket, listeners);
    }

    @Override
    protected boolean onError(WebSocket webSocket, Throwable t) {
        DaemonServerWebSocket serverSocket = (DaemonServerWebSocket) webSocket;
        log.error("An error occurred on WebSocket for " + serverSocket.getServer(), t);
        return super.onError(webSocket, t);
    }

}

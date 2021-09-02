package com.battledash.daemon.dynamic;

import com.battledash.daemon.dynamic.socket.DaemonNodeSocket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http2.Http2AddOn;
import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.grizzly.websockets.WebSocketAddOn;
import org.glassfish.grizzly.websockets.WebSocketEngine;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;

import java.io.IOException;
import java.net.URI;

/**
 * The main class of DynamicDaemon. Loads the
 * Jersey ResourceConfig {@link DaemonNode},
 * and starts the HTTP server.
 */
public class Launcher {

    /**
     * Main function, initializes the HTTP server and all subsystems.
     *
     * @param args Parameters to the daemon.
     *
     * @throws IOException if {@link org.apache.commons.io.FileUtils#deleteDirectory} fails in {@link DaemonNode} initialization.
     */
    public static void main(String[] args) throws IOException {
        Logger log = LogManager.getLogger(Launcher.class);
        log.info("Loading, please wait...");

        SSLContextConfigurator sslCon = new SSLContextConfigurator();

        // TODO: 9/1/2021 Implement SSL certificate to keystore generation
        //sslCon.setKeyStoreFile(Env.SSL_KEYSTORE);
        //sslCon.setKeyStorePass("");
        //sslCon.setTrustStoreFile(Env.SSL_TRUSTSTORE);
        //sslCon.setTrustStorePass("");

        URI uri = URI.create("https://" + Env.HOST + ":" + Env.PORT + "/");
        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(uri, new DaemonNode(), false,
                new SSLEngineConfigurator(sslCon, false, false, false),
                false);

        server.getListener("grizzly").registerAddOn(new WebSocketAddOn());
        server.getListener("grizzly").registerAddOn(new Http2AddOn());
        WebSocketEngine.getEngine().register("", "/ws/*", new DaemonNodeSocket());

        // Start HTTP server
        server.start();
        log.info("Online and ready!");

        // Keep it running
        System.in.read();
    }

}

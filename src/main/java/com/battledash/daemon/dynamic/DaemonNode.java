package com.battledash.daemon.dynamic;

import com.battledash.daemon.dynamic.models.Server;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.mongodb.client.model.Filters;
import lombok.Getter;
import com.battledash.daemon.dynamic.utils.NetworkUtils;
import org.apache.commons.io.FileUtils;
import org.bson.Document;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJsonProvider;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.wadl.internal.WadlResource;

import javax.ws.rs.ApplicationPath;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * The Jersey ResourceConfig for the daemon,
 * loads REST handlers/filters, and handles database
 * connections and cache initialization.
 *
 * Initialized by {@link Launcher}
 */
@ApplicationPath("/")
@Getter
public class DaemonNode extends ResourceConfig {
    private static DaemonNode instance;
    private final CacheUpdater cacheUpdater;
    private final Database database;

    public DaemonNode() throws IOException {
        instance = this;
        database = new Database();

        // Initialize Jersey handlers
        packages("com.battledash.daemon.dynamic");
        register(LoggingFeature.class);
        register(WadlResource.class);

        final JacksonJsonProvider jacksonJsonProvider = new JacksonJaxbJsonProvider();
        jacksonJsonProvider.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        register(jacksonJsonProvider);

        // Initialize Archive Cache for GameTypes, pulling from Network Control
        cacheUpdater = new CacheUpdater();

        // Delete any old servers in the event of a node crash
        for (String s : Objects.requireNonNull(Server.SERVERS_DIR.list())) {
            File file = Paths.get(Server.SERVERS_DIR.getAbsolutePath(), s).toFile();
            if (file.isDirectory()) {
                FileUtils.deleteDirectory(file);
            }
        }

        // Delete old data relating to this node, this should be deleted on shutdown but in the event of a crash we do it here as well
        database.getDynamicNodes().deleteOne(Filters.eq("_id", Env.NODE));
        database.getDynamicServers().deleteMany(Filters.eq("node", Env.NODE));

        // Insert data for this node, only running nodes are present
        database.getDynamicNodes().insertOne(new Document("_id", Env.NODE)
                .append("host", NetworkUtils.getPublicIPAddress())
                .append("port", Env.PORT)
                .append("range", Arrays.stream(Env.SERVER_PORT_RANGE.split("-")).map(Integer::parseInt).collect(Collectors.toList())));

        // Delete this node's data on shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            database.getDynamicNodes().deleteOne(Filters.eq("_id", Env.NODE));
            database.getDynamicServers().deleteMany(Filters.eq("node", Env.NODE));
        }));
    }
}

package com.battledash.daemon.dynamic.models;

import com.battledash.daemon.dynamic.utils.StreamUtils;
import com.google.common.collect.ImmutableMap;
import com.battledash.daemon.dynamic.DaemonNode;
import com.battledash.daemon.dynamic.Env;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;

/**
 * A generic minigame server, as an example implementation of {@link Server}.
 */
public class MiniServer extends Server {
    private static final Logger log = LoggerFactory.getLogger(MiniServer.class);

    public MiniServer(String typeId, int port, Map<Object, Object> metadata) {
        super(typeId, port, metadata);
        this.setId("mini" + Integer.toHexString(port).toUpperCase());
    }

    @Override
    public void init() throws IOException {
        log.info("Starting new MiniServer as {}", this);
        while (DaemonNode.getInstance().getCacheUpdater().getLockedGametypes().contains(this.getTypeId())) {
            // Blocks the thread until the archive is done downloading
            // TODO: 7/16/2021 Find a better solution for this, java synchronization is confusing
        }
        this.getDirectory().mkdirs();
        try {
            this.unpack(); // Takes ~1.5s
            this.setProcess(new ProcessBuilder()
                    .command(Arrays.asList(
                            (String)getMetadata().getOrDefault("javaPath", "java"),
                            "-Dcom.mojang.eula.agree=true",
                            "-Dserver.id=" + getId(),
                            "-Dserver.name=" + getId(),
                            "-Daemon.hostname=" + Env.HOST + ":" + Env.PORT,
                            "-Database.mongo=" + Env.MONGO_DATABASE,
                            "-Database.redis=" + Env.REDIS_DATABASE,
                            "-DNCP.host=" + Env.NCP_HOST + ":" + Env.NCP_PORT,
                            "-DNCP.secret=" + Env.NCP_SECRET,
                            "-DIReallyKnowWhatIAmDoingISwear",
                            "-jar", this.getDirectory().getAbsolutePath() + "/server.jar",
                            "--port", String.valueOf(this.getPort()),
                            "--plugins", this.getPluginsDirectory().getAbsolutePath(),
                            "nogui"
                    )).directory(this.getDirectory())
                    .start());
        } catch (IOException e) {
            log.error("An exception occurred while initializing server " + this, e);
            this.destroy();
        }
        StreamUtils.gobbleStream(this.getProcess().getInputStream(), this::handleOutputLine);
        StreamUtils.gobbleStream(this.getProcess().getErrorStream(), this::handleErrorLine);
        this.getProcess().onExit().thenAccept(p -> {
            try {
                this.destroy();
            } catch (IOException e) {
                log.error("Failed to destroy server on exit", e);
            }
        });
        this.setStartedAt(Instant.now().toEpochMilli());
        this.insertDatabase();
    }

    @Override
    public void sendMetadata() {
        this.getWs().sendData("METADATA", ImmutableMap.builder()
                .put("server", this.getId())
                .putAll(this.getMetadata())
                .build());
    }
}

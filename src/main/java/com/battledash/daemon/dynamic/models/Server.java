package com.battledash.daemon.dynamic.models;

import com.battledash.daemon.dynamic.Database;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import lombok.Data;
import com.battledash.daemon.dynamic.DaemonNode;
import com.battledash.daemon.dynamic.Env;
import com.battledash.daemon.dynamic.errors.NotFoundException;
import com.battledash.daemon.dynamic.socket.DaemonServerWebSocket;
import com.battledash.daemon.dynamic.socket.JsonClasses;
import com.battledash.daemon.dynamic.utils.ArchiveUtils;
import org.apache.commons.io.FileUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Pattern;

@Data
public abstract class Server {
    private static final Logger log = LoggerFactory.getLogger(Server.class);

    public static final Set<Server> SERVERS = Collections.newSetFromMap(Maps.newConcurrentMap());
    public static final File SERVERS_DIR = new File("./servers/");

    static {
        SERVERS_DIR.mkdirs();
    }

    public static Server getById(String id) {
        return SERVERS.stream().filter(s -> s.getId().equals(id)).findFirst().orElse(null);
    }

    private String typeId;
    private String id;
    private int port;
    @JsonIgnore
    private transient Process process;
    @JsonIgnore
    private transient DaemonServerWebSocket ws;
    private ServerState state = ServerState.STARTING;
    private Map<Object, Object> metadata;
    private Set<String> players = new LinkedHashSet<>(); // UUIDs
    private long startedAt;
    private boolean privateGame;
    private long lastActivity;

    public Server(String typeId, int port, Map<Object, Object> metadata) {
        this.typeId = typeId;
        this.port = port;
        this.metadata = metadata;
        if (!this.getTypeArchiveFile().exists()) {
            throw new NotFoundException("The typeId '" + typeId + "' does not exist or is not a cached archive type.");
        }
        SERVERS.add(this);
    }

    public void setState(ServerState state) {
        this.state = state;
        this.updateDatabase(Updates.set("state", state.name()));
        log.info("Updated {}'s state to {}", this, state);
    }

    public void setPlayers(Set<String> players) {
        this.players = players;
        updateDatabase(Updates.set("players", players));
    }

    /**
     * Join ticketing is a work in progress,
     * and does not fully work yet. The server
     * side of things are complete, I just need
     * to finish the daemon and networking side.
     *
     * @param playerId Player UUID requesting to join
     * @param customData Any data the player wants to ask the server to accommodate (for example, a world name)
     * @param callback Y/N Response from the game server
     */
    public void requestJoinTicket(String playerId, JSONObject customData, Consumer<Boolean> callback) {
        this.getWs().sendData("REQUEST_JOIN_TICKET", new JsonClasses.JoinTicketRequest(playerId));
    }

    /**
     * @return The directory that the game server runs in
     */
    public File getDirectory() {
        return Paths.get(SERVERS_DIR.getAbsolutePath(), this.id).toFile();
    }

    /**
     * @return The plugins directory of the game server
     */
    public File getPluginsDirectory() {
        return Paths.get(SERVERS_DIR.getAbsolutePath(), this.id, "plugins").toFile();
    }

    /**
     * Handle standard output from game server
     *
     * @param line Line of output from game server
     */
    public void handleOutputLine(String line) {
        if (Pattern.compile("^\\[\\d+:\\d+:\\d+ ERROR\\]:").matcher(line).find()) {
            log.error("An error occurred on server {}: {}", this, line);
        }
    }

    /**
     * Handle game server errors
     * @param line Line of output from game server
     */
    public void handleErrorLine(String line) {
        log.error("An error occurred on server {}: {}", this, line);
    }

    public File getTypeArchiveFile() {
        return Paths.get(DaemonNode.getInstance().getCacheUpdater().archives.getAbsolutePath(), this.getTypeId().toUpperCase() + ".tar.gz").toFile();
    }

    /**
     * Unpacks the archive to the server folder
     *
     * @throws IOException if decompressing the gametype archive fails
     */
    public void unpack() throws IOException {
        log.info("Unpacking " + this.getTypeId().toUpperCase() + " Archive to " + getDirectory().getName());
        ArchiveUtils.decompressFile(getTypeArchiveFile(), getDirectory());
    }

    /**
     * Kills the server and removes all of its data.
     *
     * @throws IOException if deleting the directory fails
     */
    public void destroy() throws IOException {
        log.info("Server {} exited", this);
        if (this.process.isAlive()) this.process.destroy();
        SERVERS.remove(this);
        FileUtils.deleteDirectory(getDirectory());
        removeDatabase();
    }

    public int hashCode() {
        return this.id.hashCode();
    }

    public boolean equals(Object o) {
        if (!(o instanceof Server)) return false;
        return ((Server) o).getId().equals(this.id);
    }

    /**
     * Initializes the server; creating the process,
     * inserting it into the {@link Database}, and handling
     * websocket connections.
     *
     * @throws IOException if deleting the folder fails in {@link Server#destroy}
     */
    public abstract void init() throws IOException;

    public abstract void sendMetadata();

    public void sendCustomPayload(String type, Document data) {
        this.getWs().sendData(type, data);
    }

    public void insertDatabase() {
        try {
            Document document = Document.parse(new Gson().toJson(this));
            document.put("_id", getId());
            document.put("node", Env.NODE);
            document.remove("id");
            DaemonNode.getInstance().getDatabase().getDynamicServers().insertOne(document);
        } catch (RuntimeException e) {
            log.error("An exception occurred while inserting " + this + " into the database", e);
            this.process.destroy();
        }
    }

    public void removeDatabase() {
        DaemonNode.getInstance().getDatabase().getDynamicServers().deleteOne(Filters.eq("_id", getId()));
    }

    public void updateDatabase(Bson update) {
        DaemonNode.getInstance().getDatabase().getDynamicServers().updateOne(Filters.eq("_id", getId()), update);
    }

    /**
     * Handle message from server's websocket connection
     *
     * @param message WebSocket Message
     */
    public void onMessage(JsonClasses.WebsocketData message) {
        switch (message.type) {
            case "SET_STATE": {
                this.setState(ServerState.valueOf(message.getData()));
                break;
            }
            case "UPDATE_PLAYERS": {
                List<String> data = message.getData();
                if (!data.equals(new ArrayList<>(getPlayers()))) {
                    log.info("Updated {}'s player list: {}", this, String.join(", ", data));
                    this.setPlayers(new LinkedHashSet<>(data));
                    if (this instanceof MiniServer && !this.metadata.containsKey("exitModifier") && this.players.size() <= 0) {
                        this.process.destroy();
                    }
                }
                break;
            }
            case "UPDATE_CUSTOM_DATA": {
                this.updateDatabase(Updates.set("customData", Document.parse(message.getData())));
                break;
            }
            case "PLAYER_LEAVE": {
                // TODO: 9/1/2021 Handle Player Leaves 
                break;
            }
            case "PLAYER_JOIN": {
                // TODO: 9/1/2021 Handle Player Joins
                break;
            }
        }
    }

    @Override
    public String toString() {
        return getId() + "/" + getTypeId();
    }

}

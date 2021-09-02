package com.battledash.daemon.dynamic;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import lombok.Getter;
import org.bson.Document;

/**
 * Handles database connection and collections.
 */
@Getter
public class Database {

    private final MongoClient client;
    private final MongoCollection<Document> dynamicServers;
    private final MongoCollection<Document> dynamicNodes;

    public Database() {
        client = MongoClients.create(Env.MONGO_CONNECT_STRING);
        dynamicServers = this.getDatabase().getCollection("dynamicServers");
        dynamicNodes = this.getDatabase().getCollection("dynamicNodes");
    }

    public MongoDatabase getDatabase() {
        return client.getDatabase(Env.MONGO_DATABASE);
    }

}

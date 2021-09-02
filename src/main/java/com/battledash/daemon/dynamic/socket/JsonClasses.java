package com.battledash.daemon.dynamic.socket;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

/**
 * Common classes used for serialization/deserialization of data.
 */
@Data
public class JsonClasses {

    @AllArgsConstructor
    @RequiredArgsConstructor
    public static class WebsocketData {
        private static transient final Gson GSON = new Gson();
        public final String type;
        private JsonElement data;
        public <T> T getData(Class<T> clazz) {
            return GSON.fromJson(data, clazz);
        }
        public <T> T getData() {
            return GSON.fromJson(data, new TypeToken<T>(){}.getType());
        }
    }

    @Data
    public static class ServerData {
        public final String id;
    }

    @Data
    public static class JoinTicketRequest {
        private final String playerId;
        private final String nonce = UUID.randomUUID().toString();
    }

}

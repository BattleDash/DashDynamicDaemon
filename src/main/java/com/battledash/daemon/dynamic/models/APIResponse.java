package com.battledash.daemon.dynamic.models;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A generic API response from Network Control.
 * @param <T>
 */
public class APIResponse<T> extends LinkedHashMap<String, Object> {
    public APIResponse(Map<String, Object> data) {
        putAll(data);
    }
    public APIResponse(boolean success) {
        this.put("success", success);
    }
    public APIResponse(boolean success, T data) {
        this.put("success", success);
        this.put("data", data);
    }
    public APIResponse(boolean success, String dataKey, T data) {
        this.put("success", success);
        this.put(dataKey, data);
    }
    public APIResponse<T> append(Map<String, Object> data) {
        this.putAll(data);
        return this;
    }
    public boolean isSuccessful() {
        return (boolean) getOrDefault("success", true);
    }
    public Map<String, Object> getData() {
        if (isSuccessful()) {
            return (Map<String, Object>) get("data");
        } else {
            throw new IllegalStateException("Request was not successful.");
        }
    }
}

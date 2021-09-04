package com.battledash.daemon.dynamic.utils;

import java.net.http.HttpClient;

/**
 * A global Java 11 HTTP Client to be used to contact various services.
 */
public class HTTPClient {
    public static final HttpClient CLIENT = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .build();
}

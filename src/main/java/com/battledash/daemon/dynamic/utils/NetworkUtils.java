package com.battledash.daemon.dynamic.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class NetworkUtils {

    private static String CACHED_IP;

    /**
     * Relies on Amazon AWS's IP checker to be functional, should probably make our own solution in the future.
     *
     * @return our Public IP.
     * @throws RuntimeException if AWS is down or the request fails.
     */
    public static String getPublicIPAddress() {
        if (CACHED_IP != null) return CACHED_IP;
        try (BufferedReader in = new BufferedReader(new InputStreamReader(new URL("http://checkip.amazonaws.com").openStream()))) {
            return CACHED_IP = in.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to get public IP address.");
        }
    }
}
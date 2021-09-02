package com.battledash.daemon.dynamic.utils;

import com.battledash.daemon.dynamic.models.Server;
import com.battledash.daemon.dynamic.Env;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utilities for retrieving/manipulating data from the server we're running on
 */
public class ServerUtils {

    /**
     * Get an unused port in the port rage.
     *
     * @return An unused port in the range.
     */
    public static int getUnusedPort() {
        List<Integer> collect = Arrays.stream(Env.SERVER_PORT_RANGE.split("-")).map(Integer::parseInt).collect(Collectors.toList());
        int port = (int) (Math.floor(Math.random() * (collect.get(1) - collect.get(0))) + collect.get(0));
        if (Server.SERVERS.stream().anyMatch(s -> s.getPort() == port)) {
            return getUnusedPort();
        }
        try (ServerSocket ignored = new ServerSocket(port)) {
            return port;
        } catch (IOException ignored) {
            return getUnusedPort();
        }
    }

}

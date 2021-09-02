package com.battledash.daemon.dynamic.providers;

import com.battledash.daemon.dynamic.Env;
import org.apache.commons.lang3.RandomStringUtils;
import org.glassfish.grizzly.http.server.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.container.*;
import javax.ws.rs.ext.Provider;
import java.util.concurrent.TimeUnit;

@Provider
@PreMatching
public class RequestLogger implements ContainerRequestFilter, ContainerResponseFilter {
    private static final Logger log = LoggerFactory.getLogger(RequestLogger.class);

    @Inject
    private javax.inject.Provider<Request> grizzlyRequestProvider;

    @Override
    public void filter(ContainerRequestContext request) {
        request.setProperty("loggingNonce", RandomStringUtils.randomAlphabetic(16));
        log.info("{} request to /{} from {}, nonce: {}",
                request.getMethod(),
                request.getUriInfo().getPath(),
                grizzlyRequestProvider.get().getRemoteAddr(),
                request.getProperty("loggingNonce"));
        request.setProperty("requestStartTime", System.nanoTime());
    }

    @Override
    public void filter(ContainerRequestContext request, ContainerResponseContext response) {
        long requestLength = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - ((long) request.getProperty("requestStartTime")));
        if (Env.DEBUG) {
            if (request.getPropertyNames().contains("authStartTime")) {
                log.info("{} completed in {}ms, auth took {}ms", request.getProperty("loggingNonce"),
                        requestLength,
                        TimeUnit.NANOSECONDS.toMillis(((long) request.getProperty("authEndTime")) - ((long) request.getProperty("authStartTime"))));
            } else {
                log.info("{} completed in {}ms, auth not required", request.getProperty("loggingNonce"),
                        requestLength);
            }
        }
        response.getHeaders().add("X-Daemon-Nonce", request.getProperty("loggingNonce"));
    }
}

package com.battledash.daemon.dynamic.providers;

import com.battledash.daemon.dynamic.Env;
import com.battledash.daemon.dynamic.errors.HTTPException;
import com.battledash.daemon.dynamic.models.APIResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Catches exceptions thrown from REST handlers, and sends a JSON error to the client.
 */
@Provider
public class RestExceptionMapper implements ExceptionMapper<Throwable> {
    private static final Logger log = LoggerFactory.getLogger(RestExceptionMapper.class);

    @Override
    public Response toResponse(Throwable exception) {
        if (Env.DEBUG) {
            log.error("Caught REST Exception", exception);
        }
        log.error("Caught Exception: " + exception.getClass().getSimpleName() + " - " + exception.getMessage());
        int statusCode = getStatusCode(exception);
        return Response.status(statusCode)
                .type(MediaType.APPLICATION_JSON)
                .entity(getEntity(exception))
                .build();
    }

    /*
     * Get appropriate HTTP status code for an exception.
     */
    private int getStatusCode(Throwable exception) {
        if (exception instanceof HTTPException) {
            return ((HTTPException) exception).statusCode;
        }
        if (exception instanceof WebApplicationException) {
            return ((WebApplicationException) exception).getResponse().getStatus();
        }
        return Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
    }

    /*
     * Get response body for an exception.
     */
    private APIResponse<String> getEntity(Throwable exception) {
        if (exception instanceof NotFoundException) {
            return getEntity(new com.battledash.daemon.dynamic.errors.NotFoundException("The requested resource was not found."));
        }
        APIResponse<String> response = new APIResponse<>(false, "error", Env.DEBUG ? (exception.getClass().getSimpleName() + ": " + exception.getMessage()) : exception.getMessage());
        if (Env.DEBUG) {
            response.put("stack", Arrays.stream(exception.getStackTrace()).map(StackTraceElement::toString).collect(Collectors.toList()));
        }
        return response;
    }
}

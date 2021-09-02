package com.battledash.daemon.dynamic;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvEntry;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Takes in input from a <code>.env</code> file and transforms it to Java variables.
 * There may be a better solution for this, but I don't feel like setting OS environment variables in a startup script.
 *
 * Example <code>.env</code> file:
 * <code>
 * NODE=1
 * SERVER_PORT_RANGE=5000-6000
 * REDIS_DATABASE=0
 * DEBUG=false
 * etc...
 * </code>
 */
public class Env {
    public static Integer NODE;
    public static String HOST;
    public static Integer PORT;
    public static String SERVER_PORT_RANGE;
    public static String NCP_HOST;
    public static String NCP_PORT;
    public static String NCP_SECRET;
    public static String MONGO_CONNECT_STRING;
    public static String MONGO_DATABASE;
    public static Integer REDIS_DATABASE;
    public static Boolean DEBUG;

    static {
        Dotenv dotenv = Dotenv.load();
        for (DotenvEntry entry : dotenv.entries()) {
            try {
                Field declaredField = Env.class.getDeclaredField(entry.getKey());
                declaredField.setAccessible(true);
                Object entryValue = entry.getValue();
                try {
                    Method valueOf = declaredField.getType().getDeclaredMethod("valueOf", String.class);
                    valueOf.setAccessible(true);
                    //noinspection JavaReflectionInvocation
                    entryValue = valueOf.invoke(null, entryValue);
                } catch (NoSuchMethodException ignored) {
                    // Purposefully ignored
                }
                declaredField.set(null, entryValue);
            } catch (NoSuchFieldException | IllegalAccessException | InvocationTargetException ignored) {
                // Purposefully ignored
            }
        }
    }
}

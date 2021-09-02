package com.battledash.daemon.dynamic.utils;

import java.io.InputStream;
import java.util.Scanner;
import java.util.function.Consumer;

/**
 * I/O Stream Utilities
 */
public class StreamUtils {

    /**
     * Starts a thread to handle output message from streams.
     *
     * @param source {@link InputStream} to receive data from, to pipe into {@param lineConsumer}.
     * @param lineConsumer The function to process each received line from the stream.
     */
    public static void gobbleStream(InputStream source, Consumer<String> lineConsumer) {
        Thread thread = new Thread(() -> {
            Scanner sc = new Scanner(source);
            while (sc.hasNextLine())
                lineConsumer.accept(sc.nextLine());
        });
        thread.setDaemon(true);
        thread.start();
    }

}

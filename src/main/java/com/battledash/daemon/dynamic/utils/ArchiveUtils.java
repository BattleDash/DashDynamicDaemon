package com.battledash.daemon.dynamic.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;

/**
 * Utilities for manipulating tar archives.
 */
public class ArchiveUtils {

    /**
     * Decompresses a gzipped tar file
     *
     * @param tarFile the file to be unzipped.
     * @param destFile the output archive file.
     *
     * @throws IOException if an I/O error has occurred in stream creation/reading
     */
    public static void decompressFile(File tarFile, File destFile) throws IOException {
        FileInputStream fis = new FileInputStream(tarFile);
        GZIPInputStream gis = new GZIPInputStream(fis);
        TarArchiveInputStream tis = new TarArchiveInputStream(gis);
        TarArchiveEntry tarEntry;
        while ((tarEntry = tis.getNextTarEntry()) != null) {
            File outputFile = new File(destFile + File.separator + tarEntry.getName());
            if (tarEntry.isDirectory()) {
                if (!outputFile.exists()) {
                    outputFile.mkdirs();
                }
            } else {
                outputFile.getParentFile().mkdirs();
                FileOutputStream fos = new FileOutputStream(outputFile);
                IOUtils.copy(tis, fos);
                fos.close();
            }
        }
        tis.close();
    }

}

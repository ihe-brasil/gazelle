package net.ihe.gazelle.tm.tee.util;

import net.ihe.gazelle.tm.tee.exception.TestExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Class containing utility methods for File I/O operations
 *
 * @author tnabeel
 */
public class FileUtil {

    private static final Logger LOG = LoggerFactory.getLogger(FileUtil.class);

    /**
     * Returns the contents of the file represented by the provided resourcePath
     *
     * @param resourcePath
     * @return
     */
    public static String loadFileContents(String resourcePath) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("String loadFileContents");
        }
        BufferedReader reader = null;
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(new File(resourcePath));
            reader = new BufferedReader(fileReader);
            StringBuilder builder = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            return builder.toString();
        } catch (IOException e) {
            throw new TestExecutionException("loadFileContents() failed", e);
        } finally {
            try {
                reader.close();
                fileReader.close();
            } catch (Exception e) {
            }
        }

    }

    /**
     * This method returns the resource contents as an input stream.  It looks for the resource
     * in the class path
     *
     * @param resource the name of the resource file
     * @return the resource contents as an input stream
     */
    public static InputStream loadResourceAsStream(String resource) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("InputStream loadResourceAsStream");
        }
        String stripped = resource.startsWith("/") ? resource.substring(1) : resource;

        InputStream resourceStream = null;
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader != null) {
            resourceStream = classLoader.getResourceAsStream(stripped);
        }
        if (resourceStream == null) {
            resourceStream = FileUtil.class.getResourceAsStream(resource);
        }
        if (resourceStream == null) {
            resourceStream = FileUtil.class.getClassLoader().getResourceAsStream(stripped);
        }
        if (resourceStream == null) {
            throw new RuntimeException(resource + " not found");
        }
        return resourceStream;
    }

    /**
     * This method returns the resource contents as a String
     *
     * @param resource the name of the resource file on the classpath
     * @return the resource contents as a String
     */
    public static String loadResourceAsString(String resource) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("String loadResourceAsString");
        }
        return loadStreamContentsAsStrBuilder(loadResourceAsStream(resource)).toString();
    }

    /**
     * This method returns the resource contents as a byte array
     *
     * @param resource
     * @return
     */
    public static byte[] loadResourceAsByteArray(String resource) {
        try {
            return loadStreamContentsAsStrBuilder(loadResourceAsStream(resource)).toString().getBytes(StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String loadStreamContentsAsString(InputStream inputStream) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("String loadStreamContentsAsString");
        }
        return loadStreamContentsAsStrBuilder(inputStream).toString();
    }

    /**
     * Returns the input stream contents as a String Builder using UTF8 as the encoding
     *
     * @param inputStream
     * @return
     */
    public static StringBuilder loadStreamContentsAsStrBuilder(InputStream inputStream) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("StringBuilder loadStreamContentsAsStrBuilder");
        }
        try {
            InputStreamReader isr = new InputStreamReader(new BufferedInputStream(inputStream), StandardCharsets.UTF_8.name());
            StringBuilder fileContents = new StringBuilder();
            int c = 0;
            while ((c = isr.read()) != -1) {
                fileContents.append((char) c);
            }
            isr.close();
            return fileContents;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

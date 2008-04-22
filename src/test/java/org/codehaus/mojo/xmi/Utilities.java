package org.codehaus.mojo.xmi;


import java.io.File;
import java.net.URL;

import org.codehaus.plexus.util.FileUtils;

/**
 * @author Jan Van Besien
 */
public class Utilities
{
    /**
     * Returns the location of a resource on the classpath as a {@link java.io.File} instance.
     *
     * @param path the location on the classpath
     * @return will be {@code null} in case the resource could not be located
     */
    static File getFile(String path)
    {
        final URL url = Thread.currentThread().getContextClassLoader().getResource(path);
        return url == null ? null : FileUtils.toFile(url);
    }
}

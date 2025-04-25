package com.vgerbot.propify.loader;

import com.vgerbot.propify.core.ResourceLoader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A ResourceLoader implementation that loads resources from the file system.
 * 
 * <p>This loader handles resources with the "file:" protocol prefix. It supports:
 * <ul>
 *   <li>Absolute file paths</li>
 *   <li>Relative file paths (relative to working directory)</li>
 *   <li>Platform-independent path handling</li>
 * </ul>
 *
 * <p>Example usage:
 * <pre>
 * file:/absolute/path/to/config.yaml
 * file:relative/path/to/config.properties
 * </pre>
 *
 * @since 1.1.0
 */
public class FileResourceLoader implements ResourceLoader {
    private static final String FILE_PREFIX = "file:";

    @Override
    public boolean accept(String location) {
        if (location == null) {
            return false;
        }
        return location.startsWith(FILE_PREFIX);
    }

    @Override
    public InputStream load(String location) throws IOException {
        if (location == null) {
            throw new IllegalArgumentException("Location cannot be null");
        }
        if (!location.startsWith(FILE_PREFIX)) {
            throw new IllegalArgumentException("Location must start with 'file:': " + location);
        }

        String filePath = location.substring(FILE_PREFIX.length()).trim();
        if (filePath.isEmpty()) {
            throw new IllegalArgumentException("File path cannot be empty");
        }

        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                throw new IOException("File does not exist: " + filePath);
            }
            if (!Files.isRegularFile(path)) {
                throw new IOException("Path is not a regular file: " + filePath);
            }
            if (!Files.isReadable(path)) {
                throw new IOException("File is not readable: " + filePath);
            }

            return Files.newInputStream(path);
        } catch (SecurityException e) {
            throw new IOException("Security error accessing file: " + filePath, e);
        } catch (IOException e) {
            throw new IOException("Error loading file: " + filePath, e);
        }
    }
}

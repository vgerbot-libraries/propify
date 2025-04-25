package com.vgerbot.propify.loader;

import com.vgerbot.propify.core.ResourceLoader;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * A ResourceLoader implementation that loads resources from HTTP/HTTPS URLs.
 * 
 * <p>This loader handles resources with "http:" and "https:" protocol prefixes. It supports:
 * <ul>
 *   <li>HTTP and HTTPS protocols</li>
 *   <li>Automatic redirect following</li>
 *   <li>Connection timeout handling</li>
 *   <li>Basic error handling and status code validation</li>
 * </ul>
 *
 * <p>Example usage:
 * <pre>
 * http://example.com/config.yaml
 * https://secure-server.com/app-config.properties
 * </pre>
 *
 * @since 1.1.0
 */
public class HTTPResourceLoader implements ResourceLoader {
    private static final int CONNECT_TIMEOUT = 10000; // 10 seconds
    private static final int READ_TIMEOUT = 10000; // 10 seconds
    private static final String HTTP_PREFIX = "http:";
    private static final String HTTPS_PREFIX = "https:";

    @Override
    public boolean accept(String location) {
        if (location == null) {
            return false;
        }
        return location.startsWith(HTTP_PREFIX) || location.startsWith(HTTPS_PREFIX);
    }

    @Override
    public InputStream load(String location) throws IOException {
        if (location == null) {
            throw new IllegalArgumentException("Location cannot be null");
        }
        if (!accept(location)) {
            throw new IllegalArgumentException("Location must start with 'http:' or 'https:': " + location);
        }

        try {
            URL url = new URL(location);
            URLConnection connection = url.openConnection();
            
            if (!(connection instanceof HttpURLConnection)) {
                throw new IOException("Not an HTTP connection: " + location);
            }

            HttpURLConnection httpConnection = (HttpURLConnection) connection;
            configureConnection(httpConnection);

            try {
                int responseCode = httpConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP 
                    || responseCode == HttpURLConnection.HTTP_MOVED_PERM 
                    || responseCode == HttpURLConnection.HTTP_SEE_OTHER) {
                    String newUrl = httpConnection.getHeaderField("Location");
                    httpConnection.disconnect();
                    return load(newUrl); // Handle redirect
                }

                if (responseCode != HttpURLConnection.HTTP_OK) {
                    throw new IOException("HTTP request failed with response code: " + responseCode);
                }

                return httpConnection.getInputStream();
            } catch (IOException e) {
                httpConnection.disconnect();
                throw e;
            }
        } catch (Exception e) {
            throw new IOException("Failed to load resource from URL: " + location, e);
        }
    }

    private void configureConnection(HttpURLConnection connection) {
        connection.setConnectTimeout(CONNECT_TIMEOUT);
        connection.setReadTimeout(READ_TIMEOUT);
        connection.setInstanceFollowRedirects(true);
        connection.setRequestProperty("User-Agent", "Propify-HTTPResourceLoader/1.0");
    }
}

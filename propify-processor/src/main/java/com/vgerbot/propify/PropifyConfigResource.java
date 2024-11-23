package com.vgerbot.propify;

import javax.annotation.processing.ProcessingEnvironment;
import java.io.IOException;
import java.io.InputStream;

public interface PropifyConfigResource {
    /**
     * Whether this resource accepts the given location.
     *
     * @param location A string like "classpath:foo/bar" or "http://domain/foo/bar".
     * @return true if this resource can read from the given location, false otherwise.
     */
    Boolean accept(String location);


    /**
     * Loads the resource from the specified location using the provided processing environment.
     *
     * @param processingEnvironment the processing environment to use for loading the resource
     * @param location the location of the resource to be loaded, e.g., "classpath:foo/bar"
     * @return an InputStream to read the resource content
     * @throws IOException if an I/O error occurs while loading the resource
     */
    InputStream load(ProcessingEnvironment processingEnvironment, String location) throws IOException;
}

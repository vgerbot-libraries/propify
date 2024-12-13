package com.vgerbot.propify;

import java.io.IOException;
import java.io.InputStream;

public interface ResourceLoader {
    boolean accept(String location);

    InputStream load(String location) throws IOException;
}

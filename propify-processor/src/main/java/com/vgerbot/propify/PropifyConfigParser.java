package com.vgerbot.propify;

import java.io.IOException;
import java.io.InputStream;

public interface PropifyConfigParser {
    PropifyProperties parse(InputStream stream) throws IOException;
    Boolean accept(String mediaType);
}

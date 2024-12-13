package com.vgerbot.propify;

public interface ResourceLoaderProvider {
    ResourceLoader getLoader(String location);
}

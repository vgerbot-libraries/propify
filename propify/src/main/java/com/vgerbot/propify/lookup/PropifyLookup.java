package com.vgerbot.propify.lookup;

public interface PropifyLookup {
    String getPrefix();
    Object lookup(String variable);
}
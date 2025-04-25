package com.vgerbot.propify.i18n;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;


public class RuntimePropifyResourceBundleControl extends AbstractPropifyResourceBundleControl {

    @Override
    protected InputStream loadResource(String resourceName, final ClassLoader classLoader, final boolean reloadFlag) throws IOException {
        try {
            return AccessController.doPrivileged(
                    (PrivilegedExceptionAction<InputStream>) () -> {
                        InputStream is = null;
                        if (reloadFlag) {
                            URL url = classLoader.getResource(resourceName);
                            if (url != null) {
                                URLConnection connection = url.openConnection();
                                if (connection != null) {
                                    // Disable caches to get fresh data for
                                    // reloading.
                                    connection.setUseCaches(false);
                                    is = connection.getInputStream();
                                }
                            }
                        } else {
                            is = classLoader.getResourceAsStream(resourceName);
                        }
                        return is;
                    });
        } catch (PrivilegedActionException e) {
            throw (IOException)e.getException();
        }
    }
}

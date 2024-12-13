package com.vgerbot.example.jdk8;

import com.vgerbot.propify.*;
import com.vgerbot.propify.runtime.RuntimeResourceLoaderProvider;

import java.io.IOException;
import java.io.InputStream;

@Propify(
        location = "classpath: application.properties",
        generatedClassName = "ApplicationPropertiesPropify"
)
public class PropertiesConfig {
//    private static class Database {
//        private final PropifyProperties properties;
//        private Database(PropifyProperties properties) {
//            this.properties = properties;
//        }
//        String getUrl() {
//            return (String)properties.get("url");
//        }
//    }
//    private static class Server {
//        private final PropifyProperties properties;
//        private Server(PropifyProperties properties) {
//            this.properties = properties;
//        }
//        public Integer getPort() {
//            return (Integer)properties.get("port");
//        }
//    }
//
//    public Database getDatabase() {
//        return new Database((PropifyProperties) properties.get("database"));
//    }
//    public Server getServer() {
//        return new Server((PropifyProperties) properties.get("server"));
//    }
//
//    private final PropifyProperties properties;
//    private PropertiesConfig(PropifyProperties properties) {
//        this.properties = properties;
//    }
//    public static PropertiesConfig getInstance() throws RuntimeException {
//        PropifyContext context = new PropifyContext(
//                "",
//                "",
//                false,
//                "$$Propify",
//                RuntimeResourceLoaderProvider.getInstance()
//        );
//        try {
//            InputStream stream = context.loadResource();
//            PropifyConfigParserProvider parserProvider = PropifyConfigParserProvider.getInstance();
//            PropifyConfigParser parser = parserProvider.getParser(context.getMediaType());
//            PropifyProperties properties = parser.parse(context, stream);
//            return new PropertiesConfig(properties);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
}


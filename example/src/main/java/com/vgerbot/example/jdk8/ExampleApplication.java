package com.vgerbot.example.jdk8;

public class ExampleApplication {
    public static void main(String[] args) {

        System.out.println("==================== JAVA PROPERTIES ============================");
        ApplicationPropertiesPropify props = new ApplicationPropertiesPropify();

        System.out.println("app.name = " + props.getApp().getName());
        System.out.println("app.version = " + props.getApp().getVersion());
        System.out.println("app.description = " + props.getApp().getDescription());

        System.out.println("database.url = " + props.getDatabase().getUrl());
        System.out.println("database.username = " + props.getDatabase().getUsername());
        System.out.println("database.password = " + props.getDatabase().getPassword());

        System.out.println("server.host = " + props.getServer().getHost());
        System.out.println("server.port = " + props.getServer().getPort());

        System.out.println("==================== YAML PROPERTIES ============================");

        ApplicationYAMLPropify yamlProps = new ApplicationYAMLPropify();

        System.out.println("app.name = " + yamlProps.getApp().getName());
        System.out.println("app.version = " + yamlProps.getApp().getVersion());
        System.out.println("app.description = " + yamlProps.getApp().getDescription());

        System.out.println("database.url = " + yamlProps.getDatabase().getUrl());
        System.out.println("database.username = " + yamlProps.getDatabase().getUsername());
        System.out.println("database.password = " + yamlProps.getDatabase().getPassword());

        System.out.println("server.host = " + yamlProps.getServer().getHost());
        System.out.println("server.port = " + yamlProps.getServer().getPort());
    }
}

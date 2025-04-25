package com.vgerbot.example;

import com.ibm.icu.text.MessageFormat;
import com.vgerbot.propify.core.PropifyPropertiesBuilder;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.YAMLConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.event.EventType;
import org.apache.commons.configuration2.interpol.ConfigurationInterpolator;
import org.apache.commons.configuration2.interpol.DefaultLookups;
import org.apache.commons.configuration2.interpol.Lookup;
import org.apache.commons.configuration2.io.FileHandler;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.slf4j.helpers.MessageFormatter;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Main {
    public static void main(String[] args) throws Exception {
        YAMLConfiguration configuration = new YAMLConfiguration();
        configuration.read(Main.class.getClassLoader().getResourceAsStream("test.yaml"));

        Iterator<String> keys = configuration.getKeys();


        visitNode(configuration.getNodeModel().getRootNode(), (node, path) -> {
            System.out.println(path);
            System.out.println(node.getValue());
        }, new ArrayList<>());
//
//        Object envAll = configuration.getProperty("env.all");
//        System.out.println(envAll);
//        Object i = configuration.getInterpolator().interpolate(envAll);
//        System.out.println(i);
//
//        List<Object> list = configuration.getList("env.all");
//        System.out.println(list);
    }
    @FunctionalInterface()
    interface NodeVisitor {
        void visit(ImmutableNode node, List<String> path);
    }
    private static void visitNode(ImmutableNode rootNode, NodeVisitor visitor, List<String> path) {
        rootNode.stream().forEach(childNode -> {
            List<String> childPath = new ArrayList<>(path);
            childPath.add(childNode.getNodeName());
            visitor.visit(childNode, childPath);
            visitNode(childNode, visitor, childPath);
        });
    }
}

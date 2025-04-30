package com.vgerbot.propify.i18n;

import com.ibm.icu.text.MessagePattern;
import com.ibm.icu.text.MessagePattern.ArgType;
import com.ibm.icu.text.MessagePattern.Part;

import java.util.*;
import java.util.stream.Collectors;

final class None {
}

public class ICUTemplateArgumentsParser {
    public static final class Argument {
        private final String name;
        private final Class<?> type;

        public Argument(String name, Class<?> type) {
            this.name = name;
            this.type = type == None.class ? String.class : type;
        }

        public String getName() {
            return name;
        }

        public Class<?> getType() {
            return type;
        }

        @Override
        public String toString() {
            return "Argument{" +
                    "name='" + name + '\'' +
                    ", type=" + type +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            Argument argument = (Argument) o;
            return Objects.equals(name, argument.name) && Objects.equals(type, argument.type);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, type);
        }
    }

    private static class Placeholder {
        public String name;
        public String type;
        public String style;

        public Placeholder(String name, String type, String style) {
            this.name = name;
            this.type = type;
            this.style = style;
        }

        @Override
        public String toString() {
            return "Name: " + name + ", Type: " + type + ", Style: " + style;
        }
    }

    public static List<Argument> parseTemplate(String pattern) {
        Map<String, List<Placeholder>> arguments = new LinkedHashMap<>();

        MessagePattern messagePattern = new MessagePattern(pattern);

        for (int i = 0; i < messagePattern.countParts(); i++) {
            Part part = messagePattern.getPart(i);

            if (part.getType() == Part.Type.ARG_START) {
                String name = messagePattern.getSubstring(messagePattern.getPart(i + 1));
                ArgType argType = part.getArgType();
                String typeName = argType.name();

                String style = "";
                if (argType != ArgType.NONE && argType != ArgType.SIMPLE) {
                    style = argType.toString();
                } else if (argType == ArgType.SIMPLE) {
                    if (i + 2 < messagePattern.countParts()) {
                        Part argTypePart = messagePattern.getPart(i + 2);
                        if (argTypePart.getType() == Part.Type.ARG_TYPE) {
                            typeName = messagePattern.getSubstring(argTypePart);
                        }
                    }
                    int styleIndex = i + 1;
                    if (styleIndex < messagePattern.countParts() &&
                            messagePattern.getPart(styleIndex).getType() == Part.Type.ARG_STYLE) {
                        style = messagePattern.getSubstring(messagePattern.getPart(styleIndex));
                    }
                }

                if (argType == ArgType.NONE) {
                    typeName = null;
                }
                List<Placeholder> argumentInfos = arguments.computeIfAbsent(name, k -> new ArrayList<>());
                argumentInfos.add(new Placeholder(name, typeName, style));
            }
        }
        return arguments.entrySet().stream().map(entry -> {
            String name = entry.getKey();
            Set<? extends Class<?>> classes = entry.getValue().stream().map(it -> resolveType(it.type)).collect(Collectors.toSet());

            if (classes.size() == 1) {
                return new Argument(name, classes.stream().findFirst().get());
            }
            if (classes.size() > 1) {
                Set<? extends Class<?>> classes1 = classes.stream().filter(it -> it != None.class).collect(Collectors.toSet());
                if (classes1.size() != classes.size()) {
                    return new Argument(name, classes1.stream().findFirst().get());
                }
            }
            return new Argument(name, Object.class);
        }).collect(Collectors.toList());
    }

    private static Class<?> resolveType(String typeName) {
        if (typeName == null) {
            return None.class;
        }
        switch (typeName.toLowerCase()) {
            case "string":
            case "select":
                return String.class;
            case "number":
            case "plural":
            case "selectordinal":
            case "choice":
                return Number.class;
            case "date":
            case "time":
                return Date.class;
        }
        return Object.class;
    }
}
package com.vgerbot.propify.common;

import com.ibm.icu.text.MessagePattern;
import com.ibm.icu.text.MessagePattern.Part;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MessageFormatParser {

    public static List<PlaceholderInfo> parsePlaceholders(String pattern) {
        MessagePattern msgPattern = new MessagePattern(pattern);
        List<PlaceholderInfo> placeholders = new ArrayList<>();

        for (int i = 0; i < msgPattern.countParts(); i++) {
            Part part = msgPattern.getPart(i);
            if (part.getType() == MessagePattern.Part.Type.ARG_START) {
                String name = null;

                for_inner: for (i = i + 1; i < msgPattern.countParts(); i++) {
                    final Part nextPart = msgPattern.getPart(i);
                    final Part.Type type = nextPart.getType();

                    switch (type) {
                        case ARG_START:
                            i --;
                            break for_inner;
                        case ARG_NAME:
                        case ARG_NUMBER:
                            name = msgPattern.getSubstring(nextPart);
                            break;
                        case ARG_TYPE:
                            final String formatType = msgPattern.getSubstring(nextPart);
                            placeholders.add(new PlaceholderInfo(name, formatType));
                            break for_inner;
                        case ARG_INT:
                            placeholders.add(new PlaceholderInfo(name, "number"));
                            break for_inner;
                        case ARG_SELECTOR:
                            placeholders.add(new PlaceholderInfo(name, "select"));
                            break for_inner;
                        case ARG_LIMIT:
                            placeholders.add(new PlaceholderInfo(name, null));
                            break for_inner;
                    }
                }
            }
        }

        return placeholders;
    }

    public static final class PlaceholderInfo {

        private final String name;
        private final String formatType;

        public PlaceholderInfo(String name, String formatType) {
            this.name = name;
            this.formatType = formatType;
        }

        // Getters
        public String getName() { return name; }
        public String getFormatType() { return formatType; }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            PlaceholderInfo that = (PlaceholderInfo) o;
            return Objects.equals(name, that.name) && Objects.equals(formatType, that.formatType);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, formatType);
        }

    }
}
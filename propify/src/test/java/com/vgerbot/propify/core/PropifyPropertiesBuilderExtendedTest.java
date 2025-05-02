package com.vgerbot.propify.core;

import org.apache.commons.configuration2.MapConfiguration;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Extended tests for PropifyPropertiesBuilder to improve coverage of complex
 * scenarios.
 */
public class PropifyPropertiesBuilderExtendedTest {

    private PropifyPropertiesBuilder builder;

    @Before
    public void setUp() {
        builder = new PropifyPropertiesBuilder();
    }

    @Test
    public void testInvalidTypeFormat() {
        Map<String, Object> map = new HashMap<>();
        map.put("invalidType(<>)", "value");

        try {
            builder.config(new MapConfiguration(map)).build();
        } catch (PropifyPropertiesBuilder.PropifyTypeConversionException e) {
            assertThat(e.getCause(), instanceOf(IllegalArgumentException.class));
            assertThat(e.getCause().getMessage(), containsString("Invalid type format"));
        }
    }

    @Test
    public void testComplexGenericTypesParsing() {
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> innerMap = new HashMap<>();
        innerMap.put("key", Arrays.asList(1, 2, 3));
        map.put("nestedGeneric", innerMap);

        PropifyProperties props = builder.config(new MapConfiguration(map)).build();

        PropifyProperties nestedMap = (PropifyProperties) props.get("nestedGeneric");
        assertThat(nestedMap, is(notNullValue()));
        assertThat(nestedMap.size(), is(1));

        List<?> nestedList = (List<?>) nestedMap.get("key");
        assertThat(nestedList, is(notNullValue()));
        assertThat(nestedList.size(), is(3));
        assertThat(nestedList.contains(1), is(true));
        assertThat(nestedList.contains(2), is(true));
        assertThat(nestedList.contains(3), is(true));
    }

    @Test
    public void testNullValueHandling() {
        Map<String, Object> map = new HashMap<>();
        map.put("nullValue", null);

        PropifyProperties props = builder.config(new MapConfiguration(map)).build();

        assertThat(props.containsKey("nullValue"), is(true));
        assertThat(props.get("nullValue"), is(nullValue()));
    }

    @Test
    public void testEmptyStringValueHandling() {
        Map<String, Object> map = new HashMap<>();
        map.put("emptyString", "");

        PropifyProperties props = builder.config(new MapConfiguration(map)).build();

        assertThat(props.get("emptyString"), is(""));
    }

    @Test
    public void testNestedMapWithNullValues() {
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> nestedMap = new HashMap<>();
        nestedMap.put("key1", "value1");
        nestedMap.put("key2", null);
        map.put("nestedMap", nestedMap);

        PropifyProperties props = builder.config(new MapConfiguration(map)).build();

        PropifyProperties result = (PropifyProperties) props.get("nestedMap");
        assertThat(result, is(notNullValue()));
        assertThat(result.get("key1"), is("value1"));
        assertThat(result.containsKey("key2"), is(true));
        assertThat(result.get("key2"), is(nullValue()));
    }

    @Test
    public void testListWithMixedTypes() {
        Map<String, Object> map = new HashMap<>();
        List<Object> mixedList = new ArrayList<>();
        mixedList.add("string");
        mixedList.add(123);
        mixedList.add(true);
        map.put("mixedList", mixedList);

        PropifyProperties props = builder.config(new MapConfiguration(map)).build();

        List<?> result = (List<?>) props.get("mixedList");
        assertThat(result, is(notNullValue()));
        assertThat(result.size(), is(3));
        assertThat(result.get(0), is("string"));
        assertThat(result.get(1), is(123));
        assertThat(result.get(2), is(true));
    }

    @Test
    public void testDeepNestedStructure() {
        Map<String, Object> map = new HashMap<>();
        map.put("level1.level2.level3.level4.level5.value", "deeplyNested");

        PropifyProperties props = builder.config(new MapConfiguration(map)).build();

        PropifyProperties level1 = (PropifyProperties) props.get("level1");
        assertThat(level1, is(notNullValue()));

        PropifyProperties level2 = (PropifyProperties) level1.get("level2");
        assertThat(level2, is(notNullValue()));

        PropifyProperties level3 = (PropifyProperties) level2.get("level3");
        assertThat(level3, is(notNullValue()));

        PropifyProperties level4 = (PropifyProperties) level3.get("level4");
        assertThat(level4, is(notNullValue()));

        PropifyProperties level5 = (PropifyProperties) level4.get("level5");
        assertThat(level5, is(notNullValue()));

        assertThat(level5.get("value"), is("deeplyNested"));
    }
}

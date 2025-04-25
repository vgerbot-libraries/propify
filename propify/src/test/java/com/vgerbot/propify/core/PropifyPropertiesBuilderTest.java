package com.vgerbot.propify.core;

import org.apache.commons.configuration2.MapConfiguration;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.util.*;

import static org.hamcrest.MatcherAssert.*;

import static org.hamcrest.CoreMatchers.*;

public class PropifyPropertiesBuilderTest {
    private PropifyPropertiesBuilder builder;

    @Before
    public void setUp() {
        builder = new PropifyPropertiesBuilder();
    }

    @Test(expected = IllegalStateException.class)
    public void testBuildWithoutConfig() {
        builder.build();
    }

    @Test
    public void testBasicTypes() {
        Map<String, Object> map = new HashMap<>();
        map.put("stringValue", "test");
        map.put("intValue(int)", "42");
        map.put("boolValue(boolean)", "true");
        map.put("doubleValue(double)", "3.14");
        map.put("longValue(long)", "1234567890");
        map.put("charValue(char)", "A");

        PropifyProperties props = builder.config(new MapConfiguration(map)).build();

        assertThat("test", is(props.get("stringValue")));
        assertThat(42, is(props.get("intValue")));
        assertThat(true, is(props.get("boolValue")));
        assertThat(Math.abs(3.14 - (Double) props.get("doubleValue")) <= 0.001, is(true));
        assertThat(1234567890L, is(props.get("longValue")));
        assertThat('A', is(props.get("charValue")));
    }

    @Test
    public void testListTypes() {
        Map<String, Object> map = new HashMap<>();
        map.put("stringList(List<String>)", Arrays.asList("a", "b", "c"));
        map.put("intList(List<int>)", Arrays.asList("1", "2", "3"));

        PropifyProperties props = builder.config(new MapConfiguration(map)).build();

        List<?> stringList = (List<?>) props.get("stringList");
        assertThat(stringList, is(Arrays.asList("a", "b", "c")));

        List<?> intList = (List<?>) props.get("intList");
        assertThat(intList, is(Arrays.asList(1, 2, 3)));
    }

    @Test
    public void testSetTypes() {
        Map<String, Object> map = new HashMap<>();
        map.put("stringSet(Set<String>)", Arrays.asList("a", "b", "b", "c"));

        PropifyProperties props = builder.config(new MapConfiguration(map)).build();

        Set<?> stringSet = (Set<?>) props.get("stringSet");
        assertThat(stringSet, is(new HashSet<>(Arrays.asList("a", "b", "c"))));
    }

    @Test
    public void testArrayTypes() {
        Map<String, Object> map = new HashMap<>();
        map.put("stringArray(string[])", Arrays.asList("a", "b", "c"));
        map.put("intArray(int[])", Arrays.asList("1", "2", "3"));

        PropifyProperties props = builder.config(new MapConfiguration(map)).build();

        Object[] stringArray = (Object[]) props.get("stringArray");
        assertThat(stringArray, is(new String[]{"a", "b", "c"}));

        Object[] intArray = (Object[]) props.get("intArray");
        assertThat(intArray, is(new Integer[]{1, 2, 3}));
    }

    @Test
    public void testNestedProperties() {
        Map<String, Object> map = new HashMap<>();
        map.put("parent.child.stringValue", "test");
        map.put("parent.child.intValue(int)", "42");

        PropifyProperties props = builder.config(new MapConfiguration(map)).build();

        PropifyProperties parent = (PropifyProperties) props.get("parent");
        assertThat(parent, is(notNullValue()));
        PropifyProperties child = (PropifyProperties) parent.get("child");
        assertThat(child, is(notNullValue()));
        assertThat(child.get("stringValue"), is("test"));
        assertThat(child.get("intValue"), is(42));
    }

    @Test
    public void testMapTypes() {
        Map<String, Object> map = new HashMap<>();
        map.put("stringMap.key1", "value1");
        map.put("stringMap.key2", "value2");
        map.put("intMap.key1(int)", "1");
        map.put("intMap.key2(int)", "2");

        PropifyProperties props = builder.config(new MapConfiguration(map)).build();

        Map<?, ?> stringMap = (Map<?, ?>) props.get("stringMap");
        assertThat(stringMap.get("key1"), is("value1"));
        assertThat(stringMap.get("key2"), is("value2"));

        Map<?, ?> intMap = (Map<?, ?>) props.get("intMap");
        assertThat(intMap.get("key1"), is(1));
        assertThat(intMap.get("key2"), is(2));
    }

//    @Test
//    public void testInvalidTypeConversion() {
//        Map<String, Object> map = new HashMap<>();
//        map.put("invalid(NonExistentType)", "value");
//
//        builder.config(new MapConfiguration(map)).build();
//    }

    @Test
    public void testEmptyCollections() {
        Map<String, Object> map = new HashMap<>();
        map.put("emptyList(List<String>)", Collections.emptyList());
        map.put("emptySet(Set<String>)", Collections.emptyList());
        map.put("emptyMap(Map<String,String>)", Collections.emptyMap());

        PropifyProperties props = builder.config(new MapConfiguration(map)).build();

        assertThat(((List<?>) props.get("emptyList")).isEmpty(), is(true));
        assertThat(((Set<?>) props.get("emptySet")).isEmpty(), is(true));
        assertThat(((Map<?, ?>) props.get("emptyMap")).isEmpty(), is(true));
    }

    @Test
    public void testComplexNestedTypes() {
        Map<String, Object> map = new HashMap<>();
        map.put("complex(List<Map<String,List<int>>>)", Arrays.asList(
            Collections.singletonMap("key1", Arrays.asList("1", "2")),
            Collections.singletonMap("key2", Arrays.asList("3", "4"))
        ));

        PropifyProperties props = builder.config(new MapConfiguration(map)).build();

        List<?> complex = (List<?>) props.get("complex");
        assertThat(complex.size(), is(2));
        
        Map<?, ?> firstMap = (Map<?, ?>) complex.get(0);
        List<?> firstList = (List<?>) firstMap.get("key1");
        assertThat(firstList, is(Arrays.asList(1, 2)));

        Map<?, ?> secondMap = (Map<?, ?>) complex.get(1);
        List<?> secondList = (List<?>) secondMap.get("key2");
        assertThat(secondList, is(Arrays.asList(3, 4)));
    }
    
    @Test
    public void testBigDecimalAndBigInteger() {
        Map<String, Object> map = new HashMap<>();
        map.put("decimal(BigDecimal)", "123.456");
        map.put("integer(BigInteger)", "9876543210");

        PropifyProperties props = builder.config(new MapConfiguration(map)).build();

        assertThat(props.get("decimal"), instanceOf(BigDecimal.class));
        assertThat(props.get("integer"), instanceOf(BigInteger.class));
        
        assertThat(new BigDecimal("123.456"), is(props.get("decimal")));
        assertThat(new BigInteger("9876543210"), is(props.get("integer")));
    }
    
    @Test
    public void testTemporalTypes() {
        Map<String, Object> map = new HashMap<>();
        map.put("localDate(LocalDate)", "2023-05-15");
        map.put("localTime(LocalTime)", "14:30:45");
        map.put("localDateTime(LocalDateTime)", "2023-05-15T14:30:45");
        map.put("date(Date)", "2023-05-15T14:30:45Z");
        map.put("instant(Instant)", "2023-05-15T14:30:45Z");
        map.put("duration(Duration)", "PT1H30M");
        map.put("period(Period)", "P1Y2M15D");

        PropifyProperties props = builder.config(new MapConfiguration(map)).build();

        assertThat(props.get("localDate"), is(LocalDate.parse("2023-05-15")));
        assertThat(props.get("localTime"), is(LocalTime.parse("14:30:45")));
        assertThat(props.get("localDateTime"), is(LocalDateTime.parse("2023-05-15T14:30:45")));
        assertThat(props.get("date"), is(Date.from(Instant.parse("2023-05-15T14:30:45Z"))));
        assertThat(props.get("instant"), is(Instant.parse("2023-05-15T14:30:45Z")));
        assertThat(props.get("duration"), is(Duration.parse("PT1H30M")));
        assertThat(props.get("period"), is(Period.parse("P1Y2M15D")));

    }
    
    @Test
    public void testMultiLevelNesting() {
        Map<String, Object> map = new HashMap<>();
        map.put("level1.level2.level3.value", "deepValue");
        map.put("level1.level2.level3.number(int)", "42");
        map.put("level1.siblingValue", "sibling");

        PropifyProperties props = builder.config(new MapConfiguration(map)).build();

        PropifyProperties level1 = (PropifyProperties) props.get("level1");
        assertThat(level1, is(notNullValue()));
        assertThat(level1.get("siblingValue"), is("sibling"));
        
        PropifyProperties level2 = (PropifyProperties) level1.get("level2");
        assertThat(level2, is(notNullValue()));
        
        PropifyProperties level3 = (PropifyProperties) level2.get("level3");
        assertThat(level3, is(notNullValue()));
        assertThat(level3.get("value"), is("deepValue"));
        assertThat(level3.get("number"), is(42));
    }
    
    @Test
    public void testNestedListOfProperties() {
        Map<String, Object> map = new HashMap<>();
        List<Object> complexList = new ArrayList<>();
        
        Map<String, Object> item1 = new HashMap<>();
        item1.put("name", "item1");
        item1.put("value(int)", "10");
        
        Map<String, Object> item2 = new HashMap<>();
        item2.put("name", "item2");
        item2.put("value(int)", "20");
        
        complexList.add(item1);
        complexList.add(item2);
        
        map.put("items", complexList);

        PropifyProperties props = builder.config(new MapConfiguration(map)).build();
        
        List<?> items = (List<?>) props.get("items");
        assertThat(items.size(), is(2));
        
        Map<?, ?> prop1 =  (Map<?, ?>)items.get(0);
        assertThat(prop1.get("name"), is("item1"));
        assertThat(prop1.get("value"), is(10));
        
        Map<?, ?> prop2 = (Map<?, ?>) items.get(1);
        assertThat(prop2.get("name"), is("item2"));
        assertThat(prop2.get("value"), is(20));
    }
    
    @Test(expected = PropifyPropertiesBuilder.PropifyTypeConversionException.class)
    public void testInvalidTemporalConversion() {
        Map<String, Object> map = new HashMap<>();
        map.put("invalidDate(LocalDate)", "not-a-date");

        builder.config(new MapConfiguration(map)).build();
    }
}

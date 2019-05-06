package ru.iopump.jdbi.db.entity;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Test;
import ru.iopump.jdbi.db.entity.Entity.KeyValueMap;

import static org.assertj.core.api.Assertions.assertThat;

public class KeyValueMapTest {

    @Test
    public void testCreation() {
        final Map<String, Object> mapOne = new HashMap<>();
        mapOne.put("1", 1);
        mapOne.put("2", 1);
        mapOne.put("3", 3);
        mapOne.put("4", 4);
        mapOne.put("5", 5);
        mapOne.put("1null", null);
        mapOne.put("2null", null);

        KeyValueMap res = new KeyValueMap(mapOne, true);
        assertThat(res.getSourceMap()).containsAllEntriesOf(mapOne);

        res = new KeyValueMap(mapOne, false);
        assertThat(res.getSourceMap())
                .containsKeys("1", "2", "3", "4", "5")
                .doesNotContainKeys("1null", "1null");
    }

    @Test
    public void testGetLists() {
        final Map<String, Object> mapOne = new HashMap<>();
        mapOne.put("1null", null);
        mapOne.put("1", 1);
        mapOne.put("2", 1);
        mapOne.put("3", 3);
        mapOne.put("4", 4);
        mapOne.put("5", 5);
        mapOne.put("2null", null);

        KeyValueMap res = new KeyValueMap(mapOne);
        List<String> keys = res.getKeysAsList();
        assertThat(keys).containsExactlyElementsOf(mapOne.keySet());

        List<Object> values = res.getValuesAsList();
        assertThat(values).isEqualTo(keys.stream().map(mapOne::get).collect(Collectors.toList()));
    }

    @Test
    public void testMinus() {
        Map<String, Object> mapOne = new HashMap<>();
        mapOne.put("1null", null);
        mapOne.put("1", 1);


        Map<String, Object> mapTwo = new HashMap<>();
        mapTwo.put("1null", null);
        mapTwo.put("1", 1);

        KeyValueMap one = new KeyValueMap(mapOne);
        KeyValueMap two = new KeyValueMap(mapTwo);

        assertThat(one.minus(two, true).getSourceMap()).isEmpty();
        assertThat(one.minus(two, false).getSourceMap()).isEmpty();
        //////////////////
        mapOne = new HashMap<>();
        mapOne.put("1null", null);
        mapOne.put("1", 1);
        mapOne.put("2null", null);

        one = new KeyValueMap(mapOne);
        two = new KeyValueMap(mapTwo);

        assertThat(one.minus(two, true).getSourceMap()).containsOnly(new AbstractMap.SimpleEntry<>("2null", null));
        assertThat(one.minus(two, false).getSourceMap()).isEmpty();
        //////////////////
        mapOne = new HashMap<>();
        mapOne.put("1null", null);
        mapOne.put("1", 2);
        mapOne.put("2null", 2);
        mapOne.put("3null", null);

        one = new KeyValueMap(mapOne);
        two = new KeyValueMap(mapTwo);

        assertThat(one.minus(two, true).getSourceMap())
                .containsOnly(new AbstractMap.SimpleEntry<>("1", 2),
                        new AbstractMap.SimpleEntry<>("3null", null),
                        new AbstractMap.SimpleEntry<>("2null", 2));
        assertThat(one.minus(two, false).getSourceMap())
                .containsOnly(new AbstractMap.SimpleEntry<>("1", 2),
                        new AbstractMap.SimpleEntry<>("2null", 2));
    }
}
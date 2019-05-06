package ru.iopump.jdbi.db.helper;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertThat;
import static ru.iopump.jdbi.db.helper.TableAsserts.containsAllEntries;
import static ru.iopump.jdbi.db.helper.TableAsserts.valueIs;


public class TableAssertsTest {

    @Test
    public void testTableHasRow() {
        final Map<String, String> actualRow1 = ImmutableMap.of("1", "one");
        final Map<String, String> actualRow2 = ImmutableMap.of("1", "two");
        final Set<Map<String, String>> actualTable = ImmutableSet.of(actualRow1, actualRow2);

        final Map<String, String> expected = ImmutableMap.of("1", "not_value", "2", "not_value");

        Assertions.assertThatThrownBy(() -> assertThat(actualTable, hasItem(containsAllEntries(expected))))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("1 -> equalToIgnoringCase(\"not_value\")")
                .hasMessageContaining("2 -> equalToIgnoringCase(\"not_value\")")

                .hasMessageContaining("у колонки 1 актуальное значение: \"one\"")
                .hasMessageContaining("колонка 2 не найдена")
                .hasMessageContaining("вся запись: [<1=one>]")

                .hasMessageContaining("у колонки 1 актуальное значение: \"two\"")
                .hasMessageContaining("колонка 2 не найдена")
                .hasMessageContaining("вся запись: [<1=two>]");
    }

    @Test
    public void testContainsAllEntries() {
        final Map<String, String> actualRow = ImmutableMap.of("1", "value");

        final Map<String, String> expected1 = ImmutableMap.of("1", "not_value");
        Assertions.assertThatThrownBy(() -> assertThat(actualRow, containsAllEntries(expected1)))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("1 -> equalToIgnoringCase(\"not_value\")")
                .hasMessageContaining("у колонки 1 актуальное значение: \"value\"")
                .hasMessageContaining("вся запись: [<1=value>]");


        final Map<String, String> expected2 = ImmutableMap.of("0", "not_value");
        Assertions.assertThatThrownBy(() -> assertThat(actualRow, containsAllEntries(expected2)))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("0 -> equalToIgnoringCase(\"not_value\")")
                .hasMessageContaining("колонка 0 не найдена")
                .hasMessageContaining("вся запись: [<1=value>]");
    }

    @Test
    public void testValueIs() {
        final String actual = "actual";
        final String notActual = "notActual";
        Assertions.assertThatThrownBy(() -> assertThat(actual, valueIs("{regexp:тест}")))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("Expected: a String matching the regular expression (?i)тест")
                .hasMessageContaining("but: was \"actual\"");

        Assertions.assertThatThrownBy(() -> assertThat(actual, valueIs(notActual)))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("Expected: equalToIgnoringCase(\"notActual\")")
                .hasMessageContaining("but: was actual");
    }
}
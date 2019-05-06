package ru.iopump.jdbi.db.helper;

import static java.lang.String.format;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import io.cucumber.datatable.DataTable;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Matcher;
import ru.iopump.jdbi.cucumber.DbHelper;
import ru.iopump.jdbi.db.entity.Entity;

import static com.jcabi.matchers.RegexMatchers.matchesPattern;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static ru.iopump.jdbi.db.helper.ColumnMatcher.column;
import static ru.iopump.jdbi.db.helper.RowMatcher.rowHas;

/**
 * Ассерты для таблиц БД. Для перевода {@link DataTable} используй {@link DbHelper#asMap(DataTable)}
 */
@SuppressWarnings({"unused", "WeakerAccess"})
@Slf4j
public final class TableAsserts {
    private static final String REGEX_PATTERN = "\\{regexp:(.+?)}";

    private TableAsserts() {
        throw new UnsupportedOperationException("Static class");
    }

    /**
     * Проверить все записи. Хотябы в одной должны содержаться все значения из expectedRowEntries вида [колонка:значение].<br/>
     * Все исходные записи приводятся в строку через {@link java.util.Objects#toString()}.<br/>
     * Для ожидаемых данных поддерживается regexp вида '/^.+$/',
     * Для проверки null значения в expectedRowEntries должно быть значение "null"
     *
     * @param tableName          Имя таблицы.
     * @param recordList         Список записей.
     * @param expectedRowEntries Ожидаемые значения вида [колонка:значение], которые будут содержаться в одной из записей.
     */
    public static void assertRows(
            @NonNull String tableName,
            @Nullable List<? extends Entity> recordList,
            @Nullable Map<String, String> expectedRowEntries
    ) {
        if (expectedRowEntries == null || expectedRowEntries.isEmpty()) {
            return;
        }
        val message = format(
                "В таблице '%s' ожидается запись '%s'",
                tableName,
                "\n" + expectedRowEntries
        );
        assertThat(message, recordList, not(nullValue()));
        assertThat(message, recordList, not(empty()));
        log.debug("[RECORDS] {}", Joiner.on("\n").join(asSetMap(recordList)));
        assertThat(message, asSetMap(recordList), hasItem(containsAllEntries(expectedRowEntries)));
    }

    /**
     * Проверить одну запись. Должны содержаться все значения из expectedRowEntries вида [колонка:значение].<br/>
     * Все исходные данные приводятся в строку через {@link Strings#toString()}.<br/>
     * Для ожидаемых данных поддерживается regexp вида '/^.+$/',
     * Для проверки null значения в expectedRowEntries должно быть значение "null"
     *
     * @param tableName          Имя таблицы.
     * @param record             Запись.
     * @param expectedRowEntries Ожидаемые значения вида [колонка:значение], которые будут содержаться в одной из записей.
     */
    public static void assertRow(
            @NonNull String tableName,
            @Nullable Entity record,
            @Nullable Map<String, String> expectedRowEntries
    ) {
        if (expectedRowEntries == null || expectedRowEntries.isEmpty()) {
            return;
        }
        val message = format(
                "В таблице '%s' ожидается запись '%s'",
                tableName,
                "\n" + Objects.toString(expectedRowEntries, "null")
        );
        assertThat(message, record, not(nullValue()));
        log.debug("[RECORD] {}", record);
        assertThat(message, recordToString(record), containsAllEntries(expectedRowEntries));
    }

    /**
     * Проверить одну запись.
     * Все исходные записи приводятся в строку через {@link Strings#toString()}.<br/>
     * Для ожидаемых записей поддерживается regexp вида '/^.+$/',
     * Для проверки null значения в expectedRowEntries должно быть значение "null"
     *
     * @param tableName Имя таблицы.
     * @param entity    Запись.
     * @param expColumn Имя клолнки.
     * @param expValue  Значение.
     */
    public static void assertRowSimple(@NonNull String tableName,
                                       @NonNull Entity entity,
                                       @NonNull String expColumn,
                                       @Nullable String expValue
    ) {

        String r = entity.getFieldValueAsString(expColumn);
        expValue = expValue == null ? "null" : expValue;
        assertThat(
                format(
                        "Поле %s строки из таблицы %s должно иметь значение",
                        expColumn,
                        tableName
                ),
                r == null ? "null" : r,
                // TODO :: add converters
                expValue.matches(REGEX_PATTERN)
                        ? matchesPattern("(?i)" + expValue)
                        : equalToIgnoringCase(expValue)
        );
    }

    //region Private
    static Set<Map<String, String>> asSetMap(List<? extends Entity> data) {
        return data.stream().map(TableAsserts::recordToString).collect(Collectors.toSet());
    }

    static Matcher<Map<String, String>> containsAllEntries(Map<String, String> expectedMap) {
        return rowHas(
                expectedMap.entrySet().stream()
                        .map(e -> column(e.getKey(), valueIs(e.getValue()))).collect(Collectors.toList())
        );
    }

    static Matcher<String> valueIs(String value) {
        // TODO :: add converters
        final java.util.regex.Matcher matcher = Pattern.compile(REGEX_PATTERN).matcher(value);
        if (matcher.find()) {
            return matchesPattern("(?i)" + matcher.group(1));
        } else {
            return equalToIgnoringCase(value);
        }
    }

    static Map<String, String> recordToString(Entity entity) {
        return entity.allFields().getSourceMap().entrySet().stream()
                .filter(e -> !StringUtils.equalsAnyIgnoreCase(e.getKey(), "envelope"))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> Objects.toString(e.getValue())
                ));
    }
    //endregion
}
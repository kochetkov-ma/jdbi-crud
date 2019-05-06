package ru.iopump.jdbi.db.entity;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import ru.iopump.jdbi.db.exception.DbException;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import org.jdbi.v3.core.mapper.reflect.ColumnName;

/**
 * Объектное представление записи в таблице.
 * Для переопределения Типа колонки используй {@link ColumnName}.
 *
 * @param <ID> Тип .
 */
public interface Entity<ID> {

    /**
     * Вернуть ID.
     * <br/>!Назван без 'get' из-за ошибки в Jpa плагине Jdbi!
     */
    ID id();

    /**
     * Получить значение поля.
     *
     * @param fieldOrColumnName Имя поля или колонки любым регистром.
     * @return Значение поля.
     * @throws DbException Нет колонки или поля.
     */
    Object getFieldValue(@NonNull String fieldOrColumnName);

    void setField(@NonNull String fieldOrColumnNam, String value);

    /**
     * Получить значение поля в виде строки.
     *
     * @param fieldOrColumnName Имя поля или колонки любым регистром.
     * @return Значение поля виде строки.
     * @throws DbException Нет колонки или поля.
     */
    String getFieldValueAsString(@NonNull String fieldOrColumnName);

    /**
     * Проверить наличие поля.
     *
     * @param fieldOrColumnName Имя поля или колонки любым регистром.
     * @return Значение поля виде строки.
     */
    boolean hasField(@NonNull String fieldOrColumnName);

    /**
     * Получить полный набор для сущности: [имя колонки : начение].
     * <br/>!Назван без 'get' из-за ошибки в Jpa плагине Jdbi!
     */
    KeyValueMap allFields();

    /**
     * Immutable map wrapper with extra methods : {@link #getValuesAsList()} and {@link #getKeysAsList()}.
     */
    @EqualsAndHashCode(of = "sourceMap")
    @ToString(of = "sourceMap")
    final class KeyValueMap {
        @Getter
        private final Map<String, Object> sourceMap;
        private final List<String> keys;
        private final List<Object> values;

        KeyValueMap(Map<String, Object> sourceMap) {
            this(sourceMap, true);
        }

        KeyValueMap(Map<String, Object> sourceMap, boolean nullable) {
            final Map<String, Object> sMap = sourceMap.entrySet().stream()
                    .filter((e) -> nullable || e.getValue() != null)
                    .collect(HashMap::new,
                            (m, v) -> m.put(v.getKey(), v.getValue()),
                            HashMap::putAll);
            this.sourceMap = Collections.unmodifiableMap(sMap);
            final List<String> keysTmp = Lists.newArrayList();
            final List<Object> valuesTmp = Lists.newArrayList();
            this.sourceMap.forEach((k, v) -> {
                keysTmp.add(k);
                valuesTmp.add(v);
            });
            keys = Collections.unmodifiableList(keysTmp);
            values = Collections.unmodifiableList(valuesTmp);
        }

        /**
         * Get list of keys with ordering bonding to values list in {@link #getValuesAsList()} and same size.
         *
         * @return New list of keys.
         */
        public List<String> getKeysAsList() {
            return keys;
        }

        /**
         * Get list of values with ordering bonding to values list in {@link #getKeysAsList()} and same size.
         *
         * @return New list of values.
         */
        public List<Object> getValuesAsList() {
            return values;
        }

        public KeyValueMap minus(@Nullable KeyValueMap otherKeyValueMap, boolean nullable) {
            if (otherKeyValueMap == null) {
                return new KeyValueMap(getSourceMap(), nullable);
            }
            final Map<String, Object> resultMap = new HashMap<>(getSourceMap());
            resultMap.entrySet().removeAll(otherKeyValueMap.getSourceMap().entrySet());
            return new KeyValueMap(resultMap, nullable);
        }
    }
}

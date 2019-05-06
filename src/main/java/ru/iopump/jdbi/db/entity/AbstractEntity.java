package ru.iopump.jdbi.db.entity;

import static java.lang.String.format;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.persistence.Column;

import com.google.common.collect.Iterables;
import ru.iopump.jdbi.db.exception.DbException;
import lombok.NonNull;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;

@SuppressWarnings("WeakerAccess")
public abstract class AbstractEntity<ID> implements Entity<ID> {

    @Override
    public void setField(String fieldOrColumnName, String value) {
        final Field field = getField(fieldOrColumnName);
        final Object toWrite;
        // TODO::add converters
        try {
            if (field.getType() == LocalDateTime.class) {
                try {
                    toWrite = LocalDateTime.parse(value);
                } catch (Exception ex) {
                    throw new IllegalArgumentException("Неверный формат даты. " +
                            "Используйте формат из класса DateUtil или по аналогии 2000-10-10T10:10:10", ex);
                }
            } else {
                toWrite = "null".equals(value) ? null : value;
            }
            FieldUtils.writeField(getField(fieldOrColumnName),
                    this,
                    toWrite,
                    true);
        } catch (Exception e) {
            throw new DbException(format("Ошибка записи в поле '%s' значения '%s'",
                    field, value), e);
        }
    }

    @Override
    public Object getFieldValue(String fieldOrColumnName) {
        return getValue(getField(fieldOrColumnName));
    }

    @Override
    public String getFieldValueAsString(String fieldOrColumnName) {
        Object fieldValue = getFieldValue(fieldOrColumnName);
        return fieldValue == null ? null : fieldValue.toString();
    }

    @Override
    public boolean hasField(@NonNull String fieldOrColumnName) {
        return !get(fieldOrColumnName).isEmpty();
    }

    @Override
    public KeyValueMap allFields() {
        return new KeyValueMap(
                FieldUtils.getFieldsListWithAnnotation(getClass(), Column.class)
                        .stream()
                        .collect(HashMap::new,
                                (m, v) -> {
                                    // сначала идут декларированные поля и у них больший приоритет
                                    if (!m.containsKey(getName(v))) {
                                        m.put(getName(v), getValue(v));
                                    }
                                },
                                HashMap::putAll)
        );
    }

    //region Private
    private Set<Field> get(@NonNull String fieldOrColumnName) {
        val res = FieldUtils.getFieldsListWithAnnotation(getClass(), Column.class)
                .parallelStream()
                .filter(field -> {
                    final String columnName = field.getAnnotation(Column.class).name();
                    final String fieldName = field.getName();
                    return StringUtils.equalsAnyIgnoreCase(fieldOrColumnName, columnName, fieldName);
                })
                .collect(Collectors.toSet());
        val declaredRes = res.stream().filter(f -> f.getDeclaringClass() == getClass()).collect(Collectors.toSet());
        if (declaredRes.isEmpty()) {
            return res;
        } else {
            return declaredRes;
        }
    }

    @Nullable
    private Object getValue(@NonNull Field field) {
        field.setAccessible(true);
        try {
            return field.get(this);
        } catch (IllegalAccessException e) {
            throw new DbException(e);
        }
    }

    @NonNull
    private String getName(@NonNull Field field) {
        final Column column = field.getAnnotation(Column.class);
        if (column != null && !StringUtils.isBlank(column.name())) {
            return column.name();
        }
        return field.getName();
    }

    @NonNull
    public Field getField(String fieldOrColumnName) {
        val values = get(fieldOrColumnName);
        if (values.isEmpty()) {
            throw new DbException(format("В классе '%s' не существует поля-колонки '%s'", getClass().getSimpleName(), fieldOrColumnName));
        }
        if (values.size() > 1) {
            throw new DbException(format("В классе '%s' найдено более одного поля-колонки '%s'. Уточните поиск",
                    getClass().getSimpleName(),
                    fieldOrColumnName));
        }
        return Iterables.getLast(values);
    }
    //endregion
}
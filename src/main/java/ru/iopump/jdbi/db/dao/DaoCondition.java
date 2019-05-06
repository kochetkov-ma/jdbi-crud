package ru.iopump.jdbi.db.dao;

import java.util.Objects;
import java.util.StringJoiner;
import javax.annotation.Nullable;

import lombok.NonNull;
import lombok.Value;

@SuppressWarnings({"WeakerAccess", "unused"})
@Value
public class DaoCondition implements Condition {
    @NonNull
    private final String columnName;
    @NonNull
    private final String operand;

    private final Object value;

    @Override
    public String asString() {
        return new StringJoiner(" ")
                .add(columnName)
                .add(operand)
                .add(getValueAsString())
                .toString();
    }

    @Override
    public String toString() {
        return asString();
    }

    private String getValueAsString() {
        // TODO:: add converters
        if (value instanceof Number) {
            return Objects.toString(value);
        }
        return "'" + Objects.toString(value, "null") + "'";
    }

    public static Condition like(@NonNull String columnName, @NonNull String value) {
        return new DaoCondition(columnName, "like", "%" + value + "%");
    }

    public static Condition more(@NonNull String columnName, @NonNull Object value) {
        return new DaoCondition(columnName, ">", value);
    }

    public static Condition less(@NonNull String columnName, @NonNull Object value) {
        return new DaoCondition(columnName, "<", value);
    }

    public static Condition equal(@NonNull String columnName, @Nullable Object value) {
        if (value == null || (value instanceof String && "null".equalsIgnoreCase((String) value))) {
            return isNull(columnName);
        }
        return new DaoCondition(columnName, "=", value);
    }

    public static Condition isNull(@NonNull String columnName) {
        return new DaoCondition(columnName, "is", null);
    }
}
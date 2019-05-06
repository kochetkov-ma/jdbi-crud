package ru.iopump.jdbi.db.dao;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import ru.iopump.jdbi.db.entity.Entity;
import lombok.NonNull;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;

@Value
public class DaoSubQueryColumnCondition<ENTITY extends Entity> implements Condition {
    @NonNull
    private final String targetColumnName;
    @NonNull
    private final String subQueryColumnName;
    @NonNull
    private final Supplier<List<ENTITY>> crudDaoCallback;


    @Override
    public String asString() {
        final String subQuery = calculateSubQuery();
        if (StringUtils.isBlank(subQuery)) {
            return null;
        } else {
            return targetColumnName + " in " + "(" + subQuery + ")";
        }
    }

    private String calculateSubQuery() {
        return crudDaoCallback.get().stream()
                .map(record -> record.getFieldValueAsString(subQueryColumnName))
                .collect(Collectors.joining(","));
    }

    @Override
    public String toString() {
        return targetColumnName + " in " + "(" + subQueryColumnName + " from other table" + ")";
    }
}
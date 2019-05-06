package ru.iopump.jdbi.db.dao;

import static java.lang.String.format;

import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;

import ru.iopump.jdbi.db.exception.DbException;
import ru.iopump.jdbi.db.exception.NoSubQueryResult;
import ru.iopump.jdbi.db.entity.Entity;
import lombok.NonNull;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;

/**
 * <br/>Доступ к CRUD операциям.
 * <br/>Методы, начинающиеся на internal не переопределять.
 * <br/>Если нужно добавить / поменять реализацию метода, то использовать default методы или аннотации JDBI.
 * <br/>Важно сохранить тип Интерфейс, т.к. JDBI работает только с Интерфейсами, т.к. использует {@link Proxy} из JDK,
 * а не CGLIB как, например, Hibernate...
 * <br/>При написании конкретных DAO, обязательно указывать в {@link RegisterBeanMapper} конкретный класс сущности!
 */
@SuppressWarnings({"deprecation", "unused", "DeprecatedIsStillUsed"})
public interface CrudDao<ENTITY extends Entity<ID>, ID> extends InternalCrudDao<ENTITY, ID> {

    default List<ENTITY> findListByColumnValue(@NonNull String column, @Nullable Object value) {
        String operand = "=";
        if (value == null) {
            operand = "is";
        }
        return internalFind(getTableName(), column, value, operand, getIdColumnName());
    }

    default List<ENTITY> findListByColumnValue(@NonNull DaoConditionChain conditionChain) {
        try {
            final String cString = conditionChain.asString();
            if (StringUtils.isBlank(cString)) {
                return internalFindAll(getTableName(), getIdColumnName(), OrderType.ASC, 100);
            } else {
                return internalFindConditions(getTableName(), cString, getIdColumnName());
            }
        } catch (NoSubQueryResult expected) {
            return Collections.emptyList();
        }
    }

    default Optional<ENTITY> findOneByColumnValue(@NonNull String column, @Nullable Object value) {
        return findListByColumnValue(column, value)
                .stream()
                .findFirst();
    }

    default Optional<ENTITY> findOneByColumnValue(@NonNull DaoConditionChain conditionChain) {
        return findListByColumnValue(conditionChain)
                .stream()
                .findFirst();
    }

    default List<ENTITY> findLast(int limit) {
        return internalFindAll(getTableName(), getIdColumnName(), OrderType.DESC, limit);
    }

    default List<ENTITY> findFirst(int limit) {
        return internalFindAll(getTableName(), getIdColumnName(), OrderType.ASC, limit);
    }

    default Optional<ENTITY> findOne(@NonNull ID id) {
        return findOneByColumnValue(getIdColumnName(), id);
    }

    default int updateByColumnValue(@NonNull String column,
                                    @Nullable Object value,
                                    @NonNull DaoConditionChain conditionChain) {

        try {
            final String cString = conditionChain.asString();
            if (StringUtils.isBlank(cString)) {
                return internalUpdateAll(getTableName(), column, value);
            } else {
                return internalUpdateConditions(getTableName(), column, value, cString);
            }
        } catch (NoSubQueryResult expected) {
            return 0;
        }
    }

    default void insert(@NonNull ENTITY entity) {
        val all = entity.allFields();
        final ID id = entity.id();
        internalInsert(getTableName(), all.getKeysAsList(), all.getValuesAsList());
    }

    default ID update(@NonNull ENTITY entity) {
        final ID id = entity.id();
        if (id == null) {
            throw new DbException(format("[%s] Не указан id записи для обновления", entity.getClass().getSimpleName()));
        }
        final Optional<ENTITY> existsEntity = findOne(id);
        if (!existsEntity.isPresent()) {
            throw new DbException(format("[%s] Запись с id = '%s' не существует. Создайте ее или воспользуйтесь методом updateOrInsert",
                    entity.getClass().getSimpleName(),
                    id)
            );
        }
        final Entity.KeyValueMap toUpdate = entity.allFields().minus(existsEntity.get().allFields(), true);
        toUpdate.getSourceMap().forEach((k, v) -> internalUpdate(getTableName(), k, v, getIdColumnName(), id));
        return id;
    }

    default int delete(@NonNull ID id) {
        return internalDelete(getTableName(), getIdColumnName(), id);
    }

    default void truncate() {
        internalTruncate(getTableName());
    }

    default long count() {
        return internalCount(getTableName());
    }

    /**
     * Получить имя таблицы.
     * Необходимо реализовать для конкретной таблицы.
     *
     * @return Имя таблицы.
     */
    @NonNull
    String getTableName();

    /**
     * Получить имя колонки для ID.
     * Необходимо реализовать для конкретной таблицы.
     *
     * @return Имя колонки для ID.
     */
    @NonNull
    String getIdColumnName();
}
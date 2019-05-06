package ru.iopump.jdbi.db.example;

import ru.iopump.jdbi.db.dao.Dao;
import ru.iopump.jdbi.db.dao.CrudDao;

/**
 * Базовый интерфейс CRUD для расширения в проекте.
 * Создан как пример реализации наследования представлений.
 * Для собственной реализации используй {@link CrudDao}
 */
@Dao(include = false)
public interface BaseExampleDao<T extends ExampleEntity> extends CrudDao<T, Integer> {
    @Override
    default String getTableName() {
        return "table";
    }

    @Override
    default String getIdColumnName() {
        return "id";
    }
}
package ru.iopump.jdbi.db.example;

import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;

/**
 * Реализация по умолчанию. Не для наследования! Для расширения используй: {@link BaseExampleDao}.
 * Создан как пример реализации схемы наследования представлений.
 */
@RegisterBeanMapper(value = ExampleEntity.class)
public interface ExampleDao extends BaseExampleDao<ExampleEntity> {
}
package ru.iopump.jdbi.db.dao;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import ru.iopump.jdbi.db.entity.Entity;

/**
 * Аннотируется пользовательский DAO класс который наследуется от общего DAO класса из common.
 * Неорбходимо указать пользовательский класс {@link Entity} в {@link #entityClass()}.
 * Если изменяется имя таблицы, то нужно отразить это в {@link #tableName()}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Dao {

    Class<? extends Entity> entityClass() default Entity.class;

    String tableName() default "";

    boolean include() default true;
}

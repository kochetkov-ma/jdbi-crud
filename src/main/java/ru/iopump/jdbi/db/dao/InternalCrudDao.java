package ru.iopump.jdbi.db.dao;

import java.lang.reflect.Proxy;
import java.util.List;
import javax.annotation.Nullable;

import ru.iopump.jdbi.db.entity.Entity;
import lombok.NonNull;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindList;
import org.jdbi.v3.sqlobject.customizer.Define;
import org.jdbi.v3.sqlobject.customizer.DefineList;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

/**
 * <br/>Доступ к CRUD операциям.
 * <br/>Методы, начинающиеся на internal не переопределять.
 * <br/>Если нужно добавить / поменять реализацию метода, то использовать default методы или аннотации JDBI.
 * <br/>Важно сохранить тип Интерфейс, т.к. JDBI работает только с Интерфейсами, т.к. использует {@link Proxy} из JDK,
 * а не CGLIB как, например, Hibernate...
 * <br/>При написании конкретных DAO, обязательно указывать в {@link RegisterBeanMapper} конкретный класс сущности!
 */
@SuppressWarnings({"unused"})
interface InternalCrudDao<ENTITY extends Entity<ID>, ID> {
    //region CRUD

    /**
     * Inner implementation. Not for Override!!!
     */
    @SqlQuery("select count(*) from <tableName>")
    long internalCount(@Define("tableName") @NonNull String tableName);

    /**
     * Inner implementation. Not for Override!!!
     */
    @SqlQuery("select * from <tableName> order by <idColumnName> <orderType> limit <limit>")
    List<ENTITY> internalFindAll(@Define("tableName") @NonNull String tableName,
                                 @Define("idColumnName") @NonNull String idColumnName,
                                 @Define("orderType") @NonNull OrderType orderType,
                                 @Define("limit") int limit
    );

    /**
     * Inner implementation. Not for Override!!!
     */
    @SqlQuery("select * from <tableName> where <conditions> order by <idColumnName> desc")
    List<ENTITY> internalFindConditions(@Define("tableName") @NonNull String tableName,
                                        @Define("conditions") @NonNull String conditions,
                                        @Define("idColumnName") @NonNull String idColumnName);

    /**
     * Inner implementation. Not for Override!!!
     */
    @SqlUpdate("update <tableName> set <columnName> = :value where <conditions>")
    int internalUpdateConditions(@Define("tableName") @NonNull String tableName,
                                 @Define("columnName") @NonNull String columnName,
                                 @Bind("value") @NonNull Object value,
                                 @Define("conditions") @NonNull String conditions);

    /**
     * Inner implementation. Not for Override!!!
     */
    @SqlQuery("select * from <tableName> where <column> <operand> :value order by <idColumnName> desc")
    List<ENTITY> internalFind(@Define("tableName") @NonNull String tableName,
                              @Define("column") @NonNull String column,
                              @Bind("value") @Nullable Object value,
                              @Define("operand") @NonNull String operand,
                              @Define("idColumnName") @NonNull String idColumnName);

    /**
     * Inner implementation. Not for Override!!!
     */
    @SqlUpdate("truncate table <tableName>")
    void internalTruncate(@Define("tableName") @NonNull String tableName);

    /**
     * Inner implementation. Not for Override!!!
     */
    @SqlUpdate("delete from <tableName> where <idColumnName> = :id")
    int internalDelete(@Define("tableName") @NonNull String tableName,
                       @Define("idColumnName") @NonNull String idColumnName,
                       @Bind("id") @NonNull ID id);

    /**
     * Inner implementation. Not for Override!!!
     */
    @SqlUpdate("insert into <tableName> (<columnList>) values (<valueList>)")
    void internalInsert(@Define("tableName") @NonNull String tableName,
                        @DefineList("columnList") @NonNull List<String> columnList,
                        @BindList("valueList") @NonNull List<Object> valueList);

    /**
     * Inner implementation. Not for Override!!!
     */
    @SqlUpdate("update <tableName> set <columnName> = :value where <idColumnName> = :id")
    void internalUpdate(@Define("tableName") @NonNull String tableName,
                        @Define("columnName") @NonNull String columnName,
                        @Bind("value") @Nullable Object value,
                        @Define("idColumnName") @NonNull String idColumnName,
                        @Bind("id") @NonNull ID id);

    /**
     * Inner implementation. Not for Override!!!
     */
    @SqlUpdate("update <tableName> set <columnName> = :value")
    int internalUpdateAll(@Define("tableName") @NonNull String tableName,
                          @Define("columnName") @NonNull String columnName,
                          @Bind("value") @Nullable Object value);
    //endregion

    enum OrderType {
        ASC, DESC
    }
}
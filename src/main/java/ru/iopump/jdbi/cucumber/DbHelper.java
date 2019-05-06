package ru.iopump.jdbi.cucumber;

import static java.lang.String.format;

import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import com.google.common.collect.Maps;
import io.cucumber.datatable.DataTable;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import ru.iopump.jdbi.db.dao.CrudDao;
import ru.iopump.jdbi.db.dao.Dao;
import ru.iopump.jdbi.db.dao.DaoCondition;
import ru.iopump.jdbi.db.dao.DaoConditionChain;
import ru.iopump.jdbi.db.entity.Entity;
import ru.iopump.jdbi.db.exception.DbException;
import ru.iopump.jdbi.util.ReflectionUtils;


@SuppressWarnings("unused")
public class DbHelper {

    private final String[] daoPackages;

    private final Object DAO_CLASSES_LOCK = new Object();
    private Map<String, Class<? extends CrudDao>> DAO_CLASSES_CACHE;

    public DbHelper(String... daoPackages) {
        this.daoPackages = daoPackages;
    }

    /**
     * Превратить cucumber Пример вида 'имя колонки|значение' в карту вида [имя колонки : значение].
     *
     * @param dataTable Пример cucumber.
     */
    public static Map<String, String> asMap(@Nullable DataTable dataTable) {
        if (dataTable == null) {
            return Maps.newHashMap();
        }
        return dataTable.asMap(String.class, String.class);
    }


    /**
     * Загрузить {@link CrudDao} по имени таблицы.
     * Имя таблицы проверяется в первую очередь, если имеется аннотация {@link Dao}, если аннотации нет (или значение пустое),
     * то вызывается метод {@link CrudDao#getTableName()}.
     */
    @NonNull
    public <DAO extends CrudDao> DAO loadDaoByTableName(@NonNull Jdbi jdbi,
                                                        @Nullable String table) {

        synchronized (DAO_CLASSES_LOCK) {
            if (DAO_CLASSES_CACHE == null) {
                DAO_CLASSES_CACHE = ReflectionUtils.getAllClasses(CrudDao.class, daoPackages)
                        .stream()
                        .filter(i -> i != CrudDao.class)
                        .map(this::getTableClass)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toMap(Pair::getKey,
                                Pair::getValue,
                                (one, two) -> {
                                    // если есть Dao аннотация, то использовать именно этот класс в первую очередь.
                                    if (two.isAnnotationPresent(Dao.class)) {
                                        return two;
                                    } else {
                                        return one;
                                    }
                                }));
            }
        }
        final Class<? extends CrudDao> cls = DAO_CLASSES_CACHE.get(StringUtils.upperCase(table));
        if (cls == null) {
            throw new IllegalArgumentException(format("Не найдено CrudDao для таблицы %s в пакете %s",
                    table,
                    Arrays.toString(daoPackages))
            );
        }
        //noinspection unchecked
        return (DAO) jdbi.onDemand(cls);
    }

    /**
     * Получить класс сущености, чтобы потом создать его через отражение.
     */
    public <ENTITY extends Entity> Class<ENTITY> getEntityClass(@NonNull final CrudDao crudDao) {
        Class clz;
        if (Proxy.isProxyClass(crudDao.getClass())) {
            clz = crudDao.getClass().getInterfaces()[0];
        } else {
            clz = crudDao.getClass();
        }
        if (clz.isAnnotationPresent(Dao.class)) {
            Class res = ((Dao) clz.getAnnotation(Dao.class)).entityClass();
            if (res != Entity.class) {
                //noinspection unchecked
                return (Class<ENTITY>) res;
            }
        }
        if (clz.isAnnotationPresent(RegisterBeanMapper.class)) {
            //noinspection unchecked
            return (Class<ENTITY>) ((RegisterBeanMapper) clz.getAnnotation(RegisterBeanMapper.class)).value();
        }
        throw new DbException(format("Для DAO %s не удается получить класс ENTITY. " +
                "Проверьте наличие аннотаций : Dao или RegisterBeanMapper", clz));
    }

    @NonNull
    public DaoConditionChain dataTableToConditionals(@NonNull DataTable dataTable) {
        return dataTable.asLists(Object.class)
                .stream()
                .map(row -> {
                    if (row.size() == 2) {
                        return DaoCondition.equal((String) row.get(0), row.get(1));
                    } else if (row.size() == 3) {
                        return new DaoCondition((String) row.get(0), (String) row.get(1), row.get(2));
                    } else {
                        throw new IllegalArgumentException(format("Ряд %s должн быть рамером 2 или 3",
                                Objects.toString(row)));
                    }
                })
                .reduce(new DaoConditionChain(), DaoConditionChain::and, (one, two) -> two);
    }

    //region Private
    @Nullable
    private Pair<String, Class<? extends CrudDao>> getTableClass(@NonNull Class<? extends CrudDao> crudDaoClass) {
        String tableName = null;
        if (crudDaoClass.isAnnotationPresent(Dao.class)) {
            if (!crudDaoClass.getAnnotation(Dao.class).include()) {
                return null;
            }
            if (!StringUtils.isBlank(crudDaoClass.getAnnotation(Dao.class).tableName())) {
                tableName = crudDaoClass.getAnnotation(Dao.class).tableName();
            }
        }
        if (tableName == null) {
            try {
                final CrudDao proxy = (CrudDao) Proxy.newProxyInstance(
                        Thread.currentThread().getContextClassLoader(),
                        new Class[]{crudDaoClass},
                        (p, m, a) -> {
                            final Method method = crudDaoClass.getMethod("getTableName");
                            Constructor<Lookup> constructor = Lookup.class
                                    .getDeclaredConstructor(Class.class);
                            constructor.setAccessible(true);
                            return constructor.newInstance(crudDaoClass)
                                    .in(crudDaoClass)
                                    .unreflectSpecial(method, crudDaoClass)
                                    .bindTo(p)
                                    .invokeWithArguments();
                        }
                );
                tableName = proxy.getTableName();
            } catch (Throwable e) {
                throw new DbException(format("В классе %s не найден метод %s", crudDaoClass, "getTableName"), e);
            }
        }
        return Pair.of(StringUtils.upperCase(tableName), crudDaoClass);
    }
    //endregion
}
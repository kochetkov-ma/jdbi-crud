package ru.iopump.jdbi.db.helper;

import static java.lang.String.format;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;

import com.google.common.base.Joiner;
import ru.iopump.jdbi.db.dao.CrudDao;
import ru.iopump.jdbi.db.dao.DaoConditionChain;
import ru.iopump.jdbi.db.entity.Entity;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.awaitility.Duration;
import org.awaitility.core.ConditionTimeoutException;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.number.OrderingComparison.greaterThan;

@SuppressWarnings({"WeakerAccess", "unused"})
@Slf4j
public class TableWaiter {
    private final int timeoutSec;
    private final int pollIntervalMs;

    //region Constructors
    public TableWaiter() {
        this(30, 2000);
    }

    public TableWaiter(int timeoutSec) {
        this(timeoutSec, 2000);
    }

    public TableWaiter(int timeoutSec,
                       int pollIntervalMs) {
        this.timeoutSec = timeoutSec;
        // если таймаут больше чем задержка между циклами, то ок, если нет, то задержка выбирается = таймауту
        this.pollIntervalMs = timeoutSec * 1000 > pollIntervalMs ? pollIntervalMs : timeoutSec * 1000 - 1;
    }
    //endregion

    //region Wait Standard

    /**
     * Ожидание появления хотя-бы одной записи или определенного числа записей, если count > 0.
     *
     * @param table       Имя таблицы.
     * @param fetchEntity Пользовательская реализация выборки записей из таблицы.
     * @param <ENTITY>    Тип записей.
     * @param count       Ожидаемое кол-во. Если оно <= 0, то ожидается любое кол-во большее 0.
     * @return Список найденных записей.
     * @throws ConditionTimeoutException Если не появилось ни одной записи.
     */
    public <ENTITY extends Entity> List<ENTITY> waitRows(@NonNull String table,
                                                         @NonNull Callable<List<ENTITY>> fetchEntity,
                                                         int count,
                                                         @Nullable String message) {

        final String msg;
        if (!StringUtils.isBlank(message)) {
            msg = message;
        } else {
            msg = format("Ожидание хотя бы одной записи в %s в течение %d сек.", table, timeoutSec);
        }
        final List<ENTITY> logRows = await(msg)
                .pollInSameThread()
                .pollInterval(pollIntervalMs, TimeUnit.MILLISECONDS)
                .pollDelay(Duration.ZERO)
                .timeout(timeoutSec, TimeUnit.SECONDS)
                .until(fetchEntity, count > 0 ? hasSize(count) : hasSize(greaterThan(0)));
        log.debug("{}", Joiner.on("\n").join(logRows));
        return logRows;
    }

    /**
     * Ожидание появления хотя-бы одной записи или определенного числа записей, если count > 0.
     *
     * @param dao        DAO для выборки.
     * @param conditions Условия выборки.
     * @param <ENTITY>   Тип записи.
     * @param <ID>       Тип ID записи
     * @return Список найденных записей.
     * @throws ConditionTimeoutException Если не появилось ни одной записи.
     */
    public <ENTITY extends Entity<ID>, ID> List<ENTITY> waitRows(@NonNull CrudDao<ENTITY, ID> dao,
                                                                 @NonNull DaoConditionChain conditions,
                                                                 int count) {

        return waitRows(dao.getTableName(), () -> dao.findListByColumnValue(conditions), count,
                format("Ожидание хотя бы одной записи в %s в течение %d сек. по условиям '%s'",
                        dao.getTableName(),
                        timeoutSec,
                        conditions.toString()
                )
        );
    }
    //endregion

    //region Wait With Assertion

    /**
     * Проверка появления указанного кол-ва записей.
     *
     * @param table         Имя таблицы.
     * @param fetchEntity   Пользовательская реализация выборки записей из таблицы.
     * @param expectedCount Ожидаемое кол-во записей.
     * @param <ENTITY>      Тип записи.
     * @param <ID>          Тип ID записи
     * @throws AssertionError Если не появилось нужного кол-ва записей.
     */
    public <ENTITY extends Entity<ID>, ID> void waitRowsAsserted(@NonNull String table,
                                                                 @NonNull Callable<List<ENTITY>> fetchEntity,
                                                                 int expectedCount,
                                                                 @Nullable String message) {

        final String msg;
        if (!StringUtils.isBlank(message)) {
            msg = message;
        } else {
            msg = format("В таблице %s не найдено записей в кол-ве %d в течении %d сек.",
                    table,
                    expectedCount,
                    timeoutSec);
        }
        try {
            waitRows(table, fetchEntity, expectedCount, null);
        } catch (ConditionTimeoutException ex) {
            throw new AssertionError(msg, ex);
        }
    }

    /**
     * Проверка появления указанного кол-ва записей.
     *
     * @param dao        DAO для выборки.
     * @param conditions Условия выборки.
     * @param <ENTITY>   Тип записи.д
     * @param <ID>       Тип ID записи
     * @throws AssertionError Если не появилось нужного кол-ва записей.
     */
    public <ENTITY extends Entity<ID>, ID> void waitRowsAsserted(@NonNull CrudDao<ENTITY, ID> dao,
                                                                 @NonNull DaoConditionChain conditions,
                                                                 int expectedCount) {
        waitRowsAsserted(dao.getTableName(),
                () -> dao.findListByColumnValue(conditions),
                expectedCount,
                format("В таблице %s не найдено записей в кол-ве %d в течении %d сек. по условиям '%s'",
                        dao.getTableName(),
                        expectedCount,
                        timeoutSec,
                        conditions.toString())
        );
    }
    //endregion
}
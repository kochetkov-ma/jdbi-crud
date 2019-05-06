package ru.iopump.jdbi.db.dao;

import static java.lang.String.format;

import java.time.LocalDateTime;

import lombok.val;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.UnableToCreateStatementException;
import org.jdbi.v3.jpa.JpaPlugin;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.junit.Rule;
import org.junit.Test;
import org.zapodot.junit.db.CompatibilityMode;
import org.zapodot.junit.db.EmbeddedDatabaseRule;
import ru.iopump.jdbi.db.exception.DbException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static ru.iopump.jdbi.db.dao.DaoCondition.less;
import static ru.iopump.jdbi.db.dao.DaoCondition.like;
import static ru.iopump.jdbi.db.dao.DaoCondition.more;

public class AbstractCrudDaoTest {

    @Rule
    public final EmbeddedDatabaseRule dbRule = EmbeddedDatabaseRule.h2()
            .withMode(CompatibilityMode.MySQL)
            .withInitialSqlFromResource("classpath:db.sql")
            .build();

    @Test
    public void testFindOne() {
        assertThat(dbRule.getConnection()).isNotNull();
        final OnlineLogDao dao = getDao();
        assertThat(dao.findOne(1))
                .isPresent()
                .get()
                .hasFieldOrPropertyWithValue("recordId", 1)
                .hasFieldOrPropertyWithValue("envId", "0000000001")
                .hasFieldOrPropertyWithValue("envTimeIn", LocalDateTime.of(2000, 1, 1, 0, 0, 0))
                .hasFieldOrPropertyWithValue("safPlanId", "PLAN_1")
                .hasFieldOrPropertyWithValue("txnSource", "TNX_TEST_1");

        assertThat(dao.findOne(2))
                .isPresent()
                .get()
                .hasFieldOrPropertyWithValue("recordId", 2)
                .hasFieldOrPropertyWithValue("envId", "0000000002")
                .hasFieldOrPropertyWithValue("envTimeIn", LocalDateTime.of(2000, 1, 1, 10, 0, 0))
                .hasFieldOrPropertyWithValue("safPlanId", "PLAN_2")
                .hasFieldOrPropertyWithValue("txnSource", null);

        assertThat(dao.findOne(3))
                .isPresent()
                .get()
                .hasFieldOrPropertyWithValue("recordId", 3)
                .hasFieldOrPropertyWithValue("envId", "0000000003")
                .hasFieldOrPropertyWithValue("envTimeIn", null)
                .hasFieldOrPropertyWithValue("safPlanId", "PLAN_3")
                .hasFieldOrPropertyWithValue("txnSource", "TNX_TEST_3");
    }

    @Test
    public void testFindListByColumnValue() {
        assertThat(dbRule.getConnection()).isNotNull();
        final OnlineLogDao dao = getDao();

        assertThat(dao.findListByColumnValue("env_id", "0000000003"))
                .hasSize(2);
        assertThat(dao.findListByColumnValue("ENV_ID", "0000000003"))
                .hasSize(2);

        assertThatThrownBy(() -> dao.findListByColumnValue("ENVID", "0000000003"))
                .isInstanceOf(UnableToCreateStatementException.class);

        assertThat(dao.findListByColumnValue("txn_source", null))
                .hasSize(2);
    }


    @Test
    public void testFindListByCondition() {
        assertThat(dbRule.getConnection()).isNotNull();
        final OnlineLogDao dao = getDao();
        val date = LocalDateTime.of(2000, 1, 1, 0, 0, 1);


        DaoConditionChain chain = new DaoConditionChain(more("record_id", 0))
                .and(like("env_id", "0000000003"))
                .and(less("env_timein", date));

        assertThat(dao.findListByColumnValue(chain))
                .hasSize(1);
    }

    @Test
    public void testFindListBySubQueryCondition() {
        assertThat(dbRule.getConnection()).isNotNull();
        final OnlineLogDao dao = getDao();
        val date = LocalDateTime.of(2000, 1, 1, 0, 0, 1);


        DaoConditionChain chainSub = new DaoConditionChain(more("record_id", 0))
                .and(like("env_id", "0000000003"))
                .and(less("env_timein", date));

        DaoConditionChain chainMain = new DaoConditionChain(
                new DaoSubQueryColumnCondition<>(
                        "record_id",
                        "record_id",
                        () -> dao.findListByColumnValue(chainSub))
        );

        assertThat(dao.findListByColumnValue(chainMain))
                .hasSize(1);
    }

    @Test
    public void testFindLast() {
        assertThat(dbRule.getConnection()).isNotNull();
        final OnlineLogDao dao = getDao();
        assertThat(dao.findLast(2))
                .hasSize(2)
                .containsExactly(
                        new OnlineLogEntity(4,
                                "0000000003",
                                LocalDateTime.of(2000, 1, 1, 0, 0, 0),
                                "PLAN_1",
                                null),
                        new OnlineLogEntity(3,
                                "0000000003",
                                null,
                                "PLAN_3",
                                "TNX_TEST_3")
                );
    }

    @Test
    public void testFindFirst() {
        assertThat(dbRule.getConnection()).isNotNull();
        final OnlineLogDao dao = getDao();
        assertThat(dao.findFirst(2))
                .hasSize(2)
                .containsExactly(
                        new OnlineLogEntity(1,
                                "0000000001",
                                LocalDateTime.of(2000, 1, 1, 0, 0, 0),
                                "PLAN_1",
                                "TNX_TEST_1"),
                        new OnlineLogEntity(2,
                                "0000000002",
                                LocalDateTime.of(2000, 1, 1, 10, 0, 0),
                                "PLAN_2",
                                null)
                );
    }

    @Test
    public void testInsert() {
        assertThat(dbRule.getConnection()).isNotNull();
        final OnlineLogDao dao = getDao();
        val date = LocalDateTime.now();
        dao.insert(new OnlineLogEntity(null, null, date, "TEST", "TEST"));

        assertThat(dao.findOne(5))
                .isPresent()
                .get()
                .hasFieldOrPropertyWithValue("recordId", 5)
                .hasFieldOrPropertyWithValue("envId", null)
                .hasFieldOrPropertyWithValue("envTimeIn", date)
                .hasFieldOrPropertyWithValue("safPlanId", "TEST")
                .hasFieldOrPropertyWithValue("txnSource", "TEST");
    }

    @Test
    public void testUpdate() {
        assertThat(dbRule.getConnection()).isNotNull();
        final OnlineLogDao dao = getDao();
        val date = LocalDateTime.of(2000, 1, 1, 0, 0, 0);
        assertThatThrownBy(() -> dao.update(new OnlineLogEntity()))
                .isInstanceOf(DbException.class);
        OnlineLogEntity res = new OnlineLogEntity();
        res.setRecordId(10);
        assertThatThrownBy(() -> dao.update(res))
                .isInstanceOf(DbException.class);


        assertThat(dao.update(new OnlineLogEntity(4,
                        "0000000003",
                        date,
                        null,
                        "TNX_TEST_1")
                )
        ).isEqualTo(4);

        assertThat(dao.findOne(4))
                .isPresent()
                .get()
                .hasFieldOrPropertyWithValue("safPlanId", null)
                .hasFieldOrPropertyWithValue("txnSource", "TNX_TEST_1");
    }

    @Test
    public void testDelete() {
        assertThat(dbRule.getConnection()).isNotNull();
        final OnlineLogDao dao = getDao();
        assertThat(dao.delete(4)).isEqualTo(1);
        assertThat(dao.count()).isEqualTo(3);
    }

    @Test
    public void testTruncate() {
        assertThat(dbRule.getConnection()).isNotNull();
        final OnlineLogDao dao = getDao();
        dao.truncate();
        assertThat(dao.count()).isZero();
    }

    private OnlineLogDao getDao() {
        //noinspection deprecation
        return Jdbi.create(dbRule.getDataSource())
                .installPlugin(new SqlObjectPlugin())
                .installPlugin(new JpaPlugin())
                .setTimingCollector((elapsedNs, ctx) -> System.out.println(format("SLQ fulfilled in %d ms : %s",
                        elapsedNs / 1_000_000, ctx.getStatement().toString())))
                .onDemand(OnlineLogDao.class);
    }
}
package ru.iopump.jdbi.cucumber;


import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.jpa.JpaPlugin;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.junit.Rule;
import org.junit.Test;
import org.zapodot.junit.db.CompatibilityMode;
import org.zapodot.junit.db.EmbeddedDatabaseRule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


public class DbHelperTest {

    @Rule
    public final EmbeddedDatabaseRule dbRule = EmbeddedDatabaseRule.h2()
            .withMode(CompatibilityMode.MySQL)
            .withInitialSqlFromResource("classpath:db.sql")
            .build();

    @Test
    public void testLoadDaoByTableName() {
        //mock creation
        final Jdbi jdbiMock = getDao();

        final DbHelper helper = new DbHelper("ru.iopump.jdbi");

        // assertions
        assertThat(helper.loadDaoByTableName(jdbiMock, "table").getTableName())
                .isEqualTo("table");

        assertThatThrownBy(() -> helper.loadDaoByTableName(jdbiMock, "not_exists").getTableName())
                .isInstanceOf(IllegalArgumentException.class);
    }

    private Jdbi getDao() {
        return Jdbi.create(dbRule.getDataSource())
                .installPlugin(new SqlObjectPlugin())
                .installPlugin(new JpaPlugin());
    }
}
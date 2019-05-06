package ru.iopump.jdbi.db.entity;

import java.util.Map;
import javax.persistence.Column;

import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.junit.Test;
import ru.iopump.jdbi.db.dao.CrudDao;
import ru.iopump.jdbi.db.exception.DbException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class AbstractEntityTest {

    @Test
    public void testGetFieldValue() {
        final AbstractEntity test = new TestAbstractEntity();
        assertThat(test.getFieldValue("cOlUmN2"))
                .isEqualTo(100);
        assertThat(test.getFieldValue("cOlUmNtWo"))
                .isEqualTo(100);
        assertThat(test.getFieldValue("column4"))
                .isNull();
        assertThatThrownBy(() -> test.getFieldValue("column"))
                .isInstanceOf(DbException.class);
        assertThatThrownBy(() -> test.getFieldValue("notColumn"))
                .isInstanceOf(DbException.class);

    }

    @Test
    public void testGetFieldValueAsString() {
        final AbstractEntity test = new TestAbstractEntity();
        assertThat(test.getFieldValueAsString("cOlUmN2"))
                .isEqualTo("100");
        assertThat(test.getFieldValueAsString("cOlUmNtWo"))
                .isEqualTo("100");
        assertThat(test.getFieldValueAsString("column4"))
                .isNull();
        assertThatThrownBy(() -> test.getFieldValue("column"))
                .isInstanceOf(DbException.class);
        assertThatThrownBy(() -> test.getFieldValue("notColumn"))
                .isInstanceOf(DbException.class);
    }

    @Test
    public void testHasField() {
        final AbstractEntity test = new TestAbstractEntity();
        assertThat(test.hasField("cOlUmN2"))
                .isTrue();
        assertThat(test.hasField("cOlUmNtWo"))
                .isTrue();
        assertThat(test.hasField("column4"))
                .isTrue();
        assertThat(test.hasField("column"))
                .isTrue();
        assertThat(test.hasField("notColumn"))
                .isFalse();
        assertThat(test.hasField("notExistsColumn"))
                .isFalse();
    }

    @Test
    public void testGetAll() {
        final Map<String, Object> map = Maps.newHashMap();
        map.put("column1", "defaultColumn");
        map.put("columnTwo", 100);
        map.put("column", null);

        final AbstractEntity test = new TestAbstractEntity();
        assertThat(test.allFields().getSourceMap())
                .containsAllEntriesOf(map);
    }

    @Setter
    @Getter
    private static class TestAbstractEntity extends AbstractEntity<Long> {

        @Column
        private String column1 = "defaultColumn";

        @Column(name = "columnTwo")
        private Integer column2 = 100;

        @Column(name = "column")
        private String column3;

        @Column(name = "column")
        private Integer column4;

        private Integer notColumn;

        @Override
        public Long id() {
            return null;
        }
    }

    private interface TestDao extends CrudDao<TestAbstractEntity, Long> {

        @Override
        @NonNull
        default String getTableName() {
            return "table";
        }

        @Override
        @NonNull
        default String getIdColumnName() {
            return "id";
        }
    }
}
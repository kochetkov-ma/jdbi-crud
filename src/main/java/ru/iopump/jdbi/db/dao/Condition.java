package ru.iopump.jdbi.db.dao;

import javax.annotation.Nullable;

public interface Condition {
    /**
     * Условие в виде части строки sql в синтаксисе (СУБД) или null, если условие следует исключить из цепочки.
     */
    @Nullable
    String asString();
}

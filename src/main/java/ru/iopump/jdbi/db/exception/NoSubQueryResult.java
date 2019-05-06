package ru.iopump.jdbi.db.exception;

/**
 * Checked Exception.
 * Для ситуции, когда подзапрос в условиях поиска вернул 0 записей и к нему применена логическая опрация AND.
 */
public class NoSubQueryResult extends Exception {
    public NoSubQueryResult() {
    }

    public NoSubQueryResult(String message) {
        super(message);
    }

    public NoSubQueryResult(String message, Throwable cause) {
        super(message, cause);
    }

    public NoSubQueryResult(Throwable cause) {
        super(cause);
    }

    public NoSubQueryResult(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

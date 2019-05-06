package ru.iopump.jdbi.db.helper;

import static java.lang.String.format;

import java.util.Map;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
@SuppressWarnings("unused")
public class ColumnMatcher extends TypeSafeMatcher<Map<String, String>> {

    private final String column;
    private final Matcher<String> valueMatcher;

    private ColumnMatcher(String column, Matcher<String> valueMatcher) {
        super();
        this.column = column;
        this.valueMatcher = valueMatcher;
    }

    @Factory
    public static Matcher<Map<String, String>> column(String column,
                                                      Matcher<String> valueMatcher) {
        return new ColumnMatcher(column, valueMatcher);
    }

    @Override
    public void describeTo(Description description) {
        description.appendText(column)
                .appendText(" -> ")
                .appendDescriptionOf(valueMatcher);
    }

    @Override
    public void describeMismatchSafely(Map<String, String> map, Description mismatchDescription) {
        if (!map.containsKey(column)) {
            mismatchDescription.appendText(format("колонка %s не найдена", column));
        } else {
            mismatchDescription
                    .appendText(format("у колонки %s актуальное значение: ", column))
                    .appendValue(map.get(column));
        }
    }

    @Override
    protected boolean matchesSafely(Map<String, String> map) {
        if (map.containsKey(column)) {
            return valueMatcher.matches(map.get(column));
        }
        return false;
    }
}
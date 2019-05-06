package ru.iopump.jdbi.db.helper;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

@SuppressWarnings("unused")
public class RowMatcher extends TypeSafeMatcher<Map<String, String>> {

    private final Collection<Matcher<Map<String, String>>> matchers;

    private RowMatcher(Collection<Matcher<Map<String, String>>> matchers) {
        super();
        this.matchers = matchers;
    }

    @Factory
    public static Matcher<Map<String, String>> rowHas(Collection<Matcher<Map<String, String>>> matchers) {
        return new RowMatcher(matchers);
    }

    @Override
    protected boolean matchesSafely(Map<String, String> map) {
        return failMatchers(map).isEmpty();
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("Ожидается запись:\n");
        matchers.forEach(m -> {
            m.describeTo(description);
            description.appendText("\n");
        });
    }

    @Override
    public void describeMismatchSafely(Map<String, String> map, Description mismatchDescription) {
        final Collection<Matcher<Map<String, String>>> fails = failMatchers(map);
        fails.forEach(m -> {
            mismatchDescription.appendText("\n");
            m.describeMismatch(map, mismatchDescription);
        });
        mismatchDescription.appendText("\n");
        mismatchDescription.appendText("вся запись: ")
                .appendValueList("[", ", ", "]\n", map.entrySet());
    }

    private Collection<Matcher<Map<String, String>>> failMatchers(Map<String, String> map) {
        if (map == null) {
            return Collections.emptyList();
        }
        return matchers.stream()
                .filter(m -> !m.matches(map))
                .collect(Collectors.toList());
    }
}
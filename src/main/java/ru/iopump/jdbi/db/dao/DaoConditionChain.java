package ru.iopump.jdbi.db.dao;

import java.util.List;
import java.util.StringJoiner;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import ru.iopump.jdbi.db.exception.NoSubQueryResult;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;


@SuppressWarnings("unused")
@NoArgsConstructor
@Slf4j
public final class DaoConditionChain {
    private final List<Item> linkedList = Lists.newLinkedList();

    public DaoConditionChain(@NonNull Condition firstCondition) {
        linkedList.add(new Item(firstCondition));
    }

    public DaoConditionChain and(@NonNull Condition condition) {
        if (linkedList.isEmpty()) {
            linkedList.add(new Item(condition));
        } else {
            linkedList.add(new Item("and", condition));
        }
        return this;
    }

    public DaoConditionChain or(@NonNull Condition condition) {
        if (linkedList.isEmpty()) {
            linkedList.add(new Item(condition));
        } else {
            linkedList.add(new Item("or", condition));
        }
        return this;
    }

    public DaoConditionChain merge(@NonNull DaoConditionChain other) {
        val o = Lists.newLinkedList(other.linkedList);
        o.set(0, new Item("and", o.get(0).condition));
        linkedList.addAll(o);
        return this;
    }

    String asString() throws NoSubQueryResult {
        if (!linkedList.isEmpty()) {
            final StringBuilder sb = new StringBuilder();
            for (Item item : linkedList) {
                if (sb.length() == 0) {
                    final String str = item.condition.asString();
                    if (str != null) {
                        sb.append(str);
                    }
                } else {
                    final String str = item.asString();
                    if (str != null) {
                        sb.append(" ").append(str);
                    }
                }
            }
            return sb.toString();
        } else {
            return "";
        }
    }

    @Override
    public String toString() {
        return Joiner.on(" ").join(linkedList);
    }

    private static class Item {
        private final String logicalOperator;
        private final Condition condition;

        private Item(Condition condition) {
            this("", condition);
        }

        private Item(String logicalOperator, Condition condition) {
            this.logicalOperator = logicalOperator;
            this.condition = condition;
        }

        String asString() throws NoSubQueryResult {
            String res = condition.asString();
            if (res == null) {
                if ("and".equalsIgnoreCase(logicalOperator)) {
                    throw new NoSubQueryResult();
                } else {
                    return null;
                }
            } else {
                return new StringJoiner(" ").add(logicalOperator).add(res).toString();
            }
        }

        @Override
        public String toString() {
            return logicalOperator + " " + condition.toString();
        }
    }
}
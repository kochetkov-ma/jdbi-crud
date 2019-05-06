package ru.iopump.jdbi.db.example;

import javax.persistence.Column;
import javax.persistence.Entity;

import ru.iopump.jdbi.db.entity.AbstractEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
@Data
@Entity
public class ExampleEntity extends AbstractEntity<Integer> {

    @Column(name = "id")
    private Integer id;

    @Column(name = "column")
    private String column;

    @Override
    public Integer id() {
        return id;
    }
}
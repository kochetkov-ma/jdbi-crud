package ru.iopump.jdbi.db.dao;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import ru.iopump.jdbi.db.entity.AbstractEntity;

@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Data
public class OnlineLogEntity extends AbstractEntity<Integer> {

    @Column(name = "record_id")
    private Integer recordId;

    @Column(name = "env_id")
    private String envId;

    @Column(name = "env_timein")
    private LocalDateTime envTimeIn;

    @Column(name = "saf_plan_id")
    private String safPlanId;

    @Column(name = "txn_source")
    private String txnSource;

    @Override
    public Integer id() {
        return recordId;
    }
}
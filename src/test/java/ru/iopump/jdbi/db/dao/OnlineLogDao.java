package ru.iopump.jdbi.db.dao;

import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;

@RegisterBeanMapper(value = OnlineLogEntity.class)
public interface OnlineLogDao extends CrudDao<OnlineLogEntity, Integer> {

    @Override
    default String getTableName() {
        return "online_log";
    }

    @Override
    default String getIdColumnName() {
        return "record_id";
    }
}
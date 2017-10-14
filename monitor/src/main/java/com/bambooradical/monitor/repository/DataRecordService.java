/*
 * Copyright (C) 2017 Peter Withers
 */
package com.bambooradical.monitor.repository;

import com.bambooradical.monitor.model.DataRecord;
import com.google.cloud.Timestamp;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.FullEntity;
import com.google.cloud.datastore.IncompleteKey;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * @since Oct 14, 2017 1:32:09 PM (creation date)
 * @author : Peter Withers <peter@gthb-bambooradical.com>
 */
@Service
public class DataRecordService {

    @Autowired
    Datastore datastore;

    private KeyFactory keyFactory;

    @PostConstruct
    public void initializeKeyFactories() {
        keyFactory = datastore.newKeyFactory().setKind("DataRecord");
    }

    public List<DataRecord> findAll() {
        List<DataRecord> resultList = new ArrayList<>();
        Query<Entity> query = Query.newEntityQueryBuilder()
                .setKind("DataRecord")
                //                .setFilter(PropertyFilter.eq("RemoteAddress", "x"))
                .build();
        QueryResults<Entity> results = datastore.run(query);
        while (results.hasNext()) {
            Entity currentEntity = results.next();
            resultList.add(new DataRecord(
                    (float) currentEntity.getDouble("Temperature"),
                    (float) currentEntity.getDouble("Humidity"),
                    (float) currentEntity.getDouble("Voltage"),
                    currentEntity.getString("MeterLocation"),
                    currentEntity.getString("Error"),
                    new Date(currentEntity.getTimestamp("RecordDate").getSeconds() * 100L)));
        }
        return resultList;
    }

    public long count() {
        throw new UnsupportedOperationException();
    }

    public Entity save(DataRecord dataRecord) {
        IncompleteKey key = keyFactory.setKind("DataRecord").newKey();
        FullEntity entity = FullEntity.newBuilder(key)
                .set("Error", dataRecord.getError())
                .set("Humidity", dataRecord.getHumidity())
                .set("Location", dataRecord.getLocation())
                .set("Temperature", dataRecord.getTemperature())
                .set("Voltage", dataRecord.getVoltage())
                .set("RecordDate", Timestamp.of(dataRecord.getRecordDate()))
                .build();
        return datastore.put(entity);
    }

    public void delete(DataRecord dataRecord) {
        Key key = keyFactory.newKey(dataRecord.getId());
        datastore.delete(key);
    }

    public List<DataRecord> findByLocationStartsWithIgnoreCaseOrderByRecordDateAsc(String location, final Pageable pageable) {
        throw new UnsupportedOperationException();
    }

    public List<DataRecord> findByLocationAndRecordDate(String location, Date recordDate) {
        throw new UnsupportedOperationException();
    }

    public List<DataRecord> findByLocationStartsWithIgnoreCaseAndRecordDateBetweenOrderByRecordDateAsc(String location, Date startDate, Date endDate) {
        throw new UnsupportedOperationException();
    }
}

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
import com.google.cloud.datastore.StructuredQuery;
import com.google.cloud.datastore.StructuredQuery.CompositeFilter;
import com.google.cloud.datastore.StructuredQuery.PropertyFilter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
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
                    currentEntity.getString("Location"),
                    currentEntity.getString("Error"),
                    new Date(currentEntity.getTimestamp("RecordDate").getSeconds() * 1000L)));
        }
        return resultList;
    }

    public long count() {
        long resultCount = 0;
        Query<Entity> query = Query.newEntityQueryBuilder()
                .setKind("DataRecord")
                .build();
        QueryResults<Entity> results = datastore.run(query);
        while (results.hasNext()) {
            results.next();
            resultCount++;
        }
        return resultCount;
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

//    public List<DataRecord> findByLocationStartsWithIgnoreCaseOrderByRecordDateAsc(String location, final Pageable pageable) {
//        throw new UnsupportedOperationException();
//    }
    public List<DataRecord> findByLocationAndRecordDate(String location, Date recordDate) {
        List<DataRecord> resultList = new ArrayList<>();
        Query<Entity> query = Query.newEntityQueryBuilder()
                .setKind("DataRecord")
                .setFilter(CompositeFilter.and(
                        PropertyFilter.eq("Location", location),
                        PropertyFilter.eq("RecordDate", Timestamp.of(recordDate))))
                .build();
        QueryResults<Entity> results = datastore.run(query);
        while (results.hasNext()) {
            Entity currentEntity = results.next();
            resultList.add(new DataRecord(
                    (float) currentEntity.getDouble("Temperature"),
                    (float) currentEntity.getDouble("Humidity"),
                    (float) currentEntity.getDouble("Voltage"),
                    currentEntity.getString("Location"),
                    currentEntity.getString("Error"),
                    new Date(currentEntity.getTimestamp("RecordDate").getSeconds() * 100L)));
        }
        return resultList;
    }

    public List<DataRecord> findByLocationStartsWithIgnoreCaseAndRecordDateBetweenOrderByRecordDateAsc(String location, Date startDate, Date endDate) {
        List<DataRecord> resultList = new ArrayList<>();
        Query<Entity> query = Query.newEntityQueryBuilder()
                .setKind("DataRecord")
                .setFilter(CompositeFilter.and(
                        PropertyFilter.ge("RecordDate", Timestamp.of(startDate)),
                        PropertyFilter.le("RecordDate", Timestamp.of(endDate))
                ))
                .addOrderBy(StructuredQuery.OrderBy.asc("RecordDate"))
                .build();
        QueryResults<Entity> results = datastore.run(query);
        while (results.hasNext()) {
            Entity currentEntity = results.next();
            final String meterLocation = currentEntity.getString("Location");
            if (meterLocation.toLowerCase().startsWith(location.toLowerCase())) {
                resultList.add(new DataRecord(
                        (float) currentEntity.getDouble("Temperature"),
                        (float) currentEntity.getDouble("Humidity"),
                        (float) currentEntity.getDouble("Voltage"), meterLocation,
                        currentEntity.getString("Error"),
                        new Date(currentEntity.getTimestamp("RecordDate").getSeconds() * 100L)));
            }
        }
        return resultList;
    }
}

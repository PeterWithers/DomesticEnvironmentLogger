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
import org.joda.time.LocalDate;
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
                    (currentEntity.contains("Temperature")) ? (float) currentEntity.getDouble("Temperature") : null,
                    (currentEntity.contains("Humidity")) ? (float) currentEntity.getDouble("Humidity") : null,
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
        final Float humidity = dataRecord.getHumidity();
        final FullEntity.Builder<IncompleteKey> builder = FullEntity.newBuilder(key);
        if (humidity != null) {
            builder.set("Humidity", humidity);
        }
        final Float temperature = dataRecord.getTemperature();
        if (temperature != null) {
            builder.set("Temperature", temperature);
        }
        FullEntity entity = builder
                .set("Error", dataRecord.getError())
                .set("Location", dataRecord.getLocation())
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
                    (currentEntity.contains("Temperature")) ? (float) currentEntity.getDouble("Temperature") : null,
                    (currentEntity.contains("Humidity")) ? (float) currentEntity.getDouble("Humidity") : null,
                    (float) currentEntity.getDouble("Voltage"),
                    currentEntity.getString("Location"),
                    currentEntity.getString("Error"),
                    new Date(currentEntity.getTimestamp("RecordDate").getSeconds() * 1000L)));
        }
        return resultList;
    }

    public void insertDailyPeeks(String location, String dateKey, LocalDate date, List<DataRecord> resultList) {
        Query<Entity> query = Query.newEntityQueryBuilder()
                .setKind("DataRecord")
                .setFilter(CompositeFilter.and(
                        //                        PropertyFilter.eq("Location", location),
                        PropertyFilter.ge("RecordDate", Timestamp.of(date.toDate())),
                        PropertyFilter.le("RecordDate", Timestamp.of(date.plusDays(1).toDate()))
                )).build();
        QueryResults<Entity> results = datastore.run(query);
        DataRecord minHumidityRecord = null;
        DataRecord maxHumidityRecord = null;
        DataRecord minTemperatureRecord = null;
        DataRecord maxTemperatureRecord = null;
        while (results.hasNext()) {
            Entity currentEntity = results.next();
            final String meterLocation = currentEntity.getString("Location");
            if (meterLocation.toLowerCase().startsWith(location.toLowerCase())) {
                DataRecord currentRecord = new DataRecord(
                        (currentEntity.contains("Temperature")) ? (float) currentEntity.getDouble("Temperature") : null,
                        (currentEntity.contains("Humidity")) ? (float) currentEntity.getDouble("Humidity") : null,
                        (float) currentEntity.getDouble("Voltage"),
                        currentEntity.getString("Location"),
                        currentEntity.getString("Error"),
                        new Date(currentEntity.getTimestamp("RecordDate").getSeconds() * 1000L));
                if (currentRecord.getHumidity() != null) {
                    if (minHumidityRecord == null) {
                        minHumidityRecord = currentRecord;
                    }
                    if (maxHumidityRecord == null) {
                        maxHumidityRecord = currentRecord;
                    }
                    if (minHumidityRecord.getHumidity() > currentRecord.getHumidity()) {
                        minHumidityRecord = currentRecord;
                    }
                    if (maxHumidityRecord.getHumidity() < currentRecord.getHumidity()) {
                        maxHumidityRecord = currentRecord;
                    }
                }
                if (currentRecord.getTemperature() != null) {
                    if (minTemperatureRecord == null) {
                        minTemperatureRecord = currentRecord;
                    }
                    if (maxTemperatureRecord == null) {
                        maxTemperatureRecord = currentRecord;
                    }
                    if (minTemperatureRecord.getTemperature() > currentRecord.getTemperature()) {
                        minTemperatureRecord = currentRecord;
                    }
                    if (maxTemperatureRecord.getTemperature() < currentRecord.getTemperature()) {
                        maxTemperatureRecord = currentRecord;
                    }
                }
            }
        }
        String dateToday = new LocalDate().toString("yyyy-MM-dd");
        boolean isToday = dateKey.equals(dateToday);
        for (DataRecord currentRecord : new DataRecord[]{
            minHumidityRecord,
            maxHumidityRecord,
            minTemperatureRecord,
            maxTemperatureRecord}) {
            if (currentRecord != null) {
                resultList.add(currentRecord);
                if (!isToday) {
                    IncompleteKey key = keyFactory.setKind("DataRecordPeek").newKey();
                    final Float humidity = currentRecord.getHumidity();
                    final FullEntity.Builder<IncompleteKey> builder = FullEntity.newBuilder(key);
                    if (humidity != null) {
                        builder.set("Humidity", humidity);
                    }
                    final Float temperature = currentRecord.getTemperature();
                    if (temperature != null) {
                        builder.set("Temperature", temperature);
                    }
                    FullEntity entity = builder
                            .set("Error", currentRecord.getError())
                            .set("Location", location) //currentRecord.getLocation()) // insert the short location so that it can be directly used in the query
                            .set("Voltage", currentRecord.getVoltage())
                            .set("RecordDate", Timestamp.of(currentRecord.getRecordDate()))
                            .set("RecordDay", dateKey)
                            .build();
                    datastore.put(entity);
                }
            }
        }
    }

    public boolean findDailyPeeks(String location, String dateKey, List<DataRecord> resultList) {
        Query<Entity> query = Query.newEntityQueryBuilder()
                .setKind("DataRecordPeek")
                .setFilter(CompositeFilter.and(
                        PropertyFilter.eq("Location", location),
                        PropertyFilter.eq("RecordDay", dateKey)))
                .build();
        QueryResults<Entity> results = datastore.run(query);
        if (!results.hasNext()) {
            return false;
        }
        while (results.hasNext()) {
            Entity currentEntity = results.next();
            resultList.add(new DataRecord(
                    (currentEntity.contains("Temperature")) ? (float) currentEntity.getDouble("Temperature") : null,
                    (currentEntity.contains("Humidity")) ? (float) currentEntity.getDouble("Humidity") : null,
                    (float) currentEntity.getDouble("Voltage"),
                    currentEntity.getString("Location"),
                    currentEntity.getString("Error"),
                    new Date(currentEntity.getTimestamp("RecordDate").getSeconds() * 1000L)));
        }
        return true;
    }

    public List<DataRecord> findByLocationStartsWithIgnoreCaseAndRecordDateBetweenOrderByRecordDateAsc(String location, Date startDate, Date endDate) {
        // if the date range is greater than three days, then use min max records
        int daysBetween = (int) ((endDate.getTime() - startDate.getTime()) / (1000 * 60 * 60 * 24));
        List<DataRecord> resultList = new ArrayList<>();
        if (daysBetween < 7) {
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
                            (currentEntity.contains("Temperature")) ? (float) currentEntity.getDouble("Temperature") : null,
                            (currentEntity.contains("Humidity")) ? (float) currentEntity.getDouble("Humidity") : null,
                            (float) currentEntity.getDouble("Voltage"), meterLocation,
                            currentEntity.getString("Error"),
                            new Date(currentEntity.getTimestamp("RecordDate").getSeconds() * 1000L)));
                }
            }
        } else {
            LocalDate start = new LocalDate(startDate);
            LocalDate end = new LocalDate(endDate);
            int insertedDays = 0;
            for (LocalDate date = start; date.isBefore(end); date = date.plusDays(1)) {
                String dateKey = date.toString("yyyy-MM-dd");
                if (!findDailyPeeks(location, dateKey, resultList)) {
                    if (insertedDays < 1) { // only insert only one days data in one request
                        insertDailyPeeks(location, dateKey, date, resultList);
                        insertedDays++;
                    }
                }
            }
        }
        resultList.sort((DataRecord o1, DataRecord o2) -> o1.getRecordDate().compareTo(o2.getRecordDate()));
        return resultList;
    }
}

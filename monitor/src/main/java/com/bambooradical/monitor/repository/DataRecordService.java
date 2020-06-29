/*
 * Copyright (C) 2017 Peter Withers
 */
package com.bambooradical.monitor.repository;

import com.bambooradical.monitor.model.DataRecord;
import com.google.cloud.Timestamp;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.FullEntity;
import com.google.cloud.datastore.IncompleteKey;
import com.google.cloud.datastore.KeyFactory;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import javax.annotation.PostConstruct;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @since 14 Oct 2017 1:32:09 PM (creation date)
 * @author : Peter Withers <peter-gthb@bambooradical.com>
 */
@Service
public class DataRecordService {

    @Autowired
    Datastore datastore;

    private KeyFactory keyFactory;

    final DayOfDataGcsFileStore dayOfDataFileStore = new DayOfDataGcsFileStore();

    //private static final HashMap<String, List<DataRecord>> DAILY_PEEKS = new HashMap<>();
    private static final HashMap<String, List<DataRecord>> DAILY_RECORDS = new HashMap<>();
//    private static final DailyOverview dailyOverview = new DailyOverview();

    @PostConstruct
    public void initializeKeyFactories() {
        keyFactory = datastore.newKeyFactory().setKind("DataRecord");
    }

    private synchronized void updateDayRecordsList(final String keyString, final List<DataRecord> dayRecordsList) {
        DAILY_RECORDS.put(keyString, dayRecordsList);
    }

    /*private synchronized void updateDayPeeksList(final String keyString, final List<DataRecord> dayPeekList) {
        DAILY_PEEKS.put(keyString, dayPeekList);
    }*/

    private synchronized void updateRecordArrays(DataRecord updatedRecord) {
        String recordLocation = updatedRecord.getLocation();
        LocalDate recordDate = new LocalDate(updatedRecord.getRecordDate());
        String dateKey = recordDate.toString("yyyy-MM-dd");
        String keyString = dateKey + "_" + recordLocation;
        //DAILY_PEEKS.remove(keyString);
        final List<DataRecord> dayRecordsList = DAILY_RECORDS.get(keyString);
        if (dayRecordsList == null) {
            final List<DataRecord> dayRecords = new ArrayList<>();
            DAILY_RECORDS.put(keyString, dayRecords);
            dayRecords.add(updatedRecord);
        } else {
            dayRecordsList.add(updatedRecord);
        }
        //updatedRecord.setError(keyString);
    }

    public List<DataRecord> findAll() {
        List<DataRecord> resultList = new ArrayList<>();
        for (List<DataRecord> dataRecordList : DAILY_RECORDS.values()) {
            resultList.addAll(dataRecordList);
        }
        return resultList;
    }

    public long count() {
        long resultCount = 0;
        for (List<DataRecord> dataRecordList : DAILY_RECORDS.values()) {
            resultCount += dataRecordList.size();
        }
        return resultCount;
    }

    public void clearCachedData() {
        //DAILY_PEEKS.clear();
        DAILY_RECORDS.clear();
    }

    public void save(DataRecord dataRecord) {
        IncompleteKey key = keyFactory.setKind("DataRecord").newKey();
        final Float humidity = dataRecord.getHumidity();
        final FullEntity.Builder<IncompleteKey> builder = FullEntity.newBuilder(key);
        if (humidity != null) {
            builder.set("Humidity", humidity);
        }
        if (dataRecord.getTvoc() != null) {
            builder.set("tvoc", dataRecord.getTvoc());
        }
        if (dataRecord.getCo2() != null) {
            builder.set("co2", dataRecord.getCo2());
        }
        if (dataRecord.getDustAvg() != null) {
            builder.set("dustAvg", dataRecord.getDustAvg());
        }
        if (dataRecord.getDustQ1() != null) {
            builder.set("dustQ1", dataRecord.getDustQ1());
        }
        if (dataRecord.getDustQ2() != null) {
            builder.set("dustQ2", dataRecord.getDustQ2());
        }
        if (dataRecord.getDustQ3() != null) {
            builder.set("dustQ3", dataRecord.getDustQ3());
        }
        if (dataRecord.getDustOutliers() != null) {
            builder.set("dustOutliers", dataRecord.getDustOutliers());
        }
        if (dataRecord.getKPa() != null) {
            builder.set("kpa", dataRecord.getKPa());
        }
        final Float temperature = dataRecord.getTemperature();
        if (temperature != null) {
            builder.set("Temperature", temperature);
        }
        if (dataRecord.getError() != null) {
            builder.set("Error", dataRecord.getError());
        }
        if (dataRecord.getVoltage() != null) {
            builder.set("Voltage", dataRecord.getVoltage());
        }
        FullEntity entity = builder
                .set("Location", dataRecord.getLocation())
                .set("RecordDate", Timestamp.of(dataRecord.getRecordDate()))
                .build();
        datastore.put(entity);
        updateRecordArrays(dataRecord);
    }

    public void delete(DataRecord dataRecord) {
        throw new UnsupportedOperationException("delete of dataRecord is no longer supported");
//        Key key = keyFactory.newKey(dataRecord.getId());
//        datastore.delete(key);
    }

//    public List<DataRecord> findByLocationStartsWithIgnoreCaseOrderByRecordDateAsc(String location, final Pageable pageable) {
//        throw new UnsupportedOperationException();
//    }
    public List<DataRecord> findByLocationAndRecordDate(String location, Date recordDate) {
        LocalDate recordLocalDate = new LocalDate(recordDate);
        String dateKey = recordLocalDate.toString("yyyy-MM-dd");
        String keyString = dateKey + "_" + location;
        return DAILY_RECORDS.get(keyString);
//        List<DataRecord> resultList = new ArrayList<>();
//        Query<Entity> query = Query.newEntityQueryBuilder()
//                .setKind("DataRecord")
//                .setFilter(CompositeFilter.and(
//                        PropertyFilter.eq("Location", location),
//                        PropertyFilter.eq("RecordDate", Timestamp.of(recordDate))))
//                .build();
//        QueryResults<Entity> results = datastore.run(query);
//        while (results.hasNext()) {
//            Entity currentEntity = results.next();
//            resultList.add(new DataRecord(
//                    (currentEntity.contains("Temperature")) ? (float) currentEntity.getDouble("Temperature") : null,
//                    (currentEntity.contains("Humidity")) ? (float) currentEntity.getDouble("Humidity") : null,
//                    (float) currentEntity.getDouble("Voltage"),
//                    currentEntity.getString("Location"),
//                    currentEntity.getString("Error"),
//                    new Date(currentEntity.getTimestamp("RecordDate").getSeconds() * 1000L)));
//        }
//        return resultList;
    }

    /*public void updateDailyPeeks(String dateKey, final DataRecord dataRecord, HashMap<String, List<DataRecord>> storedPeekData) {
        DataRecord firstRecord = dataRecord;
        DataRecord lastRecord = null;
        DataRecord minHumidityRecord = null;
        DataRecord maxHumidityRecord = null;
        DataRecord minTemperatureRecord = null;
        DataRecord maxTemperatureRecord = null;
        final String location = dataRecord.getLocation().toLowerCase();
        String currentKey = dateKey + "_" + location;
        if (DAILY_PEEKS.containsKey(currentKey)) {
            for (DataRecord currentRecord : DAILY_PEEKS.get(currentKey)) {
                if (firstRecord == null) {
                    firstRecord = currentRecord;
                } else {
                    firstRecord = (firstRecord.getRecordDate().before(currentRecord.getRecordDate())) ? firstRecord : currentRecord;
                }
                if (lastRecord == null) {
                    lastRecord = currentRecord;
                } else {
                    lastRecord = (lastRecord.getRecordDate().after(currentRecord.getRecordDate())) ? lastRecord : currentRecord;
                }
//            final String meterLocation = currentEntity.getString("Location");
//            if (meterLocation.toLowerCase().startsWith(location.toLowerCase())) {
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
//            }
//        }
//        if (minHumidityRecord == null && maxHumidityRecord == null && minTemperatureRecord == null && maxTemperatureRecord == null) {
//                IncompleteKey key = keyFactory.setKind("DataRecordPeek").newKey();
//                final FullEntity.Builder<IncompleteKey> builder = FullEntity.newBuilder(key);
//                FullEntity entity = builder
//                        .set("NoRecords", true)
//                        .set("Location", location) //currentRecord.getLocation()) // insert the short location so that it can be directly used in the query
//                        .set("RecordDate", Timestamp.of(date.toDate()))
//                        .set("RecordDay", dateKey)
//                        .build();
        // do not store NoRecords entities, it is enough to keep a record in the hashtable
//                datastore.put(entity);
//            DAILY_PEEKS.put(dateKey + "_" + location, new ArrayList<>());
//        } else {
        final ArrayList<DataRecord> arrayDataRecord = new ArrayList<>();
        for (DataRecord currentRecord : new DataRecord[]{
            firstRecord,
            minHumidityRecord,
            maxHumidityRecord,
            minTemperatureRecord,
            maxTemperatureRecord,
            lastRecord}) {
            if (currentRecord != null) {
//                resultList.add(currentRecord);
//                        IncompleteKey key = keyFactory.setKind("DataRecordPeek").newKey();
//                        final Float humidity = currentRecord.getHumidity();
//                        final FullEntity.Builder<IncompleteKey> builder = FullEntity.newBuilder(key);
//                        if (humidity != null) {
//                            builder.set("Humidity", humidity);
//                        }
//                        final Float temperature = currentRecord.getTemperature();
//                        if (temperature != null) {
//                            builder.set("Temperature", temperature);
//                        }
//                        FullEntity entity = builder
//                                .set("Error", currentRecord.getError())
//                                .set("Location", location) //currentRecord.getLocation()) // insert the short location so that it can be directly used in the query
//                                .set("Voltage", currentRecord.getVoltage())
//                                .set("RecordDate", Timestamp.of(currentRecord.getRecordDate()))
//                                .set("RecordDay", dateKey)
//                                .build();
//                        datastore.put(entity);
                arrayDataRecord.add(currentRecord);
//                }
            }
        }
        updateDayPeeksList(dateKey + "_" + location, arrayDataRecord);
        storedPeekData.put(dateKey + "_" + location, arrayDataRecord);
    }*/

    private void findDailyRecords(String location, LocalDate startDate, LocalDate endDate, List<DataRecord> resultList) {
        for (LocalDate date = startDate; date.isBefore(endDate.plusDays(1)); date = date.plusDays(1)) {
            String dateKey = date.toString("yyyy-MM-dd");
            //System.out.println(dateKey);
            for (String currentKey : DAILY_RECORDS.keySet()) {
                //	System.out.println(currentKey + ":" + dateKey + "_" + location.toLowerCase());
                if (currentKey.toLowerCase().startsWith(dateKey + "_" + location.toLowerCase())) {
                    resultList.addAll(DAILY_RECORDS.get(currentKey));
                }
            }
        }
    }

    /*private void findDailyPeeks(String location, LocalDate startDate, LocalDate endDate, List<DataRecord> resultList) {
//    public Set<String> findDailyPeeks(String location, Date startDate, Date endDate, List<DataRecord> resultList) {
//        Set<String> dateKeys = new HashSet<>();
//        if (DAILY_PEEKS.isEmpty()) {
//            Query<Entity> query = Query.newEntityQueryBuilder()
//                    .setKind("DataRecordPeek")
//                    //                .setFilter(CompositeFilter.and(
//                    //                        //                        PropertyFilter.eq("Location", location),
//                    //                        PropertyFilter.ge("RecordDate", Timestamp.of(startDate)),
//                    //                        PropertyFilter.le("RecordDate", Timestamp.of(endDate))))
//                    .build();
//            QueryResults<Entity> results = datastore.run(query);
//            while (results.hasNext()) {
//                Entity currentEntity = results.next();
//                final String meterLocation = currentEntity.getString("Location");
////                if (meterLocation.toLowerCase().startsWith(location.toLowerCase())) {
////                    dateKeys.add(currentEntity.getString("RecordDay"));
//                List<DataRecord> arrayDataRecord = DAILY_PEEKS.get(currentEntity.getString("RecordDay") + "_" + meterLocation);
//                if (arrayDataRecord == null) {
//                    arrayDataRecord = new ArrayList<>();
//                    DAILY_PEEKS.put(currentEntity.getString("RecordDay") + "_" + meterLocation, arrayDataRecord);
//                }
//                if (!currentEntity.contains("NoRecords")) {
//                    arrayDataRecord.add(new DataRecord(
//                            (currentEntity.contains("Temperature")) ? (float) currentEntity.getDouble("Temperature") : null,
//                            (currentEntity.contains("Humidity")) ? (float) currentEntity.getDouble("Humidity") : null,
//                            (float) currentEntity.getDouble("Voltage"),
//                            currentEntity.getString("Location"),
//                            currentEntity.getString("Error"),
//                            new Date(currentEntity.getTimestamp("RecordDate").getSeconds() * 1000L)));
////                } else {
////                    DAILY_PEEKS.put(currentEntity.getString("RecordDay") + "_" + meterLocation, null);
//                }
////                }
//            }
//        }
//        int insertedDays = 0;
        for (LocalDate date = startDate; date.isBefore(endDate.plusDays(1)); date = date.plusDays(1)) {
            String dateKey = date.toString("yyyy-MM-dd");
//            if (!DAILY_PEEKS.containsKey(dateKey + "_" + location)) {
//                //if (insertedDays < 100) { // only insert only 100 days data in one request
//                    insertDailyPeeks(location, dateKey, date, resultList);
////                    insertedDays++;
//                //}
//            } else {
            for (String currentKey : DAILY_PEEKS.keySet()) {
                if (currentKey.toLowerCase().startsWith(dateKey + "_" + location.toLowerCase())) {
                    resultList.addAll(DAILY_PEEKS.get(currentKey));
                }
            }
//            }
        }
//        if (endDate.isAfter(new LocalDate().minusDays(1))) {
//            for (String lastRecordLocation : LATEST_RECORDS.keySet()) {
//                if (lastRecordLocation.toLowerCase().startsWith(location.toLowerCase())) {
//                    resultList.add(LATEST_RECORDS.get(lastRecordLocation));
//                }
//            }
//        }
    }*/
    //    public HashMap<DataRecord> findByRecordDateBetweenOrderByRecordDateAsc(String[] locations, Date startDate, Date endDate) {

    public List<DataRecord> findByLocationStartsWithIgnoreCaseAndRecordDateBetweenOrderByRecordDateAsc(String location, Date startDate, Date endDate) {
        // if the date range is greater than three days, then use min max records
        int daysBetween = (int) ((endDate.getTime() - startDate.getTime()) / (1000 * 60 * 60 * 24));
        List<DataRecord> resultList = new ArrayList<>();
        LocalDate start = new LocalDate(startDate);
        LocalDate end = new LocalDate(endDate);
        //if (daysBetween < 14) {
            findDailyRecords(location, start, end, resultList);
        //} else {
            //findDailyPeeks(location, start, end, resultList);
        //}
        resultList.sort((DataRecord o1, DataRecord o2) -> o1.getRecordDate().compareTo(o2.getRecordDate()));
        return resultList;
    }
}

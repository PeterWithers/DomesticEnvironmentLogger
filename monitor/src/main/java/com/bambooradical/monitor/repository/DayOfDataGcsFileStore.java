/*
 * Copyright (C) 2019 Peter Withers
 */
package com.bambooradical.monitor.repository;

import com.bambooradical.monitor.model.DailyOverview;
import com.bambooradical.monitor.model.DataRecord;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.appengine.tools.cloudstorage.GcsFileOptions;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsInputChannel;
import com.google.appengine.tools.cloudstorage.GcsOutputChannel;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;
import com.google.appengine.tools.cloudstorage.RetryParams;
import com.google.cloud.Timestamp;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.StructuredQuery;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @since 6 July 2019 4:23:27 PM (creation date)
 * @author : Peter Withers <peter-gthb@bambooradical.com>
 */
@Service
public class DayOfDataGcsFileStore {

    @Autowired
    Datastore datastore;

    private final GcsService gcsService = GcsServiceFactory.createGcsService(new RetryParams.Builder().initialRetryDelayMillis(10).retryMaxAttempts(3).totalRetryPeriodMillis(100).build());

    public InputStream getOverviewStream() {
        GcsFilename dailyOverviewFileName = new GcsFilename("staging.domesticenvironmentlogger.appspot.com", "DailyOverview");
        GcsInputChannel readChannel = gcsService.openPrefetchingReadChannel(dailyOverviewFileName, 0, 2097152);
        return Channels.newInputStream(readChannel);
    }

    public InputStream getDayOfDataStream(int yyyy, int MM, int dd) {
        String dateKey = String.format("%04d-%02d-%02d", yyyy, MM, dd);
        GcsFilename dayOfDataOverviewFileName = new GcsFilename("staging.domesticenvironmentlogger.appspot.com", "DayOfData" + dateKey);
        GcsInputChannel readChannel = gcsService.openPrefetchingReadChannel(dayOfDataOverviewFileName, 0, 2097152);
        return Channels.newInputStream(readChannel);
    }

    public List<DataRecord> getDataRecordList(final String dateKey) throws IOException {
        GcsFileOptions instance = GcsFileOptions.getDefaultInstance();
        GcsFilename fileName = new GcsFilename("staging.domesticenvironmentlogger.appspot.com", "DayOfData" + dateKey);
        final List<DataRecord> dataRecordList;
        ObjectMapper dayMapper = new ObjectMapper();
        dayMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        GcsInputChannel readChannel = gcsService.openPrefetchingReadChannel(fileName, 0, 2097152);
        dataRecordList = dayMapper.readValue(Channels.newInputStream(readChannel), new TypeReference<List<DataRecord>>() {
        });
        return dataRecordList;
    }

    public void storeDayOfData(List<DataRecord> dataRecordList, final String dateKey) {
        try {
            GcsFileOptions instance = GcsFileOptions.getDefaultInstance();
            ObjectMapper daylyMapper = new ObjectMapper();
            GcsFilename fileName = new GcsFilename("staging.domesticenvironmentlogger.appspot.com", "DayOfData" + dateKey);
            daylyMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            GcsOutputChannel outputChannel = gcsService.createOrReplace(fileName, instance);
            daylyMapper.writeValue(Channels.newOutputStream(outputChannel), dataRecordList);
        } catch (IOException exception2) {
            System.out.println(exception2.getMessage());
        }
    }

    public void saveDailyOverview(DailyOverview dailyOverview) throws IOException {
        dailyOverview.calculateSummaryData();
        ObjectMapper outputMapper = new ObjectMapper();
        GcsFileOptions instance = GcsFileOptions.getDefaultInstance();
        GcsFilename fileName = new GcsFilename("staging.domesticenvironmentlogger.appspot.com", "DailyOverview");
        GcsOutputChannel outputChannel = gcsService.createOrReplace(fileName, instance);
        outputMapper.writeValue(Channels.newOutputStream(outputChannel), dailyOverview);
    }

    public DailyOverview loadDailyOverview() {
        try {
            ObjectMapper dailyOverviewMapper = new ObjectMapper();
            dailyOverviewMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            GcsFilename dailyOverviewFileName = new GcsFilename("staging.domesticenvironmentlogger.appspot.com", "DailyOverview");
            GcsInputChannel readChannel = gcsService.openPrefetchingReadChannel(dailyOverviewFileName, 0, 2097152);
            Map<String, Map<String, Map<String, LinkedHashMap<String, float[]>>>> instanceMap = dailyOverviewMapper.readValue(Channels.newInputStream(readChannel), new TypeReference<Map<String, Map<String, Map<String, LinkedHashMap<String, float[]>>>>>() {
            });
            DailyOverview dailyOverview = new DailyOverview();
            dailyOverview.addData(instanceMap);
            return dailyOverview;
        } catch (IOException exception) {
            System.out.println(exception.getMessage());
            return new DailyOverview();
        }
    }

    public void loadDayOfData(Date startDate, Date endDate) {
//        HashMap<String, List<DataRecord>> storedPeekData;
        final DailyOverview dailyOverview = loadDailyOverview();
        boolean loadedMoreData = false;
//        try {
//            ObjectMapper peeksMapper = new ObjectMapper();
//            peeksMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
//            GcsFilename peeksFileName = new GcsFilename("staging.domesticenvironmentlogger.appspot.com", "DayPeeksOfData");
//            GcsInputChannel readChannel = gcsService.openPrefetchingReadChannel(peeksFileName, 0, 2097152);
//            storedPeekData = peeksMapper.readValue(Channels.newInputStream(readChannel), new TypeReference<Map<String, List<DataRecord>>>() {
//            });
//        } catch (IOException exception) {
//            System.out.println(exception.getMessage());
//            storedPeekData = new HashMap<>();
//        }

        for (LocalDate date = new LocalDate(startDate); date.isBefore(new LocalDate(endDate).plusDays(1)); date = date.plusDays(1)) {
            String dateKey = date.toString("yyyy-MM-dd");
            boolean hasDate = false;
            if (!date.equals(new LocalDate())) {
//                    for (String currentKey : storedPeekData.keySet()) {
//                        if (currentKey.toLowerCase().startsWith(dateKey + "_")) {
//                            if (!DAILY_RECORDS.containsKey(currentKey)) {
//                                updateDayRecordsList(currentKey, storedPeekData.get(currentKey));
//                            }
//                        }
//                    }
//                for (String currentKey : storedPeekData.keySet()) {
//                    if (currentKey.toLowerCase().startsWith(dateKey + "_")) {
//                        updateDayPeeksList(currentKey, storedPeekData.get(currentKey));
//                        //if (!DAILY_RECORDS.containsKey(currentKey)) {
//                        //    updateDayRecordsList(currentKey, storedPeekData.get(currentKey));
//                        //}
//                        hasDate = true;
//                        // break;
//                    }
//                }
                hasDate = dailyOverview.hasDate(dateKey);
                if (!hasDate) {
                    try {
                        final List<DataRecord> dataRecordList = getDataRecordList(dateKey);

                        for (final DataRecord dataRecord : dataRecordList) {
                            dailyOverview.addRecord(dateKey, dataRecord);
//                            updateDailyPeeks(dateKey, dataRecord, storedPeekData);
                            loadedMoreData = true;
                        }
                    } catch (IOException exception) {
                        Query<Entity> query = Query.newEntityQueryBuilder()
                                .setKind("DataRecord")
                                .setFilter(StructuredQuery.CompositeFilter.and(
                                        StructuredQuery.PropertyFilter.ge("RecordDate", Timestamp.of(date.toDate())),
                                        StructuredQuery.PropertyFilter.le("RecordDate", Timestamp.of(date.plusDays(1).toDate()))
                                ))
                                .addOrderBy(StructuredQuery.OrderBy.asc("RecordDate"))
                                .build();
                        QueryResults<Entity> results = datastore.run(query);
                        final List<DataRecord> dataRecordList = new ArrayList<>();
                        while (results.hasNext()) {
                            Entity currentEntity = results.next();
                            final DataRecord dataRecord = new DataRecord(
                                    (currentEntity.contains("Temperature")) ? (float) currentEntity.getDouble("Temperature") : null,
                                    (currentEntity.contains("Humidity")) ? (float) currentEntity.getDouble("Humidity") : null,
				    (currentEntity.contains("tvoc")) ? (int) currentEntity.getDouble("tvoc") : null,
                                    (currentEntity.contains("tvoc")) ? (int) currentEntity.getDouble("co2") : null,
                                    (currentEntity.contains("pa")) ? (int) currentEntity.getDouble("pa") : null,
                                    (float) currentEntity.getDouble("Voltage"), currentEntity.getString("Location"),
                                    currentEntity.getString("Error"),
                                    new Date(currentEntity.getTimestamp("RecordDate").getSeconds() * 1000L));
//                        updateRecordArrays(dataRecord);
                            dailyOverview.addRecord(dateKey, dataRecord);
//                            updateDailyPeeks(dateKey, dataRecord, storedPeekData);
                            loadedMoreData = true;
                            dataRecordList.add(dataRecord);
                        }
                        storeDayOfData(dataRecordList, dateKey);
                    }
                }
            }
        }
        if (loadedMoreData) {
            try {
                saveDailyOverview(dailyOverview);
//                ObjectMapper outputMapper = new ObjectMapper();
//        mapper.configure(JsonParser.Feature.IGNORE_UNDEFINED, true);
//        mapper.enable(SerializationFeature.INDENT_OUTPUT);
//                outputMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
//            String todayDateKey = new LocalDate().toString("yyyy-MM-dd");
//            final HashMap<String, List<DataRecord>> storedData = new HashMap<>();
//            for (String currentKey : DAILY_PEEKS.keySet()) {
//                if (!currentKey.startsWith(todayDateKey)) {
//                    storedData.put(currentKey, DAILY_PEEKS.get(currentKey));
//                }
//            }
//            Key key = keyFactory.setKind("AllDataRecords").newKey("DaysOfData");
//            final FullEntity.Builder<IncompleteKey> builder = FullEntity.newBuilder(key);
//            for (String currentKey : DAILY_RECORDS.keySet()) {
//                if (!currentKey.startsWith(todayDateKey)) {
//                    builder.set(currentKey, mapper.writeValueAsString(DAILY_RECORDS.get(currentKey)));
//                }
//            }
//            FullEntity entity = builder.build();
//            datastore.put(entity);
//                GcsFileOptions instance = GcsFileOptions.getDefaultInstance();
//                GcsFilename fileName = new GcsFilename("staging.domesticenvironmentlogger.appspot.com", "DayPeeksOfData");
//                GcsOutputChannel outputChannel = gcsService.createOrReplace(fileName, instance);
//                outputMapper.writeValue(Channels.newOutputStream(outputChannel), storedPeekData);
            } catch (IOException exception) {
                System.out.println(exception.getMessage());
            }
        }
    }

}

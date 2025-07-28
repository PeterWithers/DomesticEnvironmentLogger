/*
 * Copyright (C) 2018 Peter Withers
 */
package com.bambooradical.monitor.repository;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.StringValue;
//import com.google.cloud.datastore.KeyFactory;
import java.util.Date;
//import javax.annotation.PostConstruct;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @since Apr 24, 2018 20:23:24 PM (creation date)
 * @author Peter Withers <peter-gthb@bambooradical.com>
 */
@Service
public class MagnitudeRecordService {

    @Autowired
    Datastore datastore;

//    private KeyFactory keyFactory;

//    @PostConstruct
//    public void initializeKeyFactories() {
//        keyFactory = datastore.newKeyFactory().setKind("MagnitudeRecords");
//    }

    public void save(String magnitudes, String maxMsError, String location, Date recordDate) {
        String dateKey = new LocalDate(recordDate).toString("yyyy-MM-dd");
        String timeKey = new LocalDateTime(recordDate).toString("HH:mm");
        Key magnitudeRecordsKey = datastore.newKeyFactory().setKind("MagnitudeRecords").newKey(dateKey + "_" + location);
        Entity magnitudeRecordsEntity = datastore.get(magnitudeRecordsKey);
        if (magnitudeRecordsEntity == null) {
            datastore.add(Entity.newBuilder(magnitudeRecordsKey)
                    .set(timeKey, StringValue.newBuilder(magnitudes).setExcludeFromIndexes(true).build())
                    .set(timeKey + "_maxMsError", maxMsError)
                    .build());
        } else { //if (!magnitudeRecordsEntity.contains(programRecord.getKey())) {
            datastore.update(Entity.newBuilder(magnitudeRecordsEntity)
                    .set(timeKey, StringValue.newBuilder(magnitudes).setExcludeFromIndexes(true).build())
                    .set(timeKey + "_maxMsError", maxMsError)
                    .build());
        }
    }
    
    public String getDay(String location, String dateString) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{");
        String dateKey = (dateString.isEmpty()) ? new LocalDate().toString("yyyy-MM-dd") : dateString;
        Key magnitudeRecordsKey = datastore.newKeyFactory().setKind("MagnitudeRecords").newKey(dateKey + "_" + location);
        Entity magnitudeRecordsEntity = datastore.get(magnitudeRecordsKey);
        if (magnitudeRecordsEntity != null) {
            for (String propertyName : magnitudeRecordsEntity.getNames()) {
                stringBuilder.append("\"");
                stringBuilder.append(propertyName);
                stringBuilder.append("\":\"");
                stringBuilder.append(magnitudeRecordsEntity.getString(propertyName).replaceAll("\n", ";"));
                stringBuilder.append("\",\n");
            }
	    //stringBuilder.append("}");
	    stringBuilder.setCharAt(stringBuilder.length() - 2, '}');
	} else {
            stringBuilder.append("no magnitudes found (");
            stringBuilder.append(location);
            stringBuilder.append("_");
            stringBuilder.append(dateKey);
            stringBuilder.append(")");
        }
        return stringBuilder.toString();
    }
}


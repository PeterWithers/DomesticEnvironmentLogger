/*
 * Copyright (C) 2017 Peter Withers
 */
package com.bambooradical.monitor.repository;

import com.bambooradical.monitor.model.ProgramRecord;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
//import com.google.cloud.datastore.KeyFactory;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
//import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @since Dec 30, 2017 12:17:12 PM (creation date)
 * @author : Peter Withers <peter-gthb@bambooradical.com>
 */
@Service
public class LightingService {

    @Autowired
    Datastore datastore;

//    private KeyFactory keyFactory;

//    private static final HashMap<String, String> HOURLY_PROGRAMS = new HashMap<>();
    private static final List<ProgramRecord> PROGRAM_RECORDS = new ArrayList<>();

//    @PostConstruct
//    public void initializeKeyFactories() {
//        keyFactory = datastore.newKeyFactory().setKind("LightSettings");
//    }

    public List<ProgramRecord> getProgramRecords() throws ParseException {
        if (PROGRAM_RECORDS.isEmpty()) {
            Key lightSettingsKey = datastore.newKeyFactory().setKind("LightSettings").newKey("defaultProgram");
            Entity lightSettings = datastore.get(lightSettingsKey);
            if (lightSettings != null) {
                for (String propertyName : lightSettings.getNames()) {
                    PROGRAM_RECORDS.add(new ProgramRecord(lightSettings.getString(propertyName)));
                }
            }
        }
        return PROGRAM_RECORDS;
    }

//    private String encodeHour(int milliseconds) {
//        return "ms_" + milliseconds;
    private String encodeHour(int hour) {
        return "hour_" + hour;
    }

    public void deleteProgram(final ProgramRecord programRecord) {
        Key lightSettingsKey = datastore.newKeyFactory().setKind("LightSettings").newKey("defaultProgram");
        Entity lightSettingsEntity = datastore.get(lightSettingsKey);
        datastore.update(Entity.newBuilder(lightSettingsEntity).remove(programRecord.getKey()).build());
        PROGRAM_RECORDS.clear();
    }

    public void updateProgram(final ProgramRecord programRecord) {
        Key lightSettingsKey = datastore.newKeyFactory().setKind("LightSettings").newKey("defaultProgram");
        Entity lightSettingsEntity = datastore.get(lightSettingsKey);
        datastore.update(Entity.newBuilder(lightSettingsEntity).set(programRecord.getKey(), programRecord.getProgramCode()).build());
        PROGRAM_RECORDS.clear();
    }

    public void addProgram(final ProgramRecord programRecord) {
        Key lightSettingsKey = datastore.newKeyFactory().setKind("LightSettings").newKey("defaultProgram");
        Entity lightSettingsEntity = datastore.get(lightSettingsKey);
        if (lightSettingsEntity == null) {
            datastore.add(Entity.newBuilder(lightSettingsKey).set(programRecord.getKey(), programRecord.getProgramCode()).build());
        } else if (!lightSettingsEntity.contains(programRecord.getKey())) {
            datastore.update(Entity.newBuilder(lightSettingsEntity).set(programRecord.getKey(), programRecord.getProgramCode()).build());
        }
        PROGRAM_RECORDS.clear();
    }

    public List<ProgramRecord> findProgram(final int millisOfDay) {
//        if (HOURLY_PROGRAMS.isEmpty()) {
//            Key lightSettingsKey = keyFactory.newKey("defaultSettings");
//            Entity lightSettings = datastore.get(lightSettingsKey);
//            if (lightSettings != null) {
//                for (String propertyName : lightSettings.getNames()) {
//                    HOURLY_PROGRAMS.put(propertyName, lightSettings.getString(propertyName));
//                }
//            }
//        }
        try {
            getProgramRecords();
        } catch (ParseException parseException) {
            System.err.print(parseException.getMessage());
        }
        for (ProgramRecord programRecord : PROGRAM_RECORDS) {
            programRecord.setOffset(millisOfDay);
        }
        Collections.sort(PROGRAM_RECORDS);
        return PROGRAM_RECORDS;
    }
}

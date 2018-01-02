/*
 * Copyright (C) 2017 Peter Withers
 */
package com.bambooradical.monitor.repository;

import com.bambooradical.monitor.model.ProgramRecord;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;
import java.util.HashMap;
import javax.annotation.PostConstruct;
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

    private KeyFactory keyFactory;

    private static final HashMap<String, String> HOURLY_PROGRAMS = new HashMap<>();

    @PostConstruct
    public void initializeKeyFactories() {
        keyFactory = datastore.newKeyFactory().setKind("LightSettings");
    }

    public ProgramRecord[] getProgramRecords() {
        return new ProgramRecord[]{
            new ProgramRecord("location", 0, "00ff00", true),
            new ProgramRecord("location", 3 * 60 * 60 * 1000, "aaff00", true),
            new ProgramRecord("location", 9 * 60 * 60 * 1000, "aaff00", true),
            new ProgramRecord("location", 12 * 60 * 60 * 1000, "eeff00", true),
            new ProgramRecord("location", 14 * 60 * 60 * 1000, "00ffff", true),
            new ProgramRecord("location", 17 * 60 * 60 * 1000, "11ff00", true),
            new ProgramRecord("location", 19 * 60 * 60 * 1000, "ffff00", true),
            new ProgramRecord("location", 20 * 60 * 60 * 1000, "eeff00", true),
            new ProgramRecord("location", 21 * 60 * 60 * 1000, "00ff00", true),
            new ProgramRecord("location", 22 * 60 * 60 * 1000, "66ff00", true),
            new ProgramRecord("location", 23 * 60 * 60 * 1000, "11ff00", true),};
    }

//    private String encodeHour(int milliseconds) {
//        return "ms_" + milliseconds;
    private String encodeHour(int hour) {
        return "hour_" + hour;
    }

    public void updateProgram(int hour, String program) {
        Key lightSettingsKey = keyFactory.newKey("defaultSettings");
        Entity lightSettingsEntity = datastore.get(lightSettingsKey);
        if (lightSettingsEntity == null) {
            datastore.add(Entity.newBuilder(lightSettingsKey).set(encodeHour(hour), program).build());
        } else {
            datastore.update(Entity.newBuilder(lightSettingsEntity).set(encodeHour(hour), program).build());
        }
        HOURLY_PROGRAMS.clear();
    }

    public String findProgram(int hour) {
        if (HOURLY_PROGRAMS.isEmpty()) {
            Key lightSettingsKey = keyFactory.newKey("defaultSettings");
            Entity lightSettings = datastore.get(lightSettingsKey);
            if (lightSettings != null) {
                for (String propertyName : lightSettings.getNames()) {
                    HOURLY_PROGRAMS.put(propertyName, lightSettings.getString(propertyName));
                }
            }
        }
        return HOURLY_PROGRAMS.get(encodeHour(hour));
    }
}

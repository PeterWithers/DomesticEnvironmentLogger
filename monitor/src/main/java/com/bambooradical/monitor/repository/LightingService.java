/*
 * Copyright (C) 2017 Peter Withers
 */
package com.bambooradical.monitor.repository;

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

    private String encodeHour(int hour) {
        return "hour_" + hour;
    }

    public void updateProgram(int hour, String program) {
        Key lightSettingsKey = keyFactory.newKey("defaultSettings");
        Entity lightSettingsEntity = datastore.get(lightSettingsKey);
        if (lightSettingsEntity == null) {
            lightSettingsEntity = datastore.add(Entity.newBuilder(lightSettingsKey).set(encodeHour(hour), program).build());
        } else {
            Entity.newBuilder(lightSettingsEntity).set(encodeHour(hour), program).build();
        }
        datastore.update(lightSettingsEntity);
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

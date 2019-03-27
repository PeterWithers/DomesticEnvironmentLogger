/*
 * Copyright (C) 2019 Peter Withers
 */
package com.bambooradical.monitor.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @since Mar 26, 2019 22:20:38 PM (creation date)
 * @author : Peter Withers <peter-gthb@bambooradical.com>
 */
public class DailyOverview {

    private final Map<String, Map> dateMap = new HashMap<>();
    private final Map<String, DaySummaryData> daySummaryMap;

    public class DaySummaryData {

        float average;
        float upperPeek;
        float upperQuartile;
        float middleQuartile;
        float lowerQuartile;
        float lowerPeek;
    }

    public DailyOverview() {
        this.daySummaryMap = new HashMap<>();
    }

    public DailyOverview(final Map<String, DaySummaryData> daySummaryMap) {
        this.daySummaryMap = daySummaryMap;
    }

    public boolean hasDate(String dateKey) {
        return dateMap.containsKey(dateKey);
    }

    public void addRecord(final String dateKey, final DataRecord dataRecord) {
        final String location = dataRecord.getLocation().toLowerCase();
        if (dataRecord.getHumidity() != null) {
            addRecord(dateKey, location, "humidity", dataRecord.getHumidity());
        }
        if (dataRecord.getTemperature() != null) {
            addRecord(dateKey, location, "temperature", dataRecord.getTemperature());
        }
    }

    public void addRecord(final String dateKey, final String locationKey, final String channelKey, final float value) {
        final Map<String, Map> locationMap;
        if (dateMap.containsKey(dateKey)) {
            locationMap = dateMap.get(dateKey);
        } else {
            locationMap = new HashMap<>();
            dateMap.put(dateKey, locationMap);
        }
        final Map<String, SortedSet<Float>> channelMap;
        if (locationMap.containsKey(locationKey)) {
            channelMap = locationMap.get(locationKey);
        } else {
            channelMap = new HashMap<>();
            locationMap.put(locationKey, channelMap);
        }
        final SortedSet<Float> valuesList;
        if (channelMap.containsKey(channelKey)) {
            valuesList = channelMap.get(channelKey);
        } else {
            valuesList = new TreeSet<>();
            channelMap.put(channelKey, valuesList);
        }
        valuesList.add(value);
    }

    public DaySummaryData getDaySummaryData(final String dateKey, final String locationKey, final String channelKey) {
        // todo: retrieve the DaySummaryData
        return new DaySummaryData();
    }

    public void calculateSummaryData() {
        // todo: traverse the freshly added data and calculate the respective DaySummaryData
    }

    @JsonAnyGetter
    public Map<String, Map> getDateMap() {
        return dateMap;
    }

//    @JsonAnyGetter
//    public Map<String, DaySummaryData> getDaySummaryMap() {
//        return daySummaryMap;
//    }
}

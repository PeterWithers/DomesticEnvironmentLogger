/*
 * Copyright (C) 2019 Peter Withers
 */
package com.bambooradical.monitor.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @since Mar 26, 2019 22:20:38 PM (creation date)
 * @author : Peter Withers <peter-gthb@bambooradical.com>
 */
public class DailyOverview {

    private final Map<String, Map> locationMap = new HashMap<>();
    private final Map<String, SortedSet<Float>> valuesList = new HashMap<>();

    public class DaySummaryData {

        public float[] avg = new float[0];
        public float[] max = new float[0];
        public float[] Q3 = new float[0];
        public float[] Q2 = new float[0
        public float[] Q1 = new float[0];
        public float[] min = new float[0];
    }

    public boolean hasDate(String dateKey) {
        String yearMonth = dateKey.substring(0, 7);
        int dayInt = Integer.parseInt(dateKey.substring(8, 10));
        for (Map<String, Map> channelMap : locationMap.values()) {
            for (Map<String, DaySummaryData> dateMap : channelMap.values()) {
                final DaySummaryData daySummaryData = dateMap.get(yearMonth);
                if (daySummaryData != null) {
//                    if (daySummaryData.average.length > dayInt) {
                    return daySummaryData.avg[dayInt] != 0;
//                    }
                }
            }
        }
        return false;
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
        String valuesListKey = locationKey + "_" + channelKey + "_" + dateKey;
        final SortedSet<Float> currentValueList;

        if (valuesList.containsKey(valuesListKey)) {
            currentValueList = valuesList.get(valuesListKey);
        } else {
            currentValueList = new TreeSet<>();
            valuesList.put(valuesListKey, currentValueList);
        }
        currentValueList.add(value);
    }

    public DaySummaryData getDaySummaryData(final String dateKey, final String locationKey, final String channelKey) {
        String yearMonth = dateKey.substring(0, 7);
        final Map<String, Map> channelMap = locationMap.get(locationKey);
        if (channelMap != null) {
            final Map<String, DaySummaryData> dateMap = channelMap.get(channelKey);
            if (dateMap != null) {
                final DaySummaryData daySummaryData = dateMap.get(yearMonth);
                if (daySummaryData != null) {
                    return daySummaryData;
                }
            }
        }
        return new DaySummaryData();
    }

    public void calculateSummaryData() {
        // todo: traverse the freshly added data and calculate the respective DaySummaryData
        for (String valuesListKey : valuesList.keySet()) {
            final String[] splitKey = valuesListKey.split("_");
            final String dateKey = splitKey[2];
            final String locationKey = splitKey[0];
            final String channelKey = splitKey[1];
            final Map<String, Map> channelMap;
            if (locationMap.containsKey(locationKey)) {
                channelMap = locationMap.get(locationKey);
            } else {
                channelMap = new HashMap<>();
                locationMap.put(locationKey, channelMap);
            }
            final Map<String, DaySummaryData> dateMap;
            if (channelMap.containsKey(channelKey)) {
                dateMap = channelMap.get(channelKey);
            } else {
                dateMap = new HashMap<>();
                channelMap.put(channelKey, dateMap);
            }
            String yearMonth = dateKey.substring(0, 7);
            int dayInt = Integer.parseInt(dateKey.substring(8, 10));
            final DaySummaryData daySummaryData;
            if (dateMap.containsKey(yearMonth)) {
                daySummaryData = dateMap.get(yearMonth);
            } else {
                daySummaryData = new DaySummaryData();
                dateMap.put(yearMonth, daySummaryData);
            }
            final SortedSet<Float> valuesSortedSet = valuesList.get(valuesListKey);
            double total = 0;
            int index = 0;
            int lowerIndex = (int) (valuesSortedSet.size() * 0.25);
            int middleIndex = (int) (valuesSortedSet.size() * 0.5);
            int upperIndex = (int) (valuesSortedSet.size() * 0.75);
            if (daySummaryData.avg.length < dayInt) {
                daySummaryData.avg = Arrays.copyOf(daySummaryData.avg, dayInt + 1);
                daySummaryData.min = Arrays.copyOf(daySummaryData.min, dayInt + 1);
                daySummaryData.Q1 = Arrays.copyOf(daySummaryData.Q1, dayInt + 1);
                daySummaryData.Q2 = Arrays.copyOf(daySummaryData.Q2, dayInt + 1);
                daySummaryData.Q3 = Arrays.copyOf(daySummaryData.Q3, dayInt + 1);
                daySummaryData.max = Arrays.copyOf(daySummaryData.max, dayInt + 1);
            }
            for (float value : valuesSortedSet) {
                total += value;
                if (index == lowerIndex) {
                    daySummaryData.Q1[dayInt] = value;
                }
                if (index == middleIndex) {
                    daySummaryData.Q2[dayInt] = value;
                }
                if (index == upperIndex) {
                    daySummaryData.Q3[dayInt] = value;
                }
                index++;
            }
            daySummaryData.avg[dayInt] = (float) (total / valuesSortedSet.size());
            daySummaryData.min[dayInt] = valuesSortedSet.first();
            daySummaryData.max[dayInt] = valuesSortedSet.last();
        }
    }

    @JsonAnyGetter
    public Map<String, Map> getLocationMap() {
        return locationMap;
    }
}

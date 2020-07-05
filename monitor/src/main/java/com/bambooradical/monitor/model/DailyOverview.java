/*
 * Copyright (C) 2019 Peter Withers
 */
package com.bambooradical.monitor.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @since Mar 26, 2019 22:20:38 PM (creation date)
 * @author : Peter Withers <peter-gthb@bambooradical.com>
 */
public class DailyOverview {

    private final Map<String, Map<String, Map<String, DaySummaryData>>> locationMap = new HashMap<>();
    private final Map<String, SortedSet<Float>> valuesList = new HashMap<>();

    public class DaySummaryData {

        public float[] avg = new float[0];
        public float[] max = new float[0];
        public float[] Q3 = new float[0];
        public float[] Q2 = new float[0];
        public float[] Q1 = new float[0];
        public float[] min = new float[0];
    }

    public boolean hasDate(String dateKey) {
        String yearMonth = dateKey.substring(0, 7);
        int dayInt = Integer.parseInt(dateKey.substring(8, 10));
        for (Map<String, Map<String, DaySummaryData>> channelMap : locationMap.values()) {
            for (Map<String, DaySummaryData> dateMap : channelMap.values()) {
                final DaySummaryData daySummaryData = dateMap.get(yearMonth);
                if (daySummaryData != null) {
                    if (daySummaryData.avg.length >= dayInt) {
                        return daySummaryData.avg[dayInt - 1] != 0;
                    } else {
                        return false;
                    }
                }
            }
        }
        return false;
    }

    public void addRecord(final String dateKey, final DataRecord dataRecord) {
        final String location = dataRecord.getLocation().toLowerCase().replaceAll("_", "-");
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
        final Map<String, Map<String, DaySummaryData>> channelMap = locationMap.get(locationKey);
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
            final Map<String, Map<String, DaySummaryData>> channelMap;
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
                daySummaryData.avg = Arrays.copyOf(daySummaryData.avg, dayInt);
                daySummaryData.min = Arrays.copyOf(daySummaryData.min, dayInt);
                daySummaryData.Q1 = Arrays.copyOf(daySummaryData.Q1, dayInt);
                daySummaryData.Q2 = Arrays.copyOf(daySummaryData.Q2, dayInt);
                daySummaryData.Q3 = Arrays.copyOf(daySummaryData.Q3, dayInt);
                daySummaryData.max = Arrays.copyOf(daySummaryData.max, dayInt);
            }
            for (float value : valuesSortedSet) {
                total += value;
                if (index == lowerIndex) {
                    daySummaryData.Q1[dayInt - 1] = value;
                }
                if (index == middleIndex) {
                    daySummaryData.Q2[dayInt - 1] = value;
                }
                if (index == upperIndex) {
                    daySummaryData.Q3[dayInt - 1] = value;
                }
                index++;
            }
            daySummaryData.avg[dayInt - 1] = (float) (total / valuesSortedSet.size());
            daySummaryData.min[dayInt - 1] = valuesSortedSet.first();
            daySummaryData.max[dayInt - 1] = valuesSortedSet.last();
        }
    }

    @JsonAnyGetter
    public Map<String, Map<String, Map<String, DaySummaryData>>> getLocationMap() {
        return locationMap;
    }

    private void mergeDaySummaryData(final String locationKey, final String channelKey, final String yearMonthKey, final LinkedHashMap<String, float[]> inputDaySummaryData) {
        final Map<String, Map<String, DaySummaryData>> channelMap;
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
        final DaySummaryData daySummaryData;
        if (dateMap.containsKey(yearMonthKey)) {
            daySummaryData = dateMap.get(yearMonthKey);
        } else {
            daySummaryData = new DaySummaryData();
            dateMap.put(yearMonthKey, daySummaryData);
        }
        final int length = inputDaySummaryData.get("avg").length;
        if (daySummaryData.avg.length < length) {
            daySummaryData.avg = Arrays.copyOf(daySummaryData.avg, length);
            daySummaryData.min = Arrays.copyOf(daySummaryData.min, length);
            daySummaryData.Q1 = Arrays.copyOf(daySummaryData.Q1, length);
            daySummaryData.Q2 = Arrays.copyOf(daySummaryData.Q2, length);
            daySummaryData.Q3 = Arrays.copyOf(daySummaryData.Q3, length);
            daySummaryData.max = Arrays.copyOf(daySummaryData.max, length);
        }
        for (int dayIndex = 0; dayIndex < length; dayIndex++) {
            if (inputDaySummaryData.get("avg")[dayIndex] != 0) {
                daySummaryData.avg[dayIndex] = inputDaySummaryData.get("avg")[dayIndex];
                daySummaryData.min[dayIndex] = inputDaySummaryData.get("min")[dayIndex];
                daySummaryData.Q1[dayIndex] = inputDaySummaryData.get("Q1")[dayIndex];
                daySummaryData.Q2[dayIndex] = inputDaySummaryData.get("Q2")[dayIndex];
                daySummaryData.Q3[dayIndex] = inputDaySummaryData.get("Q3")[dayIndex];
                daySummaryData.max[dayIndex] = inputDaySummaryData.get("max")[dayIndex];
            }
        }
    }

    public void addData(Map<String, Map<String, Map<String, LinkedHashMap<String, float[]>>>> inputMap) {
        for (String locationKey : inputMap.keySet()) {
            Map<String, Map<String, LinkedHashMap<String, float[]>>> channelMap = inputMap.get(locationKey);
            for (String channelKey : channelMap.keySet()) {
                Map<String, LinkedHashMap<String, float[]>> dateMap = channelMap.get(channelKey);
                for (String yearMonthKey : dateMap.keySet()) {
                    final LinkedHashMap<String, float[]> inputDaySummaryData = dateMap.get(yearMonthKey);
                    if (inputDaySummaryData != null) {
                        mergeDaySummaryData(locationKey, channelKey, yearMonthKey, inputDaySummaryData);
                    }
                }
            }
        }
    }
}

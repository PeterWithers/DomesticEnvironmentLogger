/*
 * Copyright (C) 2020 Peter Withers
 */
package com.bambooradical.monitor.repository;

import com.bambooradical.monitor.model.RadioData;
import com.google.cloud.Timestamp;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.FullEntity;
import com.google.cloud.datastore.IncompleteKey;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.StructuredQuery.PropertyFilter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * @since 02/01/2020 21:39 (creation date)
 * @author : Peter Withers <peter-gthb@bambooradical.com>
 */
@Service
public class RadioDataService {

    @Autowired
    Datastore datastore;

    private static HashMap<String,List<RadioData>> radioDataLocationMap = new HashMap<>();

    private KeyFactory keyFactory;

    @PostConstruct
    public void initializeKeyFactories() {
        keyFactory = datastore.newKeyFactory().setKind("RadioData");
    }

    private synchronized void addEntry(RadioData radioData){
        List<RadioData> records = radioDataLocationMap.get(radioData.getLocation());
        if (records != null) {
            // only append, if null then require a query to populate the full list first
            records.add(radioData);
        }
    }

    private synchronized void addEntry(String location, List<RadioData> records){
        radioDataLocationMap.put(location, records);
    }

    /*private synchronized void clearEntry(String location){
        radioDataLocationMap.remove(location);
    }*/

    public List<RadioData> findAll() {
        List<RadioData> resultList = new ArrayList<>();
        for (List<RadioData> records : radioDataLocationMap.values()) {
            resultList.addAll(records);
        }
/*        Query<Entity> query = Query.newEntityQueryBuilder()
                .setKind("RadioData")
                .build();
        QueryResults<Entity> results = datastore.run(query);
        while (results.hasNext()) {
            Entity currentEntity = results.next();
            resultList.add(new RadioData(
                    currentEntity.getString("Location"),
                    currentEntity.getDouble("DataValues"),
                    new Date(currentEntity.getTimestamp("RecordDate").getSeconds() * 1000L)));
        }
	*/
        return resultList;
    }

    public long count() {
        long resultCount = 0;
        /*Query<Entity> query = Query.newEntityQueryBuilder()
                .setKind("RadioData")
                .build();
        QueryResults<Entity> results = datastore.run(query);
        while (results.hasNext()) {
            results.next();
            resultCount++;
        }
	*/
        return resultCount;
    }

    public Entity save(RadioData radioData) {
        IncompleteKey key = keyFactory.setKind("RadioData").newKey();
        FullEntity entity = FullEntity.newBuilder(key)
                .set("Location", radioData.getLocation())
                .set("DataValues", radioData.getDataValues())
                .set("RecordDate", Timestamp.of(radioData.getRecordDate()))
                .build();
	    addEntry(radioData);
        return datastore.put(entity);
    }

    public void delete(RadioData radioData) {
        Key key = keyFactory.newKey(radioData.getId());
        datastore.delete(key);
    }

    public List<RadioData> findByLocationOrderByRecordDateAsc(String location, final Pageable pageable) {
	    List<RadioData> records = radioDataLocationMap.get(location);
	    if(records != null){
		    return records;
	    } else {
        // todo: handle Pageable values
        List<RadioData> resultList = new ArrayList<>();
        Query<Entity> query = Query.newEntityQueryBuilder()
                .setKind("RadioData")
                .setFilter(PropertyFilter.eq("Location", location))
                //.addOrderBy(StructuredQuery.OrderBy.asc("RecordDate"))
                .build();
        QueryResults<Entity> results = datastore.run(query);
        while (results.hasNext()) {
            Entity currentEntity = results.next();
            resultList.add(new RadioData(
                    currentEntity.getString("Location"),
                    currentEntity.getString("DataValues"),
                    new Date(currentEntity.getTimestamp("RecordDate").getSeconds() * 1000L)));
        }
        resultList.sort((RadioData o1, RadioData o2) -> o1.getRecordDate().compareTo(o2.getRecordDate()));
	addEntry(location, resultList);
        return resultList;
	    }
    }

    public List<RadioData> findByLocationAndRecordDate(String location, Date recordDate) {
        List<RadioData> resultList = new ArrayList<>();
	/*
        Query<Entity> query = Query.newEntityQueryBuilder()
                .setKind("RadioData")
                .setFilter(PropertyFilter.eq("Location", location))
                .setFilter(PropertyFilter.eq("RecordDate", Timestamp.of(recordDate)))
                .build();
        QueryResults<Entity> results = datastore.run(query);
        while (results.hasNext()) {
            Entity currentEntity = results.next();
            resultList.add(new radioData(
                    currentEntity.getString("Location"),
                    currentEntity.getDouble("DataValues"),
                    new Date(currentEntity.getTimestamp("RecordDate").getSeconds() * 1000L)));
        }
	*/
        return resultList;
    }
}

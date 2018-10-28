/*
 * Copyright (C) 2017 Peter Withers
 */
package com.bambooradical.monitor.repository;

import com.bambooradical.monitor.model.EnergyRecord;
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
 * @since Oct 14, 2017 1:31:49 PM (creation date)
 * @author : Peter Withers <peter@gthb-bambooradical.com>
 */
@Service
public class EnergyRecordService {

    @Autowired
    Datastore datastore;

    private static HashMap<String,List<EnergyRecord>> energyRecordLocationMap = new HashMap<>();

    private KeyFactory keyFactory;

    @PostConstruct
    public void initializeKeyFactories() {
        keyFactory = datastore.newKeyFactory().setKind("EnergyRecord");
    }

    private synchronized void addEntry(String meterLocation, List<EnergyRecord> records){
        energyRecordLocationMap.put(meterLocation, records);
    }

    private synchronized void clearEntry(String meterLocation){
        energyRecordLocationMap.remove(meterLocation);
    }

    public List<EnergyRecord> findAll() {
        List<EnergyRecord> resultList = new ArrayList<>();
/*        Query<Entity> query = Query.newEntityQueryBuilder()
                .setKind("EnergyRecord")
                .build();
        QueryResults<Entity> results = datastore.run(query);
        while (results.hasNext()) {
            Entity currentEntity = results.next();
            resultList.add(new EnergyRecord(
                    currentEntity.getString("MeterLocation"),
                    currentEntity.getDouble("MeterValue"),
                    new Date(currentEntity.getTimestamp("RecordDate").getSeconds() * 1000L)));
        }
	*/
        return resultList;
    }

    public long count() {
        long resultCount = 0;
        /*Query<Entity> query = Query.newEntityQueryBuilder()
                .setKind("EnergyRecord")
                .build();
        QueryResults<Entity> results = datastore.run(query);
        while (results.hasNext()) {
            results.next();
            resultCount++;
        }
	*/
        return resultCount;
    }

    public Entity save(EnergyRecord energyRecord) {
        IncompleteKey key = keyFactory.setKind("EnergyRecord").newKey();
        FullEntity entity = FullEntity.newBuilder(key)
                .set("MeterLocation", energyRecord.getMeterLocation())
                .set("MeterValue", energyRecord.getMeterValue())
                .set("RecordDate", Timestamp.of(energyRecord.getRecordDate()))
                .build();
	clearEntry(energyRecord.getMeterLocation());
        return datastore.put(entity);

    }

    public void delete(EnergyRecord energyRecord) {
        Key key = keyFactory.newKey(energyRecord.getId());
        datastore.delete(key);
    }

    public List<EnergyRecord> findByMeterLocationOrderByRecordDateAsc(String meterLocation, final Pageable pageable) {
	    List<EnergyRecord> records = energyRecordLocationMap.get(meterLocation);
	    if(records != null){
		    return records;
	    } else {
        // todo: handle Pageable values
        List<EnergyRecord> resultList = new ArrayList<>();
        Query<Entity> query = Query.newEntityQueryBuilder()
                .setKind("EnergyRecord")
                .setFilter(PropertyFilter.eq("MeterLocation", meterLocation))
                //.addOrderBy(StructuredQuery.OrderBy.asc("RecordDate"))
                .build();
        QueryResults<Entity> results = datastore.run(query);
        while (results.hasNext()) {
            Entity currentEntity = results.next();
            resultList.add(new EnergyRecord(
                    currentEntity.getString("MeterLocation"),
                    currentEntity.getDouble("MeterValue"),
                    new Date(currentEntity.getTimestamp("RecordDate").getSeconds() * 1000L)));
        }
        resultList.sort((EnergyRecord o1, EnergyRecord o2) -> {
            return o1.getRecordDate().compareTo(o2.getRecordDate());
        });
	addEntry(meterLocation, resultList);
        return resultList;
	    }
    }

    public List<EnergyRecord> findByMeterLocationAndRecordDate(String meterLocation, Date recordDate) {
        List<EnergyRecord> resultList = new ArrayList<>();
	/*
        Query<Entity> query = Query.newEntityQueryBuilder()
                .setKind("EnergyRecord")
                .setFilter(PropertyFilter.eq("MeterLocation", meterLocation))
                .setFilter(PropertyFilter.eq("RecordDate", Timestamp.of(recordDate)))
                .build();
        QueryResults<Entity> results = datastore.run(query);
        while (results.hasNext()) {
            Entity currentEntity = results.next();
            resultList.add(new EnergyRecord(
                    currentEntity.getString("MeterLocation"),
                    currentEntity.getDouble("MeterValue"),
                    new Date(currentEntity.getTimestamp("RecordDate").getSeconds() * 1000L)));
        }
	*/
        return resultList;
    }
}

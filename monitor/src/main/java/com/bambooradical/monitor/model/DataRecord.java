/*
 * Copyright (C) 2016 Peter Withers
 */
package com.bambooradical.monitor.model;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Temporal;
import org.springframework.data.annotation.Id;

/**
 * @created: 19/11/2016 22:11:12
 * @author : Peter Withers <peter@gthb-bambooradical.com>
 */
@Entity
public class DataRecord implements Serializable {

    @Id
    @GeneratedValue
    @javax.persistence.Id
    private Long id;
    private Float temperature;
    private Float humidity;
    private Float kpa;
    private Float tvoc;
    private Float dustAvg;
    private Float dustQ1;
    private Float dustQ2;
    private Float dustQ3;
    private Float dustOutliers;
    private Float co2;
    private Float voltage;
    private String location;
    private String error;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date recordDate;

    public DataRecord() {
    }

    public DataRecord(Float temperature, Float humidity, Float tvoc, Float co2, Float dustAvg, Float dustQ1, Float dustQ2, Float dustQ3, Float dustOutliers, Float kpa, Float voltage, String location, String error, Date recordDate) {
        this.temperature = temperature;
        this.humidity = humidity;
        this.tvoc = tvoc;
        this.dustAvg = dustAvg;
        this.dustQ1 = dustQ1;
        this.dustQ2 = dustQ2;
        this.dustQ3 = dustQ3;
        this.dustOutliers = dustOutliers;
        this.co2 = co2;
        this.kpa = kpa;
        this.voltage = voltage;
        this.location = location;
        this.error = error;
        this.recordDate = recordDate;
    }

    @Override
    public String toString() {
        return String.format("DataRecord[id=%d, temperature=%.2f, humidity=%.2f, tvoc=%f, co2=%f, dustAvg=%f, dustQ1=%.2f, dustQ2=%.2f, dustQ3=%.2f, dustOutliers=%0.2f, kpa=%f, voltage=%.2f, location='%s', error='%s', date='%s']", id,
                temperature, humidity, tvoc, co2, dustAvg, dustQ1, dustQ2, dustQ3, dustOutliers, kpa, voltage, location, error, recordDate);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Float getTemperature() {
        return temperature;
    }

    public void setTemperature(Float temperature) {
        this.temperature = temperature;
    }

    public Float getHumidity() {
        return humidity;
    }

    public void setHumidity(Float humidity) {
        this.humidity = humidity;
    }

    public Float getKPa() {
        return kpa;
    }

    public void setKPa(Float kpa) {
        this.kpa = kpa;
    }

    public Float getTvoc() {
        return tvoc;
    }

    public void setTvoc(Float tvoc) {
        this.tvoc = tvoc;
    }

    public Float getCo2() {
        return co2;
    }

    public void setCo2(Float co2) {
        this.co2 = co2;
    }

    public Float getDustAvg() {
        return dustAvg;
    }

    public Float getDustQ1() {
        return dustQ1;
    }

    public Float getDustQ2() {
        return dustQ2;
    }

    public Float getDustQ3() {
        return dustQ3;
    }

    public Float getDustOutliers() {
        return dustOutliers;
    }

    public Float getVoltage() {
        return voltage;
    }

    public void setVoltage(Float voltage) {
        this.voltage = voltage;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Date getRecordDate() {
        return recordDate;
    }

    public void setRecordDate(Date recordDate) {
        this.recordDate = recordDate;
    }
}

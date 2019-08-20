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
    private Integer pa;
    private Integer tvoc;
    private Integer co2;
    private Float voltage;
    private String location;
    private String error;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date recordDate;

    public DataRecord() {
    }

    public DataRecord(Float temperature, Float humidity, Integer tvoc, Integer co2, Integer pa, Float voltage, String location, String error, Date recordDate) {
        this.temperature = temperature;
        this.humidity = humidity;
	this.tvoc = tvoc;
        this.co2 = co2;
        this.pa = pa;
        this.voltage = voltage;
        this.location = location;
        this.error = error;
        this.recordDate = recordDate;
    }

    @Override
    public String toString() {
	return String.format("DataRecord[id=%d, temperature=%.2f, humidity=%.2f, tvoc=%d, co2=%d, pa=%d, voltage=%.2f, location='%s', error='%s', date='%s']", id,
                temperature, humidity, tvoc, co2, pa, voltage, location, error, recordDate);
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

    public Integer getPa() {
        return pa;
    }

    public void setPa(Integer pa) {
        this.pa = pa;
    }

    public Integer getTvoc() {
        return tvoc;
    }

    public void setTvoc(Integer tvoc) {
        this.tvoc = tvoc;
    }

    public Integer getCo2() {
        return co2;
    }

    public void setCo2(Integer co2) {
        this.co2 = co2;
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

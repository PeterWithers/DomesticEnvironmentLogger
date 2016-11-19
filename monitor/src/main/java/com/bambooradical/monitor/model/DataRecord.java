/*
 * Copyright (C) 2016 Peter Withers
 */
package com.bambooradical.monitor.model;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
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
    private Float voltage;
    private String location;
    private String error;

    public DataRecord() {
    }

    public DataRecord(Float temperature, Float humidity, Float voltage, String location, String error) {
        this.temperature = temperature;
        this.humidity = humidity;
        this.voltage = voltage;
        this.location = location;
        this.error = error;
    }

    @Override
    public String toString() {
        return String.format("Customer[id=%d, temperature=%.2f, humidity=%.2f,  voltage=%.2f,  location='%s',  error='%s']", id,
                temperature, humidity, voltage, location, error);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}

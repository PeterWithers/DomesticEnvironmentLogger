/*
 * Copyright (C) 2016 Peter Withers
 */
package com.bambooradical.monitor.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.util.Date;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Temporal;
import org.springframework.data.annotation.Id;

/**
 * @created: 5/12/2016 22:11:12
 * @author : Peter Withers <peter@gthb-bambooradical.com>
 */
@Entity
public class EnergyRecord implements Serializable {

    @Id
    @GeneratedValue
    @jakarta.persistence.Id
    private Long id;
    private String meterLocation;
    private double meterValue;
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Temporal(jakarta.persistence.TemporalType.TIMESTAMP)
    private Date recordDate;

    public EnergyRecord() {
    }

    public EnergyRecord(String meterLocation, double meterValue, Date recordDate) {
        this.meterLocation = meterLocation;
        this.meterValue = meterValue;
        this.recordDate = recordDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMeterLocation() {
        return meterLocation;
    }

    public void setMeterLocation(String meterLocation) {
        this.meterLocation = meterLocation;
    }

    public double getMeterValue() {
        return meterValue;
    }

    public void setMeterValue(double meterValue) {
        this.meterValue = meterValue;
    }

    public Date getRecordDate() {
        return recordDate;
    }

    public void setRecordDate(Date recordDate) {
        this.recordDate = recordDate;
    }
}

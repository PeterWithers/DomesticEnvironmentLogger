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
 * @created: 5/12/2016 22:11:12
 * @author : Peter Withers <peter@gthb-bambooradical.com>
 */
@Entity
public class EnergyRecord implements Serializable {

    @Id
    @GeneratedValue
    @javax.persistence.Id
    private Long id;
    private Float meterValue;
    private String meterNumber;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date recordDate;

    public EnergyRecord() {
    }

    public EnergyRecord(Float meterValue, String meterNumber, Date recordDate) {
        this.meterValue = meterValue;
        this.meterNumber = meterNumber;
        this.recordDate = recordDate;
    }

    @Override
    public String toString() {
        return String.format("Customer[id=%d, meterValue=%.2f, meterNumber='%s',  date='%s']", id,
                meterValue, meterNumber, recordDate);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Float getMeterValue() {
        return meterValue;
    }

    public void setMeterValue(Float meterValue) {
        this.meterValue = meterValue;
    }

    public String getMeterNumber() {
        return meterNumber;
    }

    public void setMeterNumber(String meterNumber) {
        this.meterNumber = meterNumber;
    }

    public Date getRecordDate() {
        return recordDate;
    }

    public void setRecordDate(Date recordDate) {
        this.recordDate = recordDate;
    }
}

/*
 * Copyright (C) 2020 Peter Withers
 */
package com.bambooradical.monitor.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Temporal;
import org.springframework.data.annotation.Id;

/**
 * @created: 02/01/2020 21:30
 * @author : Peter Withers <peter-gthb@bambooradical.com>
 */
@Entity
public class RadioData implements Serializable {

    @Id
    @GeneratedValue
    @javax.persistence.Id
    private Long id;
    private String location;
    private int[] dataValues;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date recordDate;

    public EnergyRecord() {
    }

    public EnergyRecord(String location, int[] dataValues, Date recordDate) {
        this.location = location;
        this.dataValues = dataValues;
        this.recordDate = recordDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int[] getDataValues() {
        return dataValues;
    }

    public void setDataValues(int[] dataValues) {
        this.dataValues = dataValues;
    }

    public Date getRecordDate() {
        return recordDate;
    }

    public void setRecordDate(Date recordDate) {
        this.recordDate = recordDate;
    }
}

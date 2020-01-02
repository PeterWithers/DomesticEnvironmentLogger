/*
 * Copyright (C) 2020 Peter Withers
 */
package com.bambooradical.monitor.repository;

import com.bambooradical.monitor.model.RadioData;
import java.util.Date;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @created: 02/01/2020 21:38
 * @author : Peter Withers <peter-gthb@bambooradical.com>
 */
public interface RadioDataRepository extends JpaRepository<RadioData, Long> {

    List<RadioData> findByLocationOrderByRecordDateAsc(String location, final Pageable pageable);

    List<RadioData> findByLocationAndRecordDate(String location, Date recordDate);
}

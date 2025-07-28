/*
 * Copyright (C) 2016 Peter Withers
 */
package com.bambooradical.monitor;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;

/**
 * @created: 19/11/2016 22:11:12
 * @author : Peter Withers <peter@gthb-bambooradical.com>
 */

@SpringBootApplication
public class Application extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(Application.class);
    }

    @Bean
    public Datastore cloudDatastoreService() {
        return DatastoreOptions.getDefaultInstance().getService();
    }
}

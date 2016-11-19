/*
 * Copyright (C) 2016 Peter Withers
 */

/*
 * ESP-POST.cpp
 *
 * Created: 19/11/2016 12:20:32
 * Author : Peter Withers <peter@gthb-bambooradical.com>
 */

#include <ESP8266WiFi.h>
#include <Adafruit_Sensor.h>
#include <DHT.h>
#include <DHT_U.h>

const char* ssid = "";
const char* password = "";

#define DHTPIN        4
#define DHTTYPE       DHT22

DHT_Unified dht(DHTPIN, DHTTYPE);

void sendMonitoredData() {
    sensors_event_t event;
    dht.temperature().getEvent(&event);
    String telemetryString = "";
    if (isnan(event.temperature)) {
        telemetryString += "Error reading temperature<br/>";
    } else {
        telemetryString += "Temperature: ";
        telemetryString += event.temperature;
        telemetryString += " *C<br/>";
    }
    dht.humidity().getEvent(&event);
    if (isnan(event.relative_humidity)) {
        telemetryString += "Error reading humidity<br/>";
    } else {
        telemetryString += "Humidity: ";
        telemetryString += event.relative_humidity;
        telemetryString += "%<br/>";
    }
    Serial.print(telemetryString);
}

void setup() {
    Serial.begin(115200);
    delay(10);
    WiFi.begin(ssid, password);
    while (WiFi.status() != WL_CONNECTED) {
        delay(500);
        Serial.println(".");
    }
    Serial.println("Connected: ");
    Serial.println(ssid);    
    dht.begin();
}

void loop() {
    Serial.println("IP Address");
    Serial.println(WiFi.localIP());
    sendMonitoredData();
    delay(30000);
}
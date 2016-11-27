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
const char* reportingServer = "";
const int httpPort = 80;
String locationString = "third%20testing%20board";

#define DHTPIN        4
#define DHTTYPE       DHT22

ADC_MODE(ADC_VCC);

DHT_Unified dht(DHTPIN, DHTTYPE);

void sendMonitoredData() {
    String errorString = "";
    Serial.println(reportingServer);
    String url = "/monitor/add?temperature=";
    sensors_event_t event;
    dht.temperature().getEvent(&event);
    String telemetryString = "";
    if (isnan(event.temperature)) {
        telemetryString += "Error reading temperature<br/>";
        errorString += "Error%20reading%20temperature. ";
    } else {
        telemetryString += "Temperature: ";
        telemetryString += event.temperature;
        telemetryString += " *C<br/>";
        url+=event.temperature;
    }
    dht.humidity().getEvent(&event);
    url+="&humidity=";
    if (isnan(event.relative_humidity)) {
        telemetryString += "Error reading humidity<br/>";
        errorString += "Error%20reading%20humidity. ";
    } else {
        telemetryString += "Humidity: ";
        telemetryString += event.relative_humidity;
        telemetryString += "%<br/>";
        url+=event.relative_humidity;
    }
    telemetryString += "ADC: ";
    telemetryString += analogRead(A0);
    telemetryString += "<br/>";
    telemetryString += "voltage: ";
    telemetryString += (analogRead(A0) / 69.0);
    telemetryString += "v";
    url+="&voltage=";
    //url += (analogRead(A0) / 69.0);
    url += (ESP.getVcc() / 1000.0);

    Serial.println(telemetryString);
    WiFiClient client;
    if (!client.connect(reportingServer, httpPort)) {
        Serial.println("connection failed");
        return;
    }
    url+="&location=";
    url+=locationString;
    url+="&error=";
    url+= errorString;
    Serial.println(url);

    String connectionString = "GET ";
    connectionString += url;
    connectionString += " HTTP/1.1\r\n";
    connectionString += "Host: ";
    connectionString += reportingServer;
    connectionString +="\r\n";
    connectionString +="Connection: close\r\n\r\n";
    Serial.println(connectionString);
    client.print(connectionString);
    unsigned long timeout = millis();
    while (client.available() == 0) {
        if (millis() - timeout > 5000) {
            Serial.println("timeout");
            client.stop();
            return;
        }
    }

    while(client.available()){
        String line = client.readStringUntil('\r');
        Serial.print(line);
    }
}

void setup() {
    Serial.begin(115200);
    delay(10);
    WiFi.mode(WIFI_STA);
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
    delay(20 * 60 * 1000);
}

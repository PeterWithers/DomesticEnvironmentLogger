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
#include <WiFiClientSecure.h>
#include <Adafruit_Sensor.h>
#include <DHT.h>
#include <DHT_U.h>

const char* ssid = "";
const char* password = "";
const char* reportingServer = "";
const char* messageServer = "";
const char* messageServerPath = "";
const char* buttonMessage0 = "on%20board%20button";
const char* buttonMessage1 = "external%20button%201";
const char* buttonMessage2 = "external%20button%202";
const char* startMessage = "restarted";
const int httpPort = 80;
const int httpsPort = 443;

const unsigned long dataSendDelayMs = 20 * 60 * 1000;
const unsigned long buttonDataSendDelayMs = 30 * 1000;
volatile unsigned long lastDataSentMs = -dataSendDelayMs; // set to a value that will trigger the data send on start up
volatile unsigned long onBoardButtonDataSentMs = 0;
volatile bool onBoardButtonChanged = false;

/*
String locationString = "testing%20board";
*/

String locationString = "third%20testing%20messaging";
#define VCC_VOLTAGE_MONITOR 
#define BUTTON_MESSAGE

/*
String locationString = "second%20testing%20board";
#define POWER_DHT_VIA_GPIO
#define VCC_VOLTAGE_MONITOR
*/

#define DHTPIN        4
#define DHTPOWERPIN   5
#define BUTTONPIN   5
#define ON_BOARD_BUTTON   0
#define DHTTYPE       DHT22

#ifdef VCC_VOLTAGE_MONITOR
    ADC_MODE(ADC_VCC);
#endif

DHT_Unified dht(DHTPIN, DHTTYPE);

void sendMessage(String messageString) {
    WiFiClientSecure client;
    if (!client.connect(messageServer, httpsPort)) {
        Serial.println("connection failed");
        return;
    }
    String connectionString = "GET ";
    connectionString += messageServerPath;
    connectionString += "&text=";
    connectionString += messageString;
    connectionString += "&username=";
    connectionString += locationString;
    connectionString += " HTTP/1.1\r\n";
    connectionString += "Host: ";
    connectionString += messageServer;
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
    #ifdef VCC_VOLTAGE_MONITOR
        // battery 3.83v = "voltage":2.61
        url += (ESP.getVcc() / 1000.0);
    #else
        url += (analogRead(A0) / 69.0);
    #endif

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

void onBoardButtonChangeInterrupt() {
    onBoardButtonChanged = true;
}

void setup() {
    Serial.begin(115200);
    delay(10);
    #ifdef POWER_DHT_VIA_GPIO
        pinMode(DHTPOWERPIN, OUTPUT);
        digitalWrite(DHTPOWERPIN, 1);
    #endif
    #ifdef BUTTON_MESSAGE
        pinMode(BUTTONPIN, INPUT);
        pinMode(ON_BOARD_BUTTON, INPUT);
        attachInterrupt(ON_BOARD_BUTTON, onBoardButtonChangeInterrupt, CHANGE);
    #endif
    WiFi.mode(WIFI_STA);
    WiFi.begin(ssid, password);
    while (WiFi.status() != WL_CONNECTED) {
        delay(500);
        Serial.println(".");
    }
    Serial.println("Connected: ");
    Serial.println(ssid);
    dht.begin();
    sendMessage(startMessage);
}

void loop() {
    if (lastDataSentMs + dataSendDelayMs < millis()) {
        Serial.println("IP Address");
        Serial.println(WiFi.localIP());
        sendMonitoredData();
        lastDataSentMs = millis();
    }
    if (onBoardButtonChanged) {
        Serial.println("onBoardButtonChanged");
        if (onBoardButtonDataSentMs + buttonDataSendDelayMs < millis()) {
            onBoardButtonDataSentMs = millis();
            Serial.println("sending button message");
            sendMessage(buttonMessage0);
        }
        onBoardButtonChanged = false;
    }
    delay(1000);
    Serial.print(".");
}

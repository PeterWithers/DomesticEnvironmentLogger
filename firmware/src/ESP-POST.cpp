/*
 * Copyright (C) 2016 Peter Withers
 */

/*
 * ESP-POST.cpp
 *
 * Created: 19/11/2016 12:20:32
 * Author : Peter Withers <peter-gthb@bambooradical.com>
 */

#include <ESP8266WiFi.h>
#include <WiFiClientSecure.h>
#include <WiFiUdp.h>
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

// UDP is used to broadcast button events
WiFiUDP udp;
unsigned int localUdpPort = 2233;

const unsigned long dataSendDelayMs = 20 * 60 * 1000;
const unsigned long buttonDataSendDelayMs = 30 * 1000;
volatile unsigned long lastDataSentMs = -dataSendDelayMs; // set to a value that will trigger the data send on start up
volatile unsigned long onBoardButtonDataSentMs = 0;
volatile unsigned long externalButton1DataSentMs = 0;
volatile unsigned long externalButton2DataSentMs = 0;
volatile int requestRGBButtonChanged = 0;
volatile int onBoardButtonChanged = 0;
volatile int externalButton1Changed = 0;
volatile int externalButton2Changed = 0;

/*
String locationString = "testing%20first%20floor";
#define DHTPIN              4
#define BUTTONPIN           14
 */

/*
String locationString = "third%20ground%20floor";
#define VCC_VOLTAGE_MONITOR
//#define POWER_DHT_VIA_GPIO
#define DHTPIN              4
#define BUTTONPIN           14
 */

/*
String locationString = "second%20top%20floor";
#define VCC_VOLTAGE_MONITOR
#define POWER_DHT_VIA_GPIO
#define DHTPOWERPIN         5
#define DHTPIN              4
#define BUTTONPIN           14
 */

/*
String locationString = "aquarium";
#define VCC_VOLTAGE_MONITOR
#define DHTPIN              2
#define BUTTONPIN           5
#define GREEN_LED_PIN       13
#define RED_LED_PIN         12
#define BLUE_LED_PIN        14
#define DS18b20_PIN         0
 */

/*
String locationString = "second%20testing%20board";
#define POWER_DHT_VIA_GPIO
#define VCC_VOLTAGE_MONITOR
#define DHTPOWERPIN         5
 */

//#define DHTPIN              4 // top floor = 4 // aquarium = 2
//#define BUTTONPIN           5
#define ON_BOARD_BUTTON     0
#define EXTERNAL_BUTTON1    1
#define EXTERNAL_BUTTON2    3
#define DHTTYPE             DHT22

#ifdef VCC_VOLTAGE_MONITOR
ADC_MODE(ADC_VCC);
#endif

DHT_Unified dht(DHTPIN, DHTTYPE);

struct SegmentRGB {
    int redValue;
    int greenValue;
    int blueValue;
    long duration;
    bool tween;
};
#define SEGMENTSIZE 100
SegmentRGB segmentRGB[SEGMENTSIZE];
long segmentMillisOffset = 0;
int segmentPreviousIndex = 0;
int segmentMessageIndex = -1;

void sendMessage(String messageString) {
    WiFiClientSecure client;
    if (!client.connect(messageServer, httpsPort)) {
        //Serial.println("connection failed");
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
    connectionString += "\r\n";
    connectionString += "Connection: close\r\n\r\n";
    //Serial.println(connectionString);
    client.print(connectionString);
    unsigned long timeout = millis();
    while (client.available() == 0) {
        if (millis() - timeout > 5000) {
            //Serial.println("timeout");
            client.stop();
            return;
        }
    }
    while (client.available()) {
        String line = client.readStringUntil('\r');
        //Serial.print(line);
    }
}

void requestRGB(String locationString) {
    WiFiClient client;
    if (!client.connect(reportingServer, httpPort)) {
        sendMessage("connection%20failed%20requestRGB");
        return;
    }
    String connectionString = "GET ";
    connectionString += "/monitor/currentRGB?location=";
    connectionString += locationString;
    connectionString += " HTTP/1.1\r\n";
    connectionString += "Host: ";
    connectionString += reportingServer;
    connectionString += "\r\n";
    connectionString += "Connection: close\r\n\r\n";
    //Serial.println(connectionString);
    client.print(connectionString);
    unsigned long timeout = millis();
    while (client.available() == 0) {
        if (millis() - timeout > 5000) {
            client.stop();
#ifdef GREEN_LED_PIN
            analogWrite(RED_LED_PIN, 100);
            analogWrite(GREEN_LED_PIN, 0);
            analogWrite(BLUE_LED_PIN, 0);
#endif
            sendMessage("timeout%20currentRGB%20" + locationString);
            return;
        }
    }
    long requestMillisOffset = millis();
    String parsedValues = "";
    //String receivedValues = "";
    int segmentIndex = 0;
    while (client.connected()) {
        if (client.available()) {
            String line = client.readStringUntil('\r');
            if (line[15] == ';') {
                if (line[7] == ':' || line[7] == 'T') {
                    for (int substringIndex = 0; substringIndex < line.length() && segmentIndex < SEGMENTSIZE; substringIndex += 15) {
                        if (line[substringIndex + 15] == ';') {
                            if (line[substringIndex + 7] == ':' || line[substringIndex + 7] == 'T') {
                                String redString = line.substring(substringIndex + 1, substringIndex + 3);
                                String greenString = line.substring(substringIndex + 3, substringIndex + 5);
                                String blueString = line.substring(substringIndex + 5, substringIndex + 7);
                                String delayString = line.substring(substringIndex + 8, substringIndex + 15);
                                //sendMessage(redString + "-" + greenString + "-" + blueString + "-" + delayString);
                                parsedValues = parsedValues + segmentIndex + "_" + redString + "-" + greenString + "-" + blueString + "_" + delayString + "%0A";
                                int redValue = (int) strtol(redString.c_str(), NULL, 16);
                                int greenValue = (int) strtol(greenString.c_str(), NULL, 16);
                                int blueValue = (int) strtol(blueString.c_str(), NULL, 16);
                                long delayValue = strtol(delayString.c_str(), NULL, 32);
                                segmentRGB[segmentIndex].duration = delayValue;
                                segmentRGB[segmentIndex].redValue = redValue;
                                segmentRGB[segmentIndex].greenValue = greenValue;
                                segmentRGB[segmentIndex].blueValue = blueValue;
                                segmentRGB[segmentIndex].tween = line[substringIndex + 7] == 'T';
                                segmentIndex++;
                            }
                        }
                    }
                }
            }
        }
    }
    if (segmentIndex > 0) {
        if (segmentIndex < SEGMENTSIZE) {
            segmentRGB[segmentIndex].duration = -1;
        }
        segmentPreviousIndex = segmentIndex - 1;
        segmentMillisOffset = requestMillisOffset;
        sendMessage(parsedValues);
    } else {
        sendMessage("no parsedValues");
    }
}

void sendMonitoredData() {
    String errorString = "";
    //Serial.println(reportingServer);
    String url = "/monitor/add?temperature=";
    sensors_event_t event;
    dht.temperature().getEvent(&event);
    String telemetryString = "";
    if (isnan(event.temperature)) {
        telemetryString += "Error reading temperature<br/>";
        errorString += "Error%20reading%20temperature. ";
        sendMessage(errorString);
    } else {
        telemetryString += "Temperature: ";
        telemetryString += event.temperature;
        telemetryString += " *C<br/>";
        url += event.temperature;
    }
    dht.humidity().getEvent(&event);
    url += "&humidity=";
    if (isnan(event.relative_humidity)) {
        telemetryString += "Error reading humidity<br/>";
        errorString += "Error%20reading%20humidity. ";
        sendMessage(errorString);
    } else {
        telemetryString += "Humidity: ";
        telemetryString += event.relative_humidity;
        telemetryString += "%<br/>";
        url += event.relative_humidity;
    }
    telemetryString += "ADC: ";
    telemetryString += analogRead(A0);
    telemetryString += "<br/>";
    telemetryString += "voltage: ";
    telemetryString += (analogRead(A0) / 69.0);
    telemetryString += "v";
    url += "&voltage=";
#ifdef VCC_VOLTAGE_MONITOR
    // battery 3.83v = "voltage":2.61
    url += (ESP.getVcc() / 1000.0);
#else
    url += (analogRead(A0) / 69.0);
#endif

    //Serial.println(telemetryString);
    WiFiClient client;
    if (!client.connect(reportingServer, httpPort)) {
        //Serial.println("connection failed");
        sendMessage("connection%20failed%20add%20" + locationString);
        return;
    }
    url += "&location=";
    url += locationString;
    url += "&error=";
    url += errorString;
    //Serial.println(url);

    String connectionString = "GET ";
    connectionString += url;
    connectionString += " HTTP/1.1\r\n";
    connectionString += "Host: ";
    connectionString += reportingServer;
    connectionString += "\r\n";
    connectionString += "Connection: close\r\n\r\n";
    //Serial.println(connectionString);
    client.print(connectionString);
    unsigned long timeout = millis();
    while (client.available() == 0) {
        if (millis() - timeout > 5000) {
            //Serial.println("timeout");
            client.stop();
            sendMessage("timeout%20add%20" + locationString);
            return;
        }
    }

    while (client.available()) {
        String line = client.readStringUntil('\r');
        //Serial.print(line);
    }
}

void requestRGBInterrupt() {
    requestRGBButtonChanged++;
}

void onBoardButtonChangeInterrupt() {
    onBoardButtonChanged++;
}

void externalButton1ChangeInterrupt() {
    externalButton2Changed++;
}

void externalButton2ChangeInterrupt() {
    externalButton2Changed++;
}

void setup() {
#ifndef BUTTON_MESSAGE
    //Serial.begin(115200);
#endif
    delay(10);
#ifdef POWER_DHT_VIA_GPIO
    pinMode(DHTPOWERPIN, OUTPUT);
    digitalWrite(DHTPOWERPIN, 1);
#endif
    pinMode(BUTTONPIN, INPUT); // todo: should this use a pull up also
#ifdef BUTTON_MESSAGE
    pinMode(ON_BOARD_BUTTON, INPUT_PULLUP);
    //digitalWrite(ON_BOARD_BUTTON, 1);
    attachInterrupt(ON_BOARD_BUTTON, onBoardButtonChangeInterrupt, CHANGE);
    pinMode(EXTERNAL_BUTTON1, INPUT_PULLUP);
    //digitalWrite(EXTERNAL_BUTTON1, 1);
    attachInterrupt(EXTERNAL_BUTTON1, externalButton1ChangeInterrupt, CHANGE);
    pinMode(EXTERNAL_BUTTON2, INPUT_PULLUP);
    //digitalWrite(EXTERNAL_BUTTON2, 1);
    attachInterrupt(EXTERNAL_BUTTON2, externalButton2ChangeInterrupt, CHANGE);
#endif
#ifdef GREEN_LED_PIN
    pinMode(RED_LED_PIN, OUTPUT);
    analogWrite(RED_LED_PIN, 0);
    pinMode(GREEN_LED_PIN, OUTPUT);
    analogWrite(GREEN_LED_PIN, 0);
    pinMode(BLUE_LED_PIN, OUTPUT);
    analogWrite(BLUE_LED_PIN, 0);

    pinMode(ON_BOARD_BUTTON, INPUT_PULLUP);
    attachInterrupt(ON_BOARD_BUTTON, requestRGBInterrupt, CHANGE);
#endif
    WiFi.mode(WIFI_STA);
    WiFi.begin(ssid, password);
    while (WiFi.status() != WL_CONNECTED) {
        delay(500);
        //Serial.println(".");
    }
    //Serial.println("Connected: ");
    //Serial.println(ssid);
    dht.begin();
    sendMessage(startMessage);
    segmentRGB[0].blueValue = 0;
    segmentRGB[0].redValue = 0;
    segmentRGB[0].greenValue = 0;
    segmentRGB[0].duration = 1000;
    segmentRGB[0].tween = false;
    segmentRGB[1].blueValue = 255;
    segmentRGB[1].redValue = 0;
    segmentRGB[1].greenValue = 0;
    segmentRGB[1].duration = 2000;
    segmentRGB[1].tween = false;
    segmentRGB[2].blueValue = 0;
    segmentRGB[2].redValue = 255;
    segmentRGB[2].greenValue = 0;
    segmentRGB[2].duration = 3000;
    segmentRGB[2].tween = false;
    segmentRGB[3].blueValue = 0;
    segmentRGB[3].redValue = 0;
    segmentRGB[3].greenValue = 255;
    segmentRGB[3].duration = 4000;
    segmentRGB[3].tween = false;
    segmentRGB[4].blueValue = 0;
    segmentRGB[4].redValue = 255;
    segmentRGB[4].greenValue = 255;
    segmentRGB[4].duration = 5000;
    segmentRGB[4].tween = true;
    segmentRGB[5].blueValue = 255;
    segmentRGB[5].redValue = 255;
    segmentRGB[5].greenValue = 0;
    segmentRGB[5].duration = 6000;
    segmentRGB[5].tween = false;
    segmentRGB[6].blueValue = 255;
    segmentRGB[6].redValue = 255;
    segmentRGB[6].greenValue = 255;
    segmentRGB[6].duration = 7000;
    segmentRGB[6].tween = false;
    segmentRGB[7].blueValue = 0;
    segmentRGB[7].redValue = 0;
    segmentRGB[7].greenValue = 0;
    segmentRGB[7].duration = -1;
    segmentRGB[7].tween = false;
}

void loop() {
    if (lastDataSentMs + dataSendDelayMs < millis()) {
        //Serial.println("IP Address");
        //Serial.println(WiFi.localIP());
        sendMonitoredData();
#ifdef GREEN_LED_PIN
        requestRGB(locationString);
#endif
        lastDataSentMs = millis();
    }
    if (onBoardButtonChanged > 0 || externalButton1Changed > 0 || externalButton2Changed > 0) {
        IPAddress broadcastIp;
        broadcastIp = ~WiFi.subnetMask() | WiFi.gatewayIP();
        udp.beginPacketMulticast(broadcastIp, localUdpPort, WiFi.localIP());
        if (onBoardButtonChanged > 0) udp.write(buttonMessage0);
        if (externalButton1Changed > 0) udp.write(buttonMessage1);
        if (externalButton2Changed > 0) udp.write(buttonMessage1);
        udp.write("\a\n");
        udp.endPacket();
        udp.flush();
        udp.stop(); // flush and stop added to address issues of the ESP hanging after send
        //Serial.print("onBoardButtonChanged: ");
        //Serial.println(onBoardButtonChanged);
        //Serial.print(" externalButton1Changed: ");
        //Serial.println(externalButton1Changed);
        //Serial.print(" externalButton2Changed: ");
        //Serial.println(externalButton2Changed);
        if (onBoardButtonChanged && onBoardButtonDataSentMs + buttonDataSendDelayMs < millis()) {
            onBoardButtonDataSentMs = millis();
            //Serial.println("sending button0 message");
            sendMessage(buttonMessage0);
        }
        if (externalButton1Changed && externalButton1DataSentMs + buttonDataSendDelayMs < millis()) {
            externalButton1DataSentMs = millis();
            //Serial.println("sending button1 message");
            sendMessage(buttonMessage1);
        }
        if (externalButton2Changed && externalButton2DataSentMs + buttonDataSendDelayMs < millis()) {
            externalButton2DataSentMs = millis();
            //Serial.println("sending button2 message");
            sendMessage(buttonMessage2);
        }
        onBoardButtonChanged = 0;
        externalButton1Changed = 0;
        externalButton2Changed = 0;
    }
    if (requestRGBButtonChanged > 0) {
        sendMessage("requestRGBButton");
        requestRGB(locationString);
        requestRGBButtonChanged = 0;
    }
#ifdef GREEN_LED_PIN
    long segmentDuration = millis() - segmentMillisOffset;
    for (int segmentIndex = 0; segmentIndex < SEGMENTSIZE; segmentIndex++) {
        if (segmentRGB[segmentIndex].duration == -1) {
            segmentPreviousIndex = segmentIndex - 1;
            segmentMillisOffset = millis();
            break;
        } else if (segmentRGB[segmentIndex].duration > segmentDuration) {
            if (segmentRGB[segmentIndex].tween) {
                int lastRed = segmentRGB[segmentPreviousIndex].redValue;
                int lastGreen = segmentRGB[segmentPreviousIndex].greenValue;
                int lastBlue = segmentRGB[segmentPreviousIndex].blueValue;
                int lastDuration = segmentRGB[segmentPreviousIndex].duration;
                float durationDifference = segmentRGB[segmentIndex].duration - lastDuration;
                if (durationDifference < 0) {
                    durationDifference = segmentRGB[segmentIndex].duration;
                    lastDuration = 0;
                }
                float durationPortion = segmentDuration - lastDuration;
                if (durationDifference > 0) {
                    // this method of gradients is overly simplistic and does not consider colour space rotation, but it might be adequate
                    int tweenedRed = (int) ((segmentRGB[segmentIndex].redValue - lastRed) / durationDifference * durationPortion) + lastRed;
                    int tweenedGreen = (int) ((segmentRGB[segmentIndex].greenValue - lastGreen) / durationDifference * durationPortion) + lastGreen;
                    int tweenedBlue = (int) ((segmentRGB[segmentIndex].blueValue - lastBlue) / durationDifference * durationPortion) + lastBlue;
                    analogWrite(RED_LED_PIN, tweenedRed);
                    analogWrite(GREEN_LED_PIN, tweenedGreen);
                    analogWrite(BLUE_LED_PIN, tweenedBlue);
                }
            } else {
                analogWrite(RED_LED_PIN, segmentRGB[segmentPreviousIndex].redValue);
                analogWrite(GREEN_LED_PIN, segmentRGB[segmentPreviousIndex].greenValue);
                analogWrite(BLUE_LED_PIN, segmentRGB[segmentPreviousIndex].blueValue);
                if (segmentPreviousIndex != segmentMessageIndex) {
                    sendMessage(String("I") + segmentPreviousIndex + "%20R" + String(segmentRGB[segmentPreviousIndex].redValue, HEX) + "%20G" + String(segmentRGB[segmentPreviousIndex].greenValue, HEX) + "%20B" + String(segmentRGB[segmentPreviousIndex].blueValue, HEX));
                    segmentMessageIndex = segmentPreviousIndex;
                }
            }
            break;
        } else {
            segmentPreviousIndex = segmentIndex;
        }
    }
#endif
    delay(10);
    //Serial.print(".");
}

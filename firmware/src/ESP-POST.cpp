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
#include "LowFrequencyMonitor.h"
#include <OneWire.h>
#include <DallasTemperature.h>
#include <ESPiLight.h>

const char* ssid = "";
const char* password = "";
const char* reportingServer = "";
const char* messageServer = "";
const char messageServerFingerprint[] PROGMEM = "";
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
#define ON_BOARD_BUTTON    14
 */

/*
String locationString = "third%20ground%20floor";
#define VCC_VOLTAGE_MONITOR
//#define POWER_DHT_VIA_GPIO
#define DHTPIN              4
#define ON_BOARD_BUTTON    14
 */

/*
String locationString = "second%20top%20floorA";
#define VCC_VOLTAGE_MONITOR
#define POWER_DHT_VIA_GPIO
#define DHTPOWERPIN         5 // D1
#define DHTPIN              4 // D2
#define ON_BOARD_BUTTON    14
*/

//
String locationString = "front%20door4";
#define VCC_VOLTAGE_MONITOR
#define POWER_DHT_VIA_GPIO
#define DHTPOWERPIN         5 // D1
#define DHTPIN              4 // D2
#define ON_BOARD_BUTTON     14
#define RX433PIN            13 // D7
#define TX433PIN            -1 
//

/*
// String locationString = "rearwall%20top%20floor";
String locationString = "frontwall%20top%20floor";
#define VCC_VOLTAGE_MONITOR
//#define POWER_DHT_VIA_GPIO
// D6
// right of rear wall
// left of front wall
#define DHTPOWERPIN         12
// D7
// middle of rear wall
// middle of front wall
#define DHTPOWERPIN1        13
// D1
// left side of rear wall
// right side of front wall
#define DHTPOWERPIN2         5

//#define DHTPOWERPIN3         ?
// D5
// right of rear wall
// left of front wall
#define DHTPIN              14
// D2
// middle of rear wall
// middle of front wall
#define DHTPIN1              4
// D3
// left side of rear wall
// righ side of front wall
#define DHTPIN2              0
// D?
//#define DHTPIN3              ?
*/
// D3 program button
//#define ON_BOARD_BUTTON     0
// known pins
// D1 - 5
// D2 - 4
// D3 - 0 --- also reset button
// D5 - 14
// D6 - 12
// D7 - 13
// D9 and D10 are busy when serial has begun
// D8 and D4 would have pull up and pull down resistors
// D0 would be connected to reset for sleep mode

// D2
//#define EXTERNAL_BUTTON1    4
// D6
// #define EXTERNAL_BUTTON2    12
/*
String locationString = "aquariumA";
#define VCC_VOLTAGE_MONITOR
#define DHTPIN              2
//#define BUTTONPIN           5
//#define GREEN_LED_PIN       13
//#define RED_LED_PIN         12
//#define BLUE_LED_PIN        14
#define DS18b20_PIN         0
+*/

/*
String locationString = "aquariumB1";
#define VCC_VOLTAGE_MONITOR
#define BUTTONPIN           0
#define GREEN_LED_PIN       13
#define RED_LED_PIN         12
#define BLUE_LED_PIN        14
#define DS18b20_PIN         4
*/

/*
String locationString = "pressure%20monitor";
#define VCC_VOLTAGE_MONITOR
#define PRESSURE_MONITOR
#define SdaPin              12
#define SclPin              14
#define ON_BOARD_BUTTON      5
 */

/*
String locationString = "audio%20monitor";
#define PRESSURE_MONITOR
#define ON_BOARD_BUTTON      5
#define LedDataPin          14
#define LedClockPin         13
#define LedCsPin            12
*/

/*
String locationString = "second%20testing%20board";
#define POWER_DHT_VIA_GPIO
#define VCC_VOLTAGE_MONITOR
#define DHTPOWERPIN         5
 */

//#define DHTPIN              4 // top floor = 4 // aquarium = 2
//#define BUTTONPIN           5
//#define ON_BOARD_BUTTON     0
//#define EXTERNAL_BUTTON1    1
//#define EXTERNAL_BUTTON2    3
#define DHTTYPE             DHT22

#ifdef VCC_VOLTAGE_MONITOR
ADC_MODE(ADC_VCC);
#endif

#ifdef DHTPIN
DHT_Unified dht(DHTPIN, DHTTYPE);
#endif
#ifdef DHTPIN1
DHT_Unified dht1(DHTPIN1, DHTTYPE);
#endif
#ifdef DHTPIN2
DHT_Unified dht2(DHTPIN2, DHTTYPE);
#endif
#ifdef DHTPIN3
DHT_Unified dht3(DHTPIN3, DHTTYPE);
#endif

#ifdef DS18b20_PIN
OneWire oneWire(DS18b20_PIN);
DallasTemperature sensors(&oneWire);
DeviceAddress thermometer0;
#endif

#ifdef TX433PIN
ESPiLight rf433(TX433PIN);
#endif

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
    Serial.println(messageString);
    WiFiClientSecure client;
    client.setFingerprint(messageServerFingerprint);
    if (!client.connect(messageServer, httpsPort)) {
        Serial.println("message failed");
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
    Serial.println(connectionString);
    client.print(connectionString);
    while (client.connected() || client.available())
    {
      if (client.available())
      {
        String line = client.readStringUntil('\n');
        Serial.println(line);
      }
    }
    client.stop();
}

void requestRGB(String locationString, bool refresh) {
    WiFiClient client;
    if (!client.connect(reportingServer, httpPort)) {
        Serial.println("connection failed requestRGB");
        sendMessage("connection%20failed%20requestRGB");
        return;
    }
    String connectionString = "GET ";
    connectionString += "/monitor/currentRGB?location=";
    connectionString += locationString;
    connectionString += (refresh) ? "&refresh=true" : "";
    connectionString += " HTTP/1.1\r\n";
    connectionString += "Host: ";
    connectionString += reportingServer;
    connectionString += "\r\n";
    connectionString += "Connection: close\r\n\r\n";
    Serial.println(connectionString);
    client.print(connectionString);
    //unsigned long timeout = millis();
    long requestMillisOffset = millis();
    String parsedValues = "";
    //String receivedValues = "";
    int segmentIndex = 0;
    while (client.connected() || client.available())
    {
      if (client.available()) {
        #ifdef GREEN_LED_PIN
        if (refresh) {
            analogWrite(RED_LED_PIN, 0xff);
            analogWrite(GREEN_LED_PIN, 0xff);
            analogWrite(BLUE_LED_PIN, 50);
          }
        #endif
        String line = client.readStringUntil('\r');
        Serial.println(line);
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
                            //Serial.println(redString + "-" + greenString + "-" + blueString + "-" + delayString);
                            parsedValues = parsedValues + segmentIndex + "_" + redString + "-" + greenString + "-" + blueString + "_" + delayString + "%0A";
                            int redValue = (int) strtol(redString.c_str(), NULL, 16);
                            int greenValue = (int) strtol(greenString.c_str(), NULL, 16);
                            int blueValue = (int) strtol(blueString.c_str(), NULL, 16);
                            long delayValue = strtol(delayString.c_str(), NULL, 16);
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
    } else {
        #ifdef GREEN_LED_PIN
        if (refresh) {
            analogWrite(RED_LED_PIN, 50);
            analogWrite(GREEN_LED_PIN, 0xff);
            analogWrite(BLUE_LED_PIN, 0xff);
          }
        #endif
      }
    }
    client.stop();
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

void serialiseTemperatureData(int sensorIndex, String &url, String &telemetryString, String &errorString) {
    sensors_event_t event;
    int sensorRetries = 3;
    while (sensorRetries > 0) {
        switch (sensorIndex) {
#ifdef DHTPIN
            case 0:
                dht.temperature().getEvent(&event);
                break;
#endif
#ifdef DHTPIN1
            case 1:
                dht1.temperature().getEvent(&event);
                break;
#endif
#ifdef DHTPIN2
            case 2:
                dht2.temperature().getEvent(&event);
                break;
#endif
#ifdef DHTPIN3
            case 3:
                dht3.temperature().getEvent(&event);
                break;
#endif
            default:
                errorString += "no%20sensor%20";
                errorString += sensorIndex;
                errorString += "%20";
                sendMessage(errorString);
                return;
        }
        if (isnan(event.temperature)) {
            sensorRetries--;
            errorString += "retry%20";
            delay(3000);
        } else {
            sensorRetries = 0;
        }
    }
    url += "&temperature=";
    if (isnan(event.temperature)) {
        telemetryString += "Error reading temperature<br/>";
        errorString += "Error%20reading%20temperature%20";
        errorString += sensorIndex;
        errorString += "%20";
        sendMessage(errorString);
    } else {
        telemetryString += "Temperature: ";
        telemetryString += event.temperature;
        telemetryString += " *C<br/>";
        url += event.temperature;
    }
    switch (sensorIndex) {
#ifdef DHTPIN
        case 0:
            dht.humidity().getEvent(&event);
            break;
#endif
#ifdef DHTPIN1
        case 1:
            dht1.humidity().getEvent(&event);
            break;
#endif
#ifdef DHTPIN2
        case 2:
            dht2.humidity().getEvent(&event);
            break;
#endif
#ifdef DHTPIN3
        case 3:
            dht3.humidity().getEvent(&event);
            break;
#endif
        default:
            errorString += "no%20sensor%20";
            errorString += sensorIndex;
            errorString += "%20";
            sendMessage(errorString);
            return;
    }
    url += "&humidity=";
    if (isnan(event.relative_humidity)) {
        telemetryString += "Error reading humidity<br/>";
        errorString += "Error%20reading%20humidity%20";
        errorString += sensorIndex;
        errorString += "%20";
        sendMessage(errorString);
    } else {
        telemetryString += "Humidity: ";
        telemetryString += event.relative_humidity;
        telemetryString += "%<br/>";
        url += event.relative_humidity;
    }
}

void sendMonitoredData() {
    String errorString = "";
    Serial.println(reportingServer);
    String url = "/monitor/add?location=";
    url += locationString;
    String telemetryString = "";
#ifdef DHTPIN
#ifdef DHTPOWERPIN
    pinMode(DHTPOWERPIN, OUTPUT);
    digitalWrite(DHTPOWERPIN, 1);
#endif
#ifdef DHTPOWERPIN1
    pinMode(DHTPOWERPIN1, OUTPUT);
    digitalWrite(DHTPOWERPIN1, 1);
#endif
#ifdef DHTPOWERPIN2
    pinMode(DHTPOWERPIN2, OUTPUT);
    digitalWrite(DHTPOWERPIN2, 1);
#endif
#ifdef DHTPOWERPIN3
    pinMode(DHTPOWERPIN3, OUTPUT);
    digitalWrite(DHTPOWERPIN3, 1);
#endif
#ifdef DHTPIN
    dht.begin();
#endif
#ifdef DHTPIN1
    dht1.begin();
#endif
#ifdef DHTPIN2
    dht2.begin();
#endif
#ifdef DHTPIN3
    dht3.begin();
#endif
    serialiseTemperatureData(0, url, telemetryString, errorString);
#ifdef DHTPIN1
    serialiseTemperatureData(1, url, telemetryString, errorString);
#endif
#ifdef DHTPIN2
    serialiseTemperatureData(2, url, telemetryString, errorString);
#endif
#ifdef DHTPIN3
    serialiseTemperatureData(3, url, telemetryString, errorString);
#endif
#ifdef DHTPOWERPIN
    // power down the DHT
    pinMode(DHTPOWERPIN, INPUT);
#endif
#ifdef DHTPOWERPIN1
    // power down the DHT
    pinMode(DHTPOWERPIN1, INPUT);
#endif
#ifdef DHTPOWERPIN2
    // power down the DHT
    pinMode(DHTPOWERPIN2, INPUT);
#endif
#ifdef DHTPOWERPIN3
    // power down the DHT
    pinMode(DHTPOWERPIN3, INPUT);
#endif
#endif
#ifdef DS18b20_PIN
   sensors.requestTemperatures();
   float tempC = sensors.getTempC(thermometer0);
   //sendMessage(String(tempC) + "c");
   url += "&temperature=";
   url += tempC;
#endif
    telemetryString += "ADC: ";
    telemetryString += analogRead(A0);
    telemetryString += "<br/>";
    telemetryString += "voltage: ";
    url += "&voltage=";
#ifdef VCC_VOLTAGE_MONITOR
    // battery 3.83v = "voltage":2.61
    url += (ESP.getVcc() / 1000.0);
    telemetryString += (analogRead(A0) / 1000.0);
#else
    url += (analogRead(A0) / 69.0);
    telemetryString += (analogRead(A0) / 69.0);
#endif
    telemetryString += "v";

    Serial.println(telemetryString);
    WiFiClient client;
    if (!client.connect(reportingServer, httpPort)) {
        Serial.println("connection failed");
        sendMessage("connection%20failed%20add%20" + locationString);
        return;
    }
#ifdef PRESSURE_MONITOR
    url += serialisePressureData(true);
#endif
    url += "&error=";
    url += errorString;
    Serial.println(url);

    String connectionString = "GET ";
    connectionString += url;
    connectionString += " HTTP/1.1\r\n";
    connectionString += "Host: ";
    connectionString += reportingServer;
    connectionString += "\r\n";
    connectionString += "Connection: close\r\n\r\n";
    Serial.println(connectionString);
    client.print(connectionString);
    while (client.connected() || client.available()) {
      if (client.available()) {
        String line = client.readStringUntil('\n');
        Serial.println(line);
      }
    }
    client.stop();
}

ICACHE_RAM_ATTR void requestRGBInterrupt() {
    requestRGBButtonChanged++;
}

ICACHE_RAM_ATTR void onBoardButtonChangeInterrupt() {
    onBoardButtonChanged++;
}

ICACHE_RAM_ATTR void externalButton1ChangeInterrupt() {
    externalButton1Changed++;
}

ICACHE_RAM_ATTR void externalButton2ChangeInterrupt() {
    externalButton2Changed++;
}

String urlEncode(String inputString) {
    String encodedString = "";
    char currentChar;
    char msv;
    char lsv;
    for (int index = 0; index < inputString.length(); index++) {
        currentChar = inputString.charAt(index);
        if (currentChar == ' ') {
            encodedString += '+';
        } else if (isalnum(currentChar)) {
            encodedString += currentChar;
        } else {
            if ((currentChar & 0xf) > 9) {
                lsv = (currentChar & 0xf) - 10 + 'A';
            } else {
                lsv = (currentChar & 0xf) + '0';
            }
            currentChar = (currentChar >> 4) & 0xf;
            if (currentChar > 9) {
                msv = currentChar - 10 + 'A';
            } else {
                msv = currentChar + '0';
            }
            encodedString += '%';
            encodedString += msv;
            encodedString += lsv;
        }
    }
    return encodedString;
}

#ifdef TX433PIN
String rf433Results = "";
uint16_t pulsesLast[100];
uint16_t matchCount = 0;
uint8_t indexCurrent = 0;
ICACHE_RAM_ATTR void rfRawCallback(const uint16_t* pulses, size_t length) {
    for (unsigned int pulseIndex = 0; pulseIndex < length; pulseIndex++) {
        if(indexCurrent < 100) {
            if (matchCount == 0) {
                pulsesLast[indexCurrent] = pulses[pulseIndex];
            } else {
                uint16_t pulseDiff = abs(pulsesLast[indexCurrent] - pulses[pulseIndex]);
                if (pulseDiff > 500) {
                    //Serial.print("pulseDiff:");
                    //Serial.println(pulseDiff);
                    indexCurrent = 0;
                    matchCount = 0;
                    pulsesLast[indexCurrent] = pulses[pulseIndex];
                }
            }
        }
        indexCurrent++;
        if (pulses[pulseIndex] > 5100) {
            if (matchCount == 1) {
                unsigned int maxLength = 100*5;
                char restultString[maxLength];
                unsigned int resultLength = 0;
                for (uint8_t outputIndex = 0; outputIndex < indexCurrent; outputIndex++) {
                    Serial.print(pulsesLast[outputIndex]);
                    Serial.print(" ");
                    resultLength += snprintf(restultString + resultLength, maxLength - resultLength, " %u", pulsesLast[outputIndex]);
                }
                Serial.println();
                rf433Results = String(restultString);
            }
            indexCurrent = 0;
            matchCount++;
        }
    }
}
#endif

void setup() {
    //Serial.begin(115200);
#ifdef PRESSURE_MONITOR
    //Serial.begin(115200);
#ifdef SdaPin
    startPressureMonitor(SdaPin, SclPin);
#else
    startAudioMonitor();
#endif
#ifdef LedDataPin
    startLedPanel(LedDataPin, LedClockPin, LedCsPin);
#endif
#endif
#ifndef BUTTON_MESSAGE
    //Serial.begin(115200);
#endif
    delay(10);
#ifdef ON_BOARD_BUTTON
    pinMode(ON_BOARD_BUTTON, INPUT_PULLUP);
    //digitalWrite(ON_BOARD_BUTTON, 1);
    attachInterrupt(ON_BOARD_BUTTON, onBoardButtonChangeInterrupt, CHANGE);
#endif
#ifdef EXTERNAL_BUTTON1
    pinMode(EXTERNAL_BUTTON1, INPUT_PULLUP);
    //digitalWrite(EXTERNAL_BUTTON1, 1);
    attachInterrupt(EXTERNAL_BUTTON1, externalButton1ChangeInterrupt, CHANGE);
#endif
#ifdef EXTERNAL_BUTTON2
    pinMode(EXTERNAL_BUTTON2, INPUT_PULLUP);
    //digitalWrite(EXTERNAL_BUTTON2, 1);
    attachInterrupt(EXTERNAL_BUTTON2, externalButton2ChangeInterrupt, CHANGE);
#endif
#ifdef GREEN_LED_PIN
    analogWriteRange(0xff);
    pinMode(RED_LED_PIN, OUTPUT);
    analogWrite(RED_LED_PIN, 0xff);
    pinMode(GREEN_LED_PIN, OUTPUT);
    analogWrite(GREEN_LED_PIN, 0xff);
    pinMode(BLUE_LED_PIN, OUTPUT);
    analogWrite(BLUE_LED_PIN, 0xff);

    pinMode(BUTTONPIN, INPUT_PULLUP);
    attachInterrupt(BUTTONPIN, requestRGBInterrupt, CHANGE);
#endif
    WiFi.mode(WIFI_STA);
    WiFi.begin(ssid, password);
    while (WiFi.status() != WL_CONNECTED) {
        delay(500);
        Serial.println(".");
    }
    Serial.println("Connected: ");
    Serial.println(ssid);
    sendMessage(startMessage);
    sendMessage(urlencode(ESP.getResetReason()));
#ifdef GREEN_LED_PIN
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
    requestRGB(locationString, true);
#endif

#ifdef DS18b20_PIN
  sensors.begin();
  if (!sensors.getAddress(thermometer0, 0)) {
    sendMessage("Unable%20to%20find%20thermometer0");
  } else {
    sensors.requestTemperatures();
    float tempC = sensors.getTempC(thermometer0);
    sendMessage(String(tempC) + "c");
  }
#endif

#ifdef TX433PIN
  Serial.println("initReceiver");
  rf433.setPulseTrainCallBack(rfRawCallback);
  rf433.initReceiver(RX433PIN);
#endif
}

void loop() {
#ifdef TX433PIN
    rf433.disableReceiver();
#endif
    if (lastDataSentMs + dataSendDelayMs < millis()) {
        Serial.println("IP Address");
        Serial.println(WiFi.localIP());

#ifdef PRESSURE_MONITOR
        if (interestingPressureData()) {
            sendMessage(serialisePressureData(false));
        }
#endif

        sendMonitoredData();
#ifdef GREEN_LED_PIN
        requestRGB(locationString, false);
#endif
        lastDataSentMs = millis();
    }
    if (onBoardButtonChanged > 0 || externalButton1Changed > 0 || externalButton2Changed > 0) {
        /*IPAddress broadcastIp;
        broadcastIp = ~WiFi.subnetMask() | WiFi.gatewayIP();
        udp.beginPacketMulticast(broadcastIp, localUdpPort, WiFi.localIP());
        if (onBoardButtonChanged > 0) udp.write(buttonMessage0);
        if (externalButton1Changed > 0) udp.write(buttonMessage1);
        if (externalButton2Changed > 0) udp.write(buttonMessage1);
        udp.write("\a\n");
        udp.endPacket();
        udp.flush();
        udp.stop(); // flush and stop added to address issues of the ESP hanging after send
        Serial.print("onBoardButtonChanged: ");
        Serial.println(onBoardButtonChanged);
        Serial.print(" externalButton1Changed: ");
        Serial.println(externalButton1Changed);
        Serial.print(" externalButton2Changed: ");
        Serial.println(externalButton2Changed);
        if (onBoardButtonChanged && onBoardButtonDataSentMs + buttonDataSendDelayMs < millis()) {
            onBoardButtonDataSentMs = millis();
            Serial.println("sending button0 message");
            sendMessage(buttonMessage0);
        }
        if (externalButton1Changed && externalButton1DataSentMs + buttonDataSendDelayMs < millis()) {
            externalButton1DataSentMs = millis();
            Serial.println("sending button1 message");
            sendMessage(buttonMessage1);
        }
        if (externalButton2Changed && externalButton2DataSentMs + buttonDataSendDelayMs < millis()) {
            externalButton2DataSentMs = millis();
            Serial.println("sending button2 message");
            sendMessage(buttonMessage2);
        }
        onBoardButtonChanged = 0;
        externalButton1Changed = 0;
        externalButton2Changed = 0;
        */
    }
    if (requestRGBButtonChanged > 0) {
        sendMessage("requestRGBButton");
        requestRGB(locationString, true);
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
          //Serial.println("setting LEDs: " + segmentIndex);
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
                    analogWrite(RED_LED_PIN, 0xff - tweenedRed);
                    analogWrite(GREEN_LED_PIN, 0xff - tweenedGreen);
                    analogWrite(BLUE_LED_PIN, 0xff - tweenedBlue);
                }
            } else {
                analogWrite(RED_LED_PIN, 0xff - segmentRGB[segmentPreviousIndex].redValue);
                analogWrite(GREEN_LED_PIN, 0xff - segmentRGB[segmentPreviousIndex].greenValue);
                analogWrite(BLUE_LED_PIN, 0xff - segmentRGB[segmentPreviousIndex].blueValue);
                if (segmentPreviousIndex != segmentMessageIndex) {
                    sendMessage(String("I") + segmentPreviousIndex + "%20R" + String(segmentRGB[segmentPreviousIndex].redValue, HEX) + "%20G" + String(segmentRGB[segmentPreviousIndex].greenValue, HEX) + "%20B" + String(segmentRGB[segmentPreviousIndex].blueValue, HEX));
                    segmentMessageIndex = segmentPreviousIndex;
                }
            }
            //Serial.println(String("I") + segmentPreviousIndex + " R" + String(segmentRGB[segmentPreviousIndex].redValue, HEX) + " G" + String(segmentRGB[segmentPreviousIndex].greenValue, HEX) + " B" + String(segmentRGB[segmentPreviousIndex].blueValue, HEX));
            break;
        } else {
            segmentPreviousIndex = segmentIndex;
        }
    }
#endif
#ifdef PRESSURE_MONITOR
    acquirePressureData();
#ifdef LedDataPin
    updateLedPanel();
#endif
#endif
#ifdef TX433PIN
    if (rf433Results != "") {
        String urlencodedString = urlencode(rf433Results);
        rf433Results = "";
        Serial.println(urlencodedString);
        sendMessage(urlencodedString);
    }
rf433.enableReceiver();
    rf433.loop();
#endif
    delay(10);
    //Serial.print(".");
}

/*
 * Copyright (C) 2018 Peter Withers
 */

/*
 * LowFrequencyMonitor.cpp
 *
 * Created: 16/04/2018 20:44
 * Author : Peter Withers <peter-gthb@bambooradical.com>
 */

#include <Sodaq_BMP085.h>
#include <Wire.h>
#include <arduinoFFT.h>

Sodaq_BMP085 pressureSensor;
arduinoFFT FFT = arduinoFFT();

#define SdaPin           12
#define SclPin           14

double baselinePressure;

const uint16_t samples = 64;
const double signalFrequency = 50;
const double samplingFrequency = 120;
const uint8_t amplitude = 100;

double arrayReal[samples];
double arrayImag[samples];

void setup() {
    Serial.begin(115200);
    Serial.println("LowFrequencyMonitor");

    Wire.pins(SdaPin, SclPin);
    pressureSensor.begin();
    baselinePressure = pressureSensor.readPressure();
    Serial.print("baseline pressure: ");
    Serial.print(baselinePressure);
    Serial.println(" mb");
}

void loop() {
    unsigned long lastMs = millis();
    double temperature = pressureSensor.readTemperature();
    double pressure = pressureSensor.readPressure();
    double altitude = pressureSensor.readAltitude(baselinePressure);
    for (int sampleIndex = 0; sampleIndex < samples; sampleIndex++) {
        arrayReal[sampleIndex] = pressureSensor.readPressure();
        arrayImag[sampleIndex] = 0.0;
        while (lastMs + 10 > millis()); /* attempt to sample at 100hz */
    }
    Serial.print("baseline pressure: ");
    Serial.print(temperature);
    Serial.println(" c");
    Serial.print(pressure);
    Serial.println(" mb");
    Serial.print(altitude);
    Serial.println(" m");
    Serial.print(millis());
    Serial.println(" ms");
}

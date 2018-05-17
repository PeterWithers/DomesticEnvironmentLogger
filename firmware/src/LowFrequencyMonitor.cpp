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
//#include <Adafruit_BMP280.h>
#include <arduinoFFT.h>
#include "LedControl.h"

#define D1 5
#define D2 4
#define D4 2
#define D3 0
#define BMP_SCK D1
#define BMP_MISO D4
#define BMP_MOSI D2
#define BMP_CS D3

//#define BMP280_ADDRESS (0x76)
Sodaq_BMP085 pressureSensor;
//Adafruit_BMP280 pressureSensor(BMP_CS, BMP_MOSI, BMP_MISO, BMP_SCK);
//Adafruit_BMP280 pressureSensor76();
arduinoFFT FFT = arduinoFFT();
LedControl* ledControl;

int msPerSample = 7;
const uint16_t samples = 64;
double samplingFrequency = 1000 / msPerSample;

double arrayReal[samples];
double arrayImag[samples];
double arrayPeaks[(samples >> 1)];
double arrayPeaksMax[(samples >> 1)];
double arrayRMS[(samples >> 1)];

double pressureAverage = 0;
bool hasChanged = false;
bool hasPeakMax = false;
int maxMsError = 0;

bool hasSensor = false;

void zeroData() {
    for (uint16_t index = 0; index < (samples >> 1); index++) {
        arrayPeaks[index] = 0.0;
        arrayPeaksMax[index] = 0.0;
        arrayRMS[index] = 0.0;
    }
}

void startAudioMonitor() {
    Serial.println("AudioFrequencyMonitor");
    msPerSample = 3;
    samplingFrequency = 1000 / msPerSample;
    zeroData();
}

void startPressureMonitor(int sdaPin, int sclPin) {
    Serial.println("LowFrequencyMonitor");
    hasSensor = true;
    Wire.pins(sdaPin, sclPin);
    //    hasSensor =
    pressureSensor.begin(BMP085_ULTRALOWPOWER);
    //hasSensor = pressureSensor.begin(0x77);
    //    if (!hasSensor) {
    //        Serial.println("pressure sensor failed");
    //        //  Serial.println("pressure sensor at 0x77 failed");
    //        //if(!pressureSensor76.begin(0x76)){
    //        //Serial.println("pressure sensor at 0x76 failed");
    //        //}
    //    } else {
    zeroData();
}

void startLedPanel(int dataPin, int clockPin, int csPin){
  ledControl = new LedControl(dataPin, clockPin, csPin, 4);
  for(int address=0; address < 4; address++) {
    ledControl->shutdown(address,false);
    ledControl->setIntensity(address,1);
    ledControl->clearDisplay(address);
  }
}

void updateLedPanel(){
for(int row = 0; row < 8; row++) {
  for(int column = 0; column < 8; column++) {
    for(int address = 0; address < 4; address++) {
      ledControl->setLed(address, row, column, arrayReal[column + (7 - address) << 1] > row);
    }
  }
}
}

void updatePeaks(double *valueData, uint16_t bufferSize) {
    for (uint16_t index = 0; index < bufferSize; index++) {
        //double hertz = ((index * 1.0 * samplingFrequency) / samples);
        //Serial.print(hertz, 6);
        //Serial.print("Hz");
        //Serial.print(" ");
        //Serial.println(valueData[index], 4);
        if (arrayPeaks[index] < valueData[index]) {
            arrayPeaks[index] = valueData[index];
            hasChanged = true;
        }
        if (arrayPeaksMax[index] <= valueData[index]) {
            arrayPeaksMax[index] = valueData[index];
            hasPeakMax = true;
        }
        arrayRMS[index] = sqrt(((arrayRMS[index] * arrayRMS[index]) + (valueData[index] * valueData[index])) / 2);
        //Serial.println();
    }
}

String serialisePressureData(bool clearPeaks) {
    hasChanged = false;
    hasPeakMax = false;
    String pressureDataString = "";
    if (clearPeaks) pressureDataString += "&magnitudes=";
    for (uint16_t index = 0; index < (samples >> 1); index++) {
        pressureDataString += ((index * 1.0 * samplingFrequency) / samples);
        pressureDataString += "Hz%20";
        pressureDataString += arrayPeaks[index];
        pressureDataString += "%20";
        pressureDataString += arrayRMS[index];
        pressureDataString += "%0A";
        if (clearPeaks) {
            arrayPeaks[index] = 0.0;
            arrayRMS[index] = 0.0;
        }
    }
    if (clearPeaks) pressureDataString += "&";
    pressureDataString += "maxMsError";
    if (clearPeaks) pressureDataString += "=";
    pressureDataString += maxMsError;
    if (clearPeaks) maxMsError = 0;
    return pressureDataString;
}

bool interestingPressureData() {
    return hasPeakMax;
}

void acquirePressureData() {
    //    if (!hasSensor) {
    //        Serial.println("pressure sensor failed");
    //    } else {
    if (hasSensor) {
        double temperature = pressureSensor.readTemperature();
        double pressure = pressureSensor.readPressure();
        Serial.print("baseline pressure: ");
        Serial.print(temperature);
        Serial.println(" c");
        Serial.print(pressure);
        Serial.println(" ");
    }
    double pressureSum = 0;
    unsigned long startMs = millis();
    for (int sampleIndex = 0; sampleIndex < samples; sampleIndex++) {
        unsigned long lastMs = millis();
        double pressureCurrent;
        if (hasSensor) {
            pressureCurrent = pressureSensor.readRawPressure();
        } else {
            pressureCurrent = analogRead(A0);
        }
        pressureSum += pressureCurrent;
        arrayReal[sampleIndex] = pressureCurrent - pressureAverage;
        arrayImag[sampleIndex] = 0.0;
        while (lastMs + msPerSample > millis()); /* attempt to sample at msPerSample */
    }
    pressureAverage = pressureSum / samples;
    unsigned long endMs = millis();
    int msError = endMs - startMs - (samples * msPerSample);
    Serial.print(msError);
    Serial.println(" ms");

    if (maxMsError < msError) maxMsError = msError;
    FFT.Windowing(arrayReal, samples, FFT_WIN_TYP_HAMMING, FFT_FORWARD);
    FFT.Compute(arrayReal, arrayImag, samples, FFT_FORWARD);
    FFT.ComplexToMagnitude(arrayReal, arrayImag, samples);
    updatePeaks(arrayReal, (samples >> 1));
    double majorPeakHz = FFT.MajorPeak(arrayReal, samples, samplingFrequency);
    Serial.println(majorPeakHz, 6);
    //    }
}

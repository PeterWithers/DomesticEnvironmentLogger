/*
 * Copyright (C) 2018 Peter Withers
 */

/*
 * LowFrequencyMonitor.cpp
 *
 * Created: 16/04/2018 20:44
 * Author : Peter Withers <peter-gthb@bambooradical.com>
 */

//#include <Sodaq_BMP085.h>
#include <Wire.h>
#include <Adafruit_BMP280.h>
#include <arduinoFFT.h>

#define D1 5
#define D2 4
#define D4 2
#define D3 0
#define BMP_SCK D1
#define BMP_MISO D4
#define BMP_MOSI D2
#define BMP_CS D3

//#define BMP280_ADDRESS (0x76)
//Sodaq_BMP085 pressureSensor;
Adafruit_BMP280 pressureSensor(BMP_CS, BMP_MOSI, BMP_MISO,  BMP_SCK);
//Adafruit_BMP280 pressureSensor76();
arduinoFFT FFT = arduinoFFT();

double baselinePressure;

const uint16_t samples = 64;
const double signalFrequency = 50;
const double samplingFrequency = 120;
const uint8_t amplitude = 100;

double arrayReal[samples];
double arrayImag[samples];
double arrayPeeks[(samples >> 1)];

bool hasChanged = false;

bool hasSensor = false;

void startPressureMonitor(int sdaPin, int sclPin) {
    Serial.println("LowFrequencyMonitor");
    //Wire.pins(sdaPin, sclPin);
    hasSensor = pressureSensor.begin();
    //hasSensor = pressureSensor.begin(0x77);
    if (!hasSensor) {
      Serial.println("pressure sensor software SPI failed");
      //  Serial.println("pressure sensor at 0x77 failed");
        //if(!pressureSensor76.begin(0x76)){
          //Serial.println("pressure sensor at 0x76 failed");
        //}
    } else {
        baselinePressure = pressureSensor.readPressure();
        Serial.print("baseline pressure: ");
        Serial.print(baselinePressure);
        Serial.println(" mb");
    }
    for (uint16_t index = 0; index < (samples >> 1); index++) {
        arrayPeeks[index] = 0.0;
    }
}

void updatePeeks(double *valueData, uint16_t bufferSize) {
    for (uint16_t index = 0; index < bufferSize; index++) {
        double hertz = ((index * 1.0 * samplingFrequency) / samples);
        Serial.print(hertz, 6);
        Serial.print("Hz");
        Serial.print(" ");
        Serial.println(valueData[index], 4);
        if (arrayPeeks[index] < valueData[index]) {
            arrayPeeks[index] = valueData[index];
            hasChanged = true;
        }
        Serial.println();
    }
}

String serialisePressureData() {
    hasChanged = false;
    String pressureDataString = "";
    for (uint16_t index = 0; index < (samples >> 1); index++) {
        pressureDataString += ((index * 1.0 * samplingFrequency) / samples);
        pressureDataString += "Hz ";
        pressureDataString += arrayPeeks[index];
        pressureDataString += "\n";
    }
    return pressureDataString;
}

bool interestingPressureData() {
    return hasChanged;
}

void acquirePressureData() {
    if (!hasSensor) {
        Serial.println("pressure sensor failed");
    } else {
        double temperature = pressureSensor.readTemperature();
        double pressure = pressureSensor.readPressure();
        double altitude = pressureSensor.readAltitude(baselinePressure);
        double cycles = (((samples - 1) * signalFrequency) / samplingFrequency);
        for (int sampleIndex = 0; sampleIndex < samples; sampleIndex++) {
            unsigned long lastMs = millis();
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

        FFT.Windowing(arrayReal, samples, FFT_WIN_TYP_HAMMING, FFT_FORWARD);
        FFT.Compute(arrayReal, arrayImag, samples, FFT_FORWARD);
        FFT.ComplexToMagnitude(arrayReal, arrayImag, samples);
        updatePeeks(arrayReal, (samples >> 1));
        double x = FFT.MajorPeak(arrayReal, samples, samplingFrequency);
        Serial.println(x, 6);
    }
}

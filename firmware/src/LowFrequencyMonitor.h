/*
 * Copyright (C) 2018 Peter Withers
 */

/*
 * LowFrequencyMonitor.h
 *
 * Created: 19/04/2018 20:10
 * Author : Peter Withers <peter-gthb@bambooradical.com>
 */

void startAudioMonitor();
void startPressureMonitor(int sdaPin, int sclPin);
String serialisePressureData(bool clearPeaks);
bool interestingPressureData();
void acquirePressureData();

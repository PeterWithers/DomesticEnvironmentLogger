/*
 * Copyright (C) 2019 Peter Withers
 */


/*
 * File:   CvData.h
 * Author : Peter Withers <peter-gthb@bambooradical.com>
 *
 * Created on January 1, 2019, 21:48 PM
 */

#ifndef CVDATA_H
#define CVDATA_H

#define SERIAL_NUMBER
#define ACCESS_KEY
#define PASSWORD
#define PATH /ecus/rrc/uiStatus

class CvData {
public:
    int getCvData();
    void postCvData();
private:
};

#endif /* CVDATA_H */



package com.evilport.accelerometergame;

/**
 * Created by dibak on 10/16/2017.
 */

class GlobalVariables {
    float x, y, z;
    double s;
    String numLockState;
    String operation;
    static GlobalVariables globalVariables = new GlobalVariables();

    GlobalVariables() {
        x = y = z = 0;
        s = 10;
        numLockState = "off";
        operation = "unknown";
    }
}

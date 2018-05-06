package com.oracle.iot.sample.mydriveapp.service.Filters;

public interface Filter {
    float[] filter(float[] values);
    float[] getOutput();
    void setTimeConstant(float timeConstant);
    void reset();
}

package com.oracle.iot.sample.mydriveapp.service;

import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.oracle.iot.sample.mydriveapp.service.Filters.LinearAccelerationFilter;
import com.oracle.iot.sample.mydriveapp.service.Filters.LowPassFilter;

public class AccelerationData<T> extends LiveData<float[]> {

    private SensorManager sensorManager;
    private SimpleSensorListener listener;
    private int sensorFrequency = SensorManager.SENSOR_DELAY_FASTEST;

    private float startTime = 0;
    private int count = 0;
    private boolean lowPassFilteringEnabled = false;
    private boolean axisInverted = false;
    private float[] acceleration = new float[3];
    private boolean linearAccelerationEnabled;
    private LowPassFilter lowPassAccelerationSmoothingFilter;
    private LinearAccelerationFilter linearAccelerationFilter;

    public AccelerationData(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        listener = new SimpleSensorListener();
        initializeFilters();
    }

    @Override
    protected void onActive() {
        startTime =0;
        count = 0;
        registerSensors(sensorFrequency);
    }

    @Override
    protected void onInactive(){
        unregisterSensors();
    }

    private void unregisterSensors() {
        sensorManager.unregisterListener(listener);
    }

    public void setAxisInverted(boolean axisInverted) {
        this.axisInverted = axisInverted;
    }

    public boolean isAxisInverted() {
        return this.axisInverted;
    }

    public void setSensorFrequency(int sensorFrequency) {
        this.sensorFrequency = sensorFrequency;
    }

    public int getSensorFrequency() {
        return this.sensorFrequency;
    }

    public void enableLinearAcceleration(boolean enabled) {
        unregisterSensors();
        this.linearAccelerationEnabled = enabled;
        registerSensors(sensorFrequency);

    }

    public void setLowPassFilterSmoothingTimeConstant(float timeConstant) {
        this.lowPassAccelerationSmoothingFilter.setTimeConstant(timeConstant);
    }

    public void enableLowPassFiltering(boolean enabled) {
        this.lowPassFilteringEnabled = enabled;
    }

    public boolean isLowPassFilteringEnabled() {
        return this.lowPassFilteringEnabled;
    }

    public void setLinearAccelerationTimeConstant(float timeConstant) {
        linearAccelerationFilter.setTimeConstant(timeConstant);
    }

    private float[] invert(float[] values) {
        for(int i = 0; i < 3; i++) {
            values[i] = -values[i];
        }

        return values;
    }

    private void initializeFilters(){
        lowPassAccelerationSmoothingFilter = new LowPassFilter();
        linearAccelerationFilter = new LinearAccelerationFilter();

    }

    private void processAcceleration(float[] accelerationValues) {
        if(axisInverted) {
            accelerationValues = invert(accelerationValues);
        }

        if (lowPassFilteringEnabled) {
            accelerationValues = lowPassAccelerationSmoothingFilter.filter(accelerationValues);
        }

        System.arraycopy(accelerationValues, 0, acceleration, 0, accelerationValues.length);
    }

    private void registerSensors(int sensorDelay) {

        lowPassAccelerationSmoothingFilter.reset();
        linearAccelerationFilter.reset();
        sensorManager.registerListener(listener,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                sensorDelay);
    }

    private class SimpleSensorListener implements SensorEventListener {

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                if (linearAccelerationEnabled) {
                    float[] accelerationValues = new float[3];
                    System.arraycopy(event.values, 0, accelerationValues, 0, event.values.length);
                    processAcceleration(linearAccelerationFilter.filter(accelerationValues));
                } else {
                    processAcceleration(event.values);
                }
                setValue(acceleration);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    }
}

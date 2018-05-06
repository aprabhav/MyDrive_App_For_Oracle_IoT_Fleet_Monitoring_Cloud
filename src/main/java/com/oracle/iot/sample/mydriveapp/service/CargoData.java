package com.oracle.iot.sample.mydriveapp.service;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class CargoData implements SensorEventListener {
    private MutableLiveData<float []> mCargoLiveData;
    private Context mAppContext;
    private SensorManager mSensorManager;
    private float mLastKnownRelativeHumidity;
    private float[] mValues;

    public CargoData(Context appContext){
        mAppContext = appContext;
        mCargoLiveData = new MutableLiveData<>();
        mValues = new float [5];
        mSensorManager = (SensorManager) mAppContext.getSystemService(Context.SENSOR_SERVICE);

        Sensor temperatureSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        Sensor humiditySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
        Sensor lightSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        Sensor pressureSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        Sensor proximitySensor =  mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        if (temperatureSensor != null) {
            mSensorManager.registerListener(this, temperatureSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }

        if (humiditySensor != null) {
            mSensorManager.registerListener(this, humiditySensor, SensorManager.SENSOR_DELAY_NORMAL);
        }

        if (lightSensor != null) {
            mSensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }

        if (pressureSensor != null) {
            mSensorManager.registerListener(this, pressureSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }

        if (proximitySensor != null) {
            mSensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    public LiveData<float[]> getLiveData(){
        return mCargoLiveData;
    }

    public void removeUpdates() {
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        int sensorType = sensorEvent.sensor.getType();
        switch (sensorType){
            case Sensor.TYPE_AMBIENT_TEMPERATURE:
                mValues[0] = sensorEvent.values[0];
                if (mLastKnownRelativeHumidity != 0){
                    mValues[1] = calculateAbsoluteHumidity(mValues[0], mLastKnownRelativeHumidity);
                } else
                    mValues[1] = 0f;
                break;
            case Sensor.TYPE_RELATIVE_HUMIDITY:
                mLastKnownRelativeHumidity = sensorEvent.values[0];
                break;
            case Sensor.TYPE_LIGHT:
                mValues[2] = sensorEvent.values[0];
                break;
            case Sensor.TYPE_PRESSURE:
                mValues[3] = sensorEvent.values[0];
                break;
            case Sensor.TYPE_PROXIMITY:
                mValues[4] = sensorEvent.values[0];
                break;
        }

        mCargoLiveData.setValue(mValues);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private float calculateAbsoluteHumidity(float Tc, float Rh)
    {
        float m = 17.62f;
        float Tn = 243.12f;
        float Ta = 216.7f;
        float A = 6.112f;
        float K = 273.15f;

        return (float) (Ta * (Rh/100) * A * Math.exp(m*Tc/(Tn+Tc)) / (K + Tc));
    }
}

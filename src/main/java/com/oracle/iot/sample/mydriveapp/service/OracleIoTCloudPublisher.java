package com.oracle.iot.sample.mydriveapp.service;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.oracle.iot.sample.mydriveapp.prefs.Constants;
import com.oracle.iot.sample.mydriveapp.prefs.PrefUtils;

import java.io.IOException;
import java.util.Properties;

import oracle.iot.client.DeviceModel;
import oracle.iot.client.device.Alert;
import oracle.iot.client.device.DirectlyConnectedDevice;
import oracle.iot.client.device.VirtualDevice;

public class OracleIoTCloudPublisher {

    private DirectlyConnectedDevice mVehicleSensorDevice, mCargoSensorDevice;
    private DeviceModel mVehicleSensorDeviceModel, mCargoSensorDeviceModel;
    private VirtualDevice mVehicleSensorVirtualDevice, mCargoSensorVirtualDevice;
    private Context mContext;
    private String mVehicleDeviceProvisioningFilePath, mVehicleDeviceProvisioningFilePwd,
            mCargoDeviceProvisioningFilePath, mCargoDeviceProvisioningFilePwd;

    OracleIoTCloudPublisher(Context context){
        mContext = context;
        mVehicleDeviceProvisioningFilePath = PrefUtils.getVehicleDeviceProvisioningFilePath(mContext);
        mVehicleDeviceProvisioningFilePwd = PrefUtils.getVehicleDeviceProvisioningFilePassword(mContext);
        mCargoDeviceProvisioningFilePath = PrefUtils.getCargoDeviceProvisioningFilePath(mContext);
        mCargoDeviceProvisioningFilePwd = PrefUtils.getCargoDeviceProvisioningFilePassword(mContext);
    }

    public boolean setupIoTCloudConnect(){
        boolean result = false;
        try{
            if (mVehicleSensorDevice == null && !mVehicleDeviceProvisioningFilePath.isEmpty()) {
                mVehicleSensorDevice = new DirectlyConnectedDevice(mVehicleDeviceProvisioningFilePath, mVehicleDeviceProvisioningFilePwd);

                if (!mVehicleSensorDevice.isActivated()) {
                    mVehicleSensorDevice.activate(Constants.VEHICLE_DATA_URN);
                }
                mVehicleSensorDeviceModel = mVehicleSensorDevice.getDeviceModel(Constants.VEHICLE_DATA_URN);
                // Set up a virtual device based on our device model
                mVehicleSensorVirtualDevice = mVehicleSensorDevice.createVirtualDevice(mVehicleSensorDevice.getEndpointId(), mVehicleSensorDeviceModel);
                result = true;
            }

            if (mCargoSensorDevice == null && !mCargoDeviceProvisioningFilePath.isEmpty()) {
                mCargoSensorDevice = new DirectlyConnectedDevice(mCargoDeviceProvisioningFilePath, mCargoDeviceProvisioningFilePwd);

                if (!mCargoSensorDevice.isActivated()) {
                    mCargoSensorDevice.activate(Constants.CARGO_DATA_URN);
                }
                mCargoSensorDeviceModel = mCargoSensorDevice.getDeviceModel(Constants.CARGO_DATA_URN);
                // Set up a virtual device based on our device model
                mCargoSensorVirtualDevice = mCargoSensorDevice.createVirtualDevice(mCargoSensorDevice.getEndpointId(), mCargoSensorDeviceModel);
                result = true;
            }

            if (!mVehicleSensorDevice.isActivated() && !mVehicleDeviceProvisioningFilePath.isEmpty()) {
                mVehicleSensorDevice.activate(Constants.VEHICLE_DATA_URN);
                result = true;
            }
            if (!mCargoSensorDevice.isActivated() && !mCargoDeviceProvisioningFilePath.isEmpty()) {
                mCargoSensorDevice.activate(Constants.CARGO_DATA_URN);
                result = true;
            }
            return result;
        } catch (IOException dse) {
            dse.printStackTrace();
            broadcastUpdate(Constants.EXCEPTION_ERROR, dse.getMessage());
            return result;
        }catch (Exception dse) {
            dse.printStackTrace();
            broadcastUpdate(Constants.EXCEPTION_ERROR, dse.getMessage());
            return result;
        }
    }

    public void sendDeviceDataMessagesToCloud(String type, Properties messageValues){
        try {
            // Update an attribute on our virtual device.
            // This will result in a message being sent to the cloud service with the updated attribute value

            switch(type){
                case Constants.IOT_VEHICLE_MESSAGE_TYPE:
                    if(mVehicleSensorVirtualDevice != null && !mVehicleDeviceProvisioningFilePath.isEmpty()){
                        mVehicleSensorVirtualDevice.update()
                                .set(Constants.SPEED_ATTRIBUTE, (int)Double.parseDouble(messageValues.getProperty(Constants.SPEED_ATTRIBUTE)))
                                .set(Constants.TIME_SINCE_ENGINE_START, (int)Long.parseLong(messageValues.getProperty(Constants.TIME_SINCE_ENGINE_START)))
                                .set(Constants.ODOMETER_VALUE, Double.parseDouble(messageValues.getProperty(Constants.ODOMETER_VALUE)))
                                .set(Constants.LATITUDE_ATTRIBUTE, Double.parseDouble(messageValues.getProperty(Constants.LATITUDE_ATTRIBUTE)))
                                .set(Constants.LONGITUDE_ATTRIBUTE, Double.parseDouble(messageValues.getProperty(Constants.LONGITUDE_ATTRIBUTE)))
                                .set(Constants.ALTITUDE_ATTRIBUTE, 10)
                                .finish();
                    }
                    break;
                case Constants.IOT_CARGO_MESSAGE_TYPE:
                    if(mCargoSensorVirtualDevice != null && !mCargoDeviceProvisioningFilePath.isEmpty()){
                        mCargoSensorVirtualDevice.update()
                                .set(Constants.CARGO_TEMP_VALUE, Float.parseFloat(messageValues.getProperty(Constants.CARGO_TEMP_VALUE)))
                                .set(Constants.CARGO_HUMIDITY_VALUE, Float.parseFloat(messageValues.getProperty(Constants.CARGO_HUMIDITY_VALUE)))
                                .set(Constants.CARGO_LIGHT_VALUE, Float.parseFloat(messageValues.getProperty(Constants.CARGO_LIGHT_VALUE)))
                                .set(Constants.CARGO_PRESSURE_VALUE, Float.parseFloat(messageValues.getProperty(Constants.CARGO_PRESSURE_VALUE)))
                                .set(Constants.CARGO_PROXIMITY_VALUE, Float.parseFloat(messageValues.getProperty(Constants.CARGO_PROXIMITY_VALUE)))
                                .finish();
                    }
                    break;
            }
        }catch (Exception dse) {
            dse.printStackTrace();
            broadcastUpdate(Constants.EXCEPTION_ERROR, dse.getMessage());
        }
    }

    public void sendDeviceAlertMessagesToCloud(String alertType, double alertValue){
        try {
            if(mVehicleSensorVirtualDevice != null && !mVehicleDeviceProvisioningFilePath.isEmpty()){
                Alert alert;
                switch (alertType) {
                    case VehicleTrackerService.EVENT_HARSHBRAKING:
                        alert = mVehicleSensorVirtualDevice.createAlert(Constants.HARSHBRAKING_ALERT_URN);
                        alert.raise();
                        break;
                    case VehicleTrackerService.EVENT_HARSHACCLERATION:
                        alert = mVehicleSensorVirtualDevice.createAlert(Constants.HARSHACCLERATION_ALERT_URN);
                        alert.raise();
                        break;
                    case VehicleTrackerService.EVENT_HARSHCORNERING:
                        alert = mVehicleSensorVirtualDevice.createAlert(Constants.HARSHCORNERING_ALERT_URN);
                        alert.raise();
                        break;
                    case VehicleTrackerService.EVENT_OVERSPEEDING:
                        alert = mVehicleSensorVirtualDevice.createAlert(Constants.OVERSPEEDING_ALERT_URN);
                        alert.set(Constants.DISTANCE_IN_OVERSPEED_ALERT_FIELD, alertValue);
                        alert.raise();
                        break;
                    case VehicleTrackerService.EVENT_OVERIDLING:
                        alert = mVehicleSensorVirtualDevice.createAlert(Constants.IDLING_ALERT_URN);
                        alert.set(Constants.IDLING_DURATION_ALERT_FIELD, alertValue);
                        alert.raise();
                        break;
                }
            }
        }catch (Exception dse) {
            dse.printStackTrace();
            broadcastUpdate(Constants.EXCEPTION_ERROR, dse.getMessage());
        }
    }

    public void cleanup () {
        try {
            mVehicleSensorDevice.close();
            mCargoSensorDevice.close();
        } catch (Exception dse) {
            dse.printStackTrace();
            broadcastUpdate(Constants.EXCEPTION_ERROR, dse.getMessage());
        }
    }

    private void broadcastUpdate(final String action, final String message) {
        Intent intent = new Intent(action);
        intent.putExtra("message", message);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }
}

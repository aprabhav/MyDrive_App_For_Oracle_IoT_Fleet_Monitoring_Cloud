package com.oracle.iot.sample.mydriveapp.prefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.SensorManager;
import android.preference.PreferenceManager;

public class PrefUtils
{
	public static boolean getInvertAxisPrefs(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getBoolean(Constants.AXIS_INVERSION_ENABLED_KEY, false);
	}

	public static boolean getPrefLinearAccelerationEnabled(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getBoolean(Constants.LINEAR_ACCEL_ENABLED_KEY, false);
	}

    public static float getPrefLinearAccelerationTimeConstant(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return Float.parseFloat(prefs.getString(Constants.LINEAR_ACCEL_TIME_CONSTANT_KEY, String.valueOf(0.5f)));
    }

    public static boolean getPrefLowPassFilterSmoothingEnabled(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getBoolean(Constants.LPF_SMOOTHING_ENABLED_KEY, false);
	}

    public static float getPrefLowPassFilterSmoothingTimeConstant(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return Float.parseFloat(prefs.getString(Constants.LPF_SMOOTHING_TIME_CONSTANT_KEY, String.valueOf(0.5f)));
	}

    public static int getPhoneOrientationPrefs(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return Integer.parseInt(prefs.getString(Constants.PHONE_ORIENTATION_KEY, String.valueOf(0)));
	}

	public static String getVehicleDeviceProvisioningFilePath(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getString(Constants.VEHICLE_DEVICE_PROV_FILE_PATH, "");
	}

	public static void setVehicleDeviceProvisioningFilePath(Context context, String filePath){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(Constants.VEHICLE_DEVICE_PROV_FILE_PATH, filePath);
		editor.commit();
	}

	public static String getVehicleDeviceProvisioningFilePassword(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getString(Constants.VEHICLE_DEVICE_PROV_FILE_PWD, "");
	}

	public static String getCargoDeviceProvisioningFilePath(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getString(Constants.CARGO_DEVICE_PROV_FILE_PATH, "");
	}

	public static String getCargoDeviceProvisioningFilePassword(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getString(Constants.CARGO_DEVICE_PROV_FILE_PWD, "");
	}
	public static String getOdometerValue(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(Constants.ODOMETER_VALUE_PREF, String.valueOf(0.0));
	}

	public static void setOdometerValue(Context context, String val){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(Constants.ODOMETER_VALUE_PREF, val);
		editor.apply();
	}

	public static Double getSpeedingThreshold(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return Double.parseDouble(prefs.getString(Constants.SPEEDING_THRESHOLD_PREF, String.valueOf(0)));
	}

	public static Double getIdlingTimeThreshold(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return Double.parseDouble(prefs.getString(Constants.IDLING_TIME_THRESHOLD_PREF, String.valueOf(0)));
	}

	public static Double getManeuveringAccelerationThreshold(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return Double.parseDouble(prefs.getString(Constants.MANEUVERING_ACCELERATION_THRESHOLD_PREF, String.valueOf(0)));
	}

	public static int getIoTDataMessageFrequency(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return Integer.parseInt(prefs.getString(Constants.IOT_DATA_MESSAGE_FREQUENCY_PREF, String.valueOf(10)));
	}
}

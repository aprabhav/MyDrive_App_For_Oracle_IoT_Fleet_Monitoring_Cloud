package com.oracle.iot.sample.mydriveapp.prefs;

public class Constants {

    //Oracle IoT FM Service Device Model Constants
    public static final String IOT_VEHICLE_MESSAGE_TYPE = "IoTVehicleMessageType";
    public static final String IOT_CARGO_MESSAGE_TYPE = "IoTCargoMessageType";

    public static final String VEHICLE_DATA_URN = "urn:com:oracle:iot:device:obd2:mobilesensors";
    public static final String CARGO_DATA_URN = "urn:com:oracle:iot:device:cargo:mobilesensors";

    public static final String HARSHBRAKING_ALERT_URN = "urn:com:oracle:iot:device:obd2:mobilesensors:harshBraking";
    public static final String HARSHACCLERATION_ALERT_URN = "urn:com:oracle:iot:device:obd2:mobilesensors:harshAccleration";
    public static final String HARSHCORNERING_ALERT_URN = "urn:com:oracle:iot:device:obd2:mobilesensors:harshCornering";

    public static final String OVERSPEEDING_ALERT_URN = "urn:com:oracle:iot:device:obd2:mobilesensors:overSpeeding";
    public static final String DISTANCE_IN_OVERSPEED_ALERT_FIELD = "distanceCoveredInKms";

    public static final String IDLING_ALERT_URN = "urn:com:oracle:iot:device:obd2:mobilesensors:overIdling";
    public static final String IDLING_DURATION_ALERT_FIELD = "idlingDurationSeconds";

    public static final String LONGITUDE_ATTRIBUTE = "ora_longitude";
    public static final String LATITUDE_ATTRIBUTE = "ora_latitude";
    public static final String ALTITUDE_ATTRIBUTE = "ora_altitude";
    public static final String SPEED_ATTRIBUTE = "ora_obd2_vehicle_speed";
    public static final String TIME_SINCE_ENGINE_START = "ora_obd2_runtime_since_engine_start";
    public static final String ODOMETER_VALUE = "ora_obd2_true_odometer";

    public static final String CARGO_TEMP_VALUE = "cargo_temperature";
    public static final String CARGO_HUMIDITY_VALUE = "cargo_humidity";
    public static final String CARGO_LIGHT_VALUE = "cargo_light";
    public static final String CARGO_PRESSURE_VALUE = "cargo_pressure";
    public static final String CARGO_PROXIMITY_VALUE = "cargo_proximity";

    //App Preference Constants
    public static final String AXIS_INVERSION_ENABLED_KEY = "axis_inversion_enabled_preference";
    public final static String PHONE_ORIENTATION_KEY = "phone_orientation_preference";
    public final static String VEHICLE_DEVICE_PROV_FILE_PATH = "vehicle_device_prov_file_location_preference";
    public final static String VEHICLE_DEVICE_PROV_FILE_PWD = "vehicle_device_prov_file_password_preference";
    public final static String CARGO_DEVICE_PROV_FILE_PATH = "cargo_device_prov_file_location_preference";
    public final static String CARGO_DEVICE_PROV_FILE_PWD = "cargo_device_prov_file_password_preference";
    public final static String IOT_DATA_MESSAGE_FREQUENCY_PREF = "iot_data_message_frequency_preference";
    public final static String ODOMETER_VALUE_PREF = "odometer_value_preference";
    public final static String SPEEDING_THRESHOLD_PREF = "speeding_threshold_preference";
    public final static String IDLING_TIME_THRESHOLD_PREF = "idling_time_threshold_preference";
    public final static String MANEUVERING_ACCELERATION_THRESHOLD_PREF = "maneuvering_acceleration_threshold_preference";
    public final static int LANDSCAPE = 1;

    // Preference keys for smoothing filters
    public static final String LPF_SMOOTHING_ENABLED_KEY = "lpf_smoothing_enabled_preference";
    public static final String LPF_SMOOTHING_TIME_CONSTANT_KEY = "lpf_smoothing_time_constant_preference";
    // Preference keys for linear acceleration filters
    public static final String LINEAR_ACCEL_ENABLED_KEY = "lpf_linear_accel_enabled_preference";
    public static final String LINEAR_ACCEL_TIME_CONSTANT_KEY = "lpf_linear_accel_time_constant_preference";
    public static final String EXCEPTION_ERROR = "ExceptionBroadcast";
    //Driver Behavior Constants
    public static int IDLING_SPEED = 1; //Low speed threshold for idling speed

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    public interface ACTION {
        String MAIN_ACTION = "com.oracle.mydriverapp.vehicletrackerservice.action.main";
        String STARTFOREGROUND_ACTION = "com.oracle.mydriverapp.vehicletrackerservice.action.startforeground";
        String STOPFOREGROUND_ACTION = "com.oracle.mydriverapp.vehicletrackerservice.action.stopforeground";
    }

    public interface NOTIFICATION_ID {
        int FOREGROUND_SERVICE = 1001;
    }
}

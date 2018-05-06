package com.oracle.iot.sample.mydriveapp.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.arch.lifecycle.LifecycleService;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.NotificationCompat;

import com.oracle.iot.sample.mydriveapp.R;
import com.oracle.iot.sample.mydriveapp.activity.HomeActivity;
import com.oracle.iot.sample.mydriveapp.prefs.Constants;
import com.oracle.iot.sample.mydriveapp.prefs.PrefUtils;

import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.abs;


public class VehicleTrackerService extends LifecycleService {

    AccelerationData<float[]> mAccelerationData;
    GPSData gpsData;
    CargoData mCargoData;
    private volatile float[] mAcceleration;
    private volatile float[] mCargoDataValues;
    private double mSpeed; //In KM/H
    private Location mLocation;
    private Location mStartLocation, mEndLocation = null;
    private long mTripStartTime, mEngineONDuration, mTimeBetweenLocationPings = 0; //mEngineONDuration in seconds
    private double mDistanceBetweenLocationPings = 0;
    private double mDistanceSinceEngineON = 0; //In KM
    private double mOdometerValue; //In KM
    private boolean overSpeedAlert = false;
    private Location overSpeedStartLocation = new Location ("overspeedingstart");
    private boolean idlingAlert = false;
    private long idlingStartTime;

    //Threshold preference settings
    double mManeuveringAccelerationThreshold, mOverspeedingThreshold, mIdlingTimeThreshold;
    int mOrientationPref = 0;

    private Timer timer;
    private TimerTask timerTask;
    public static boolean IS_SERVICE_RUNNING = false;

    public static final String ACTION_LOCATION_BROADCAST = "LocationBroadcast";
    public static final String ACTION_ACCELERATION_BROADCAST = "AccelerationBroadcast";
    public static final String ACTION_CARGODATA_BROADCAST = "CargoDataBroadcast";
    public static final String ACTION_ALERT = "DriverAlertBroadcast";
    public static final String EXTRA_ACCELVALUES = "accel_values";
    public static final String EXTRA_CARGODATAVALUES = "cargo_values";
    public static final String EXTRA_LOCATIONVALUES = "location_values";
    public static final String EXTRA_ALERTTYPE = "alert_type";
    public static final String EXTRA_ALERTVALUE = "alert_value";

    private OracleIoTCloudPublisher mOracleIoTCloudPublisher;

    public static final String EVENT_HARSHBRAKING = "harshBraking";
    public static final String EVENT_HARSHACCLERATION = "harshAccleration";
    public static final String EVENT_OVERSPEEDING = "overSpeeding";
    public static final String EVENT_OVERIDLING = "overIdling";
    public static final String EVENT_HARSHCORNERING = "harshCornering";

    private long latestBrakingTimestamp, latestAccleratingTimestamp, latestCorneringTimestamp = 0;
    private long previousBrakingTimestamp, previousAccleratingTimestamp, previousCorneringTimestamp = 0;

    public VehicleTrackerService() {
        super();
        mAcceleration = new float[3];
    }
    public VehicleTrackerService(Context applicationContext) {
        super();
        mAcceleration = new float[3];
    }

    @Override
    public void onCreate(){
        super.onCreate();
        mAcceleration = new float [3];
        mCargoDataValues = new float [5];
        mLocation = new Location("Dummy");
        mOracleIoTCloudPublisher = new OracleIoTCloudPublisher(this);

        setupLocationUpdates();
        setupAccelerationUpdates();
        setupCargoDataUpdates();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        super.onBind(intent);
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if (intent.getAction().equals(Constants.ACTION.STARTFOREGROUND_ACTION)) {
            IS_SERVICE_RUNNING = true;
            showForegroundServiceNotification();
            readDrivingPreferences();
            startTimerForIoTCloudSync();
        } else if (intent.getAction().equals(Constants.ACTION.STOPFOREGROUND_ACTION)) {
            stopForeground(true);
            IS_SERVICE_RUNNING = false;
            stopSelf();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        PrefUtils.setOdometerValue(getApplicationContext(), Double.toString(mOdometerValue));
        gpsData.removeUpdates();
        mCargoData.removeUpdates();
        mOracleIoTCloudPublisher.cleanup();
        stopTimerTask();
    }

    public void onLocationChanged(Location newLoc) {
        mLocation = newLoc;
        calculateTripDistanceAndTime();
        broadcastUpdate(ACTION_LOCATION_BROADCAST);
        checkSpeedBehavior();
    }

    public void onAccelerationChanged(float[] accelerationValues) {
        synchronized (mAcceleration) {
            System.arraycopy(accelerationValues, 0, mAcceleration, 0, accelerationValues.length);
            broadcastUpdate(ACTION_ACCELERATION_BROADCAST);
            checkAccelerationBehavior();
        }
    }

    public void onCargoDataChanged(float[] values) {
        synchronized (mCargoDataValues) {
            System.arraycopy(values, 0, mCargoDataValues, 0, values.length);
            broadcastUpdate(ACTION_CARGODATA_BROADCAST);
        }
    }

    private void startTimerForIoTCloudSync() {
        //set a new Timer
        timer = new Timer();

        //initialize the TimerTask's job
        initializeTimerTask();

        //schedule the timer, to wake up every 10 second
        //timer.schedule(timerTask, 5000, 10000);

        int freqSeconds = PrefUtils.getIoTDataMessageFrequency(getApplicationContext());
        timer.schedule(timerTask, 5000, freqSeconds * 1000);

    }

    private void initializeTimerTask(){
        timerTask = new TimerTask() {
            boolean isSetupIoTCloud = false;
            public void run() {
                if(!isSetupIoTCloud){
                    isSetupIoTCloud = mOracleIoTCloudPublisher.setupIoTCloudConnect();
                }
                if(isSetupIoTCloud){
                    Properties vehicleMessageValues = new Properties();
                    vehicleMessageValues.put(Constants.SPEED_ATTRIBUTE, String.valueOf(mSpeed));
                    vehicleMessageValues.put(Constants.TIME_SINCE_ENGINE_START, String.valueOf(mEngineONDuration));
                    vehicleMessageValues.put(Constants.ODOMETER_VALUE, String.valueOf(mOdometerValue));
                    vehicleMessageValues.put(Constants.LATITUDE_ATTRIBUTE, String.valueOf(mLocation.getLatitude()));
                    vehicleMessageValues.put(Constants.LONGITUDE_ATTRIBUTE, String.valueOf(mLocation.getLongitude()));
                    mOracleIoTCloudPublisher.sendDeviceDataMessagesToCloud(Constants.IOT_VEHICLE_MESSAGE_TYPE, vehicleMessageValues);

                    Properties cargoMessageValues = new Properties();
                    cargoMessageValues.put(Constants.CARGO_TEMP_VALUE, String.valueOf(mCargoDataValues[0]));
                    cargoMessageValues.put(Constants.CARGO_HUMIDITY_VALUE, String.valueOf(mCargoDataValues[1]));
                    cargoMessageValues.put(Constants.CARGO_LIGHT_VALUE, String.valueOf(mCargoDataValues[2]));
                    cargoMessageValues.put(Constants.CARGO_PRESSURE_VALUE, String.valueOf(mCargoDataValues[3]));
                    cargoMessageValues.put(Constants.CARGO_PROXIMITY_VALUE, String.valueOf(mCargoDataValues[4]));
                    mOracleIoTCloudPublisher.sendDeviceDataMessagesToCloud(Constants.IOT_CARGO_MESSAGE_TYPE, cargoMessageValues);
                }
            }
        };
    }

    private void stopTimerTask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private void calculateTripDistanceAndTime(){
        if(mStartLocation == null) {
            mStartLocation = mLocation;
            mEndLocation = mLocation;
            mTripStartTime = System.currentTimeMillis();
        }
        else{
            mEndLocation = mLocation;
        }

        mDistanceBetweenLocationPings = (mStartLocation.distanceTo(mEndLocation))/1000; //distance in KM
        mTimeBetweenLocationPings = (mEndLocation.getTime() - mStartLocation.getTime())/1000; //time in sec

        /*
        if (!mLocation.hasSpeed()){
            if(mTimeBetweenLocationPings != 0){
                mSpeed = (mDistanceBetweenLocationPings/mTimeBetweenLocationPings)*3.6;
            }
        } else {
            mSpeed = mLocation.getSpeed()*3.6;
        }
        */
        mSpeed = mLocation.getSpeed()*3.6;

        mDistanceSinceEngineON = mDistanceSinceEngineON + mDistanceBetweenLocationPings;
        mStartLocation = mEndLocation;
        mOdometerValue = mOdometerValue + mDistanceBetweenLocationPings;

        mEngineONDuration = System.currentTimeMillis() - mTripStartTime;
        mEngineONDuration = TimeUnit.MILLISECONDS.toSeconds(mEngineONDuration);
    }

    private void broadcastUpdate(final String action, String... more ) {

        Intent intent = new Intent(action);

        switch(action){
            case ACTION_LOCATION_BROADCAST:
                double [] locValues = new double [6];
                locValues[0] = mSpeed;
                locValues[1] = mLocation.getLatitude();
                locValues[2] = mLocation.getLongitude();
                locValues[3] = mOdometerValue;
                locValues[4] = mDistanceSinceEngineON;
                locValues[5] = mEngineONDuration;
                intent.putExtra(EXTRA_LOCATIONVALUES, locValues);
                break;
            case ACTION_ACCELERATION_BROADCAST:
                intent.putExtra(EXTRA_ACCELVALUES, mAcceleration);
                break;
            case ACTION_CARGODATA_BROADCAST:
                intent.putExtra(EXTRA_CARGODATAVALUES, mCargoDataValues);
                break;
            case ACTION_ALERT :
                String alertType = (more.length < 1) ? "" : more[0];
                intent.putExtra(EXTRA_ALERTTYPE, alertType);
                break;
        }

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void setupAccelerationUpdates(){

        mAccelerationData = new AccelerationData(getApplicationContext());
        updateAccelerationConfiguration();
        mAccelerationData.observe(this, new Observer<float[]>() {
            @Override
            public void onChanged(@Nullable float[] accelValues) {
                onAccelerationChanged(accelValues);
            }
        });
    }

    private void setupLocationUpdates() {
        gpsData = new GPSData(this);
        gpsData.getLocation().observe(this, new Observer<Location>() {
            @Override
            public void onChanged(@Nullable Location location) {
                onLocationChanged(location);
            }
        });
        
        mOdometerValue = Double.valueOf(PrefUtils.getOdometerValue(getApplicationContext()));
    }

    private void setupCargoDataUpdates(){
        mCargoData = new CargoData(this);
        mCargoData.getLiveData().observe(this, new Observer<float[]>() {
            @Override
            public void onChanged(@Nullable float [] values) {
                onCargoDataChanged (values);
            }
        });
    }

    private void checkSpeedBehavior (){
        //Over idling Alert
        if (mSpeed < Constants.IDLING_SPEED){
            if (!idlingAlert){
                idlingAlert = true;
                idlingStartTime = mLocation.getTime();
            }
        } else{
            if (idlingAlert) {
                long idlingTime = mLocation.getTime() - idlingStartTime;
                //if (idlingTime > Constants.IDLING_TIME_LIMIT) {
                if (idlingTime > mIdlingTimeThreshold) {
                    mOracleIoTCloudPublisher.sendDeviceAlertMessagesToCloud(EVENT_OVERIDLING, idlingTime/1000);
                    broadcastUpdate(ACTION_ALERT, EVENT_OVERIDLING);
                }
                idlingAlert = false;
            }
        }

        //Over speeding Alert
        //if (mSpeed > Constants.SPEED_LIMIT){
        if (mSpeed > mOverspeedingThreshold){
            if (!overSpeedAlert){
                overSpeedAlert = true;
                overSpeedStartLocation = mLocation;
            }
        } else{
            if (overSpeedAlert) {
                double distanceInOverSpeed = (overSpeedStartLocation.distanceTo(mLocation))/1000; //Overspeeding distance in KM
                mOracleIoTCloudPublisher.sendDeviceAlertMessagesToCloud(EVENT_OVERSPEEDING, distanceInOverSpeed);
                broadcastUpdate(ACTION_ALERT, EVENT_OVERSPEEDING);
                overSpeedAlert = false;
            }
        }
    }

    private void checkAccelerationBehavior(){

        /*
        double totalAcceleration = Math.sqrt(Math.pow(mAcceleration[0], 2)
                + Math.pow(mAcceleration[1], 2)
                +  Math.pow(mAcceleration[2], 2));
        */
        if (mAcceleration[2] > mManeuveringAccelerationThreshold){
            latestBrakingTimestamp = System.currentTimeMillis();
            if (((latestBrakingTimestamp - previousBrakingTimestamp) < 3000) ||
                    ((latestBrakingTimestamp - previousAccleratingTimestamp) < 3000)){
                //drop the message
            } else {
                mOracleIoTCloudPublisher.sendDeviceAlertMessagesToCloud(EVENT_HARSHBRAKING, mAcceleration[2]);
                broadcastUpdate(ACTION_ALERT, EVENT_HARSHBRAKING);
            }
            previousBrakingTimestamp = latestBrakingTimestamp;
        }

        if (mAcceleration[2] < (mManeuveringAccelerationThreshold * -1)){
            latestAccleratingTimestamp = System.currentTimeMillis();
            if (((latestAccleratingTimestamp - previousAccleratingTimestamp) < 3000) ||
                    ((latestAccleratingTimestamp - previousBrakingTimestamp) < 3000)){
                //drop the message
            } else {
                mOracleIoTCloudPublisher.sendDeviceAlertMessagesToCloud(EVENT_HARSHACCLERATION, mAcceleration[2]);
                broadcastUpdate(ACTION_ALERT, EVENT_HARSHACCLERATION);
            }
            previousAccleratingTimestamp = latestAccleratingTimestamp;
        }

        float corneringAxis = mAcceleration[0];
        if(mOrientationPref == Constants.LANDSCAPE)
            corneringAxis = mAcceleration[1];

        if (abs(corneringAxis) > mManeuveringAccelerationThreshold){
            latestCorneringTimestamp = System.currentTimeMillis();
            if ((latestCorneringTimestamp - previousCorneringTimestamp) < 3000){
                //drop the message
            } else {
                mOracleIoTCloudPublisher.sendDeviceAlertMessagesToCloud(EVENT_HARSHCORNERING, corneringAxis);
                broadcastUpdate(ACTION_ALERT, EVENT_HARSHCORNERING);
            }
            previousCorneringTimestamp = latestCorneringTimestamp;
        }
    }

    private void readDrivingPreferences(){
        mManeuveringAccelerationThreshold = PrefUtils.getManeuveringAccelerationThreshold(getApplicationContext());
        mOverspeedingThreshold = PrefUtils.getSpeedingThreshold(getApplicationContext());
        mOverspeedingThreshold = PrefUtils.getSpeedingThreshold(getApplicationContext());
        mIdlingTimeThreshold = PrefUtils.getIdlingTimeThreshold(getApplicationContext()) * 60 * 1000; //minutes converted to milliseconds

        mOrientationPref = PrefUtils.getPhoneOrientationPrefs(getApplicationContext());
    }

    private void updateAccelerationConfiguration() {
        mAccelerationData.setSensorFrequency(SensorManager.SENSOR_DELAY_NORMAL);
        mAccelerationData.setAxisInverted(false);
        mAccelerationData.enableLinearAcceleration(true);
        mAccelerationData.setLinearAccelerationTimeConstant(0.5f);
        mAccelerationData.enableLowPassFiltering(true);
        mAccelerationData.setLowPassFilterSmoothingTimeConstant(0.5f);
    }

    private void showForegroundServiceNotification() {
        Intent notificationIntent = new Intent(this, HomeActivity.class);
        notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        Intent stopIntent = new Intent(this, VehicleTrackerService.class);
        stopIntent.setAction(Constants.ACTION.STOPFOREGROUND_ACTION);
        PendingIntent pStopIntent = PendingIntent.getService(this, 0,
                stopIntent, 0);

        Bitmap icon = BitmapFactory.decodeResource(getResources(),
                R.drawable.iot_fm);

        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("MyDrive App")
                .setTicker("MyDrive App")
                .setContentText("Vehicle Tracker Service is Running")
                .setSmallIcon(R.drawable.iot_fm)
                //.setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .addAction(android.R.drawable.ic_lock_power_off, "Stop Service",
                        pStopIntent).build();

        startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, notification);

    }
}

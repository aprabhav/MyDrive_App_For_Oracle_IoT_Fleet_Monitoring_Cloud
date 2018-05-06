package com.oracle.iot.sample.mydriveapp.datalogger;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class DataLoggerManager implements Runnable {
    private static final String tag = DataLoggerManager.class.getSimpleName();

    public final static String DEFAULT_APPLICATION_DIRECTORY = "MyDrive";
    private final static long THREAD_SLEEP_TIME = 20;
    public final static String FILE_NAME_SEPARATOR = "-";

    // boolean to indicate if the data should be written to a file.
    private volatile boolean logData = false;

    // Log output time stamp
    protected long logTime = 0;
    private ArrayList<String> dataHeaders;
    private ArrayList<String> dataValues;
    private DataLoggerInterface dataLogger;
    private Context context;
    private volatile ArrayList<String> acceleration;
    private volatile ArrayList<String> location;
    private volatile ArrayList<String> cargoData;
    private volatile ArrayList<String> alerts;
    private Thread thread;

    public DataLoggerManager(Context context) {
        this.context = context;
        dataHeaders = getHeaders();
        //alertHeaders = getAlertHeaders();
        dataValues = new ArrayList<>();
        acceleration = new ArrayList<>();
        location = new ArrayList<>();
        cargoData = new ArrayList<>();
        alerts = new ArrayList<>();
    }

    @Override
    public void run() {
        while (logData && !Thread.currentThread().isInterrupted()) {
            // Check if the row is filled and ready to be written to the
            // log.
            logData();

            try {
                Thread.sleep(THREAD_SLEEP_TIME);
            } catch (InterruptedException e) {
                // very important to ensure the thread is killed
                Log.d("MyDrive", "Logger Thread Interrupted");
                Thread.currentThread().interrupt();
            }
        }

        // very important to ensure the thread is killed
        Thread.currentThread().interrupt();
    }


    public void startDataLog() throws IllegalStateException {
        if (!logData) {
            logData = true;
            logTime = System.currentTimeMillis();
            dataLogger = new CsvDataLogger(context, getFile(this.getFilePath(), this.getFileName()));
            dataLogger.setHeaders(dataHeaders);
            thread = new Thread(this);
            thread.start();
        } else {
            throw new IllegalStateException("Logger is already started!");
        }
    }

    public void stopDataLog() throws IllegalStateException {
        if (logData) {
            logData = false;
            thread.interrupt();
            thread = null;
            dataLogger.writeToFile();
        }
    }

    public void setAcceleration(float[] newAccelValues) {
        synchronized (acceleration) {
            acceleration.clear();
            for (int i = 0; i < 3; i++) {
                acceleration.add(String.valueOf(newAccelValues[i]));
            }
        }
    }

    public void setLocation(double[] newLocValues) {
        synchronized (location) {
            location.clear();
            for (int i = 0; i < 3; i++) {
                location.add(String.valueOf(newLocValues[i]));
            }
        }
    }

    public void setCargoData(float[] newCargoData) {
        synchronized (cargoData) {
            cargoData.clear();
            for (int i = 0; i < 5; i++) {
                cargoData.add(String.valueOf(newCargoData[i]));
            }
        }
    }

    public void setAlert(String type, double value){
        synchronized (alerts) {
            alerts.clear();
            alerts.add(type + " Alert");
            alerts.add(String.valueOf(value));
        }
    }

    private void logData() {
        dataValues.clear();
        //dataValues.add(String.valueOf((System.currentTimeMillis() - logTime) / 1000.0f));
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy'T'HH:mm:ss.SSS");
        String timeStamp = sdf.format(new Date(System.currentTimeMillis()));
        dataValues.add(timeStamp);

        if(!acceleration.isEmpty()){
            synchronized (acceleration) {
                dataValues.addAll(acceleration);
            }
        }

        if(!location.isEmpty()){
            synchronized (location) {
                dataValues.addAll(location);
            }
        }

        if(!cargoData.isEmpty()){
            synchronized (cargoData) {
                dataValues.addAll(cargoData);
            }
        }

        dataLogger.addRow(dataValues);

        if(!alerts.isEmpty()){
            synchronized (alerts) {
                dataValues.clear();
                dataValues.add(timeStamp);
                dataValues.addAll(alerts);
                dataLogger.addRow(dataValues);
                alerts.clear();
            }
        }
    }

    private File getFile(String filePath, String fileName) {
        File dir = new File(filePath);

        if (!dir.exists()) {
            dir.mkdirs();
        }

        return new File(dir, fileName);
    }

    private String getFilePath() {
        return new StringBuilder().append(Environment.getExternalStorageDirectory()).append(File.separator).append
                (DEFAULT_APPLICATION_DIRECTORY).append(File.separator).toString();
    }

    private String getFileName() {
        Calendar c = Calendar.getInstance();

        return new StringBuilder().append(DEFAULT_APPLICATION_DIRECTORY).append(FILE_NAME_SEPARATOR)
                .append(c.get(Calendar.YEAR)).append(FILE_NAME_SEPARATOR).append(c.get(Calendar.MONTH) + 1).append
                        (FILE_NAME_SEPARATOR).
                        append(c.get(Calendar.DAY_OF_MONTH)).append(FILE_NAME_SEPARATOR).append(c.get(Calendar.HOUR))
                .append("-").append(c.get(Calendar.MINUTE)).append(FILE_NAME_SEPARATOR).append(c.get(Calendar.SECOND)
                ).append(".csv").toString();
    }

    private ArrayList<String> getHeaders() {
        ArrayList<String> headers = new ArrayList<>();

        headers.add("Timestamp");
        headers.add("X");
        headers.add("Y");
        headers.add("Z");
        headers.add("Speed");
        headers.add("Latitude");
        headers.add("Longitude");

        headers.add("Cargo Temperature");
        headers.add("Cargo Humidity");
        headers.add("Cargo Light");
        headers.add("Cargo Pressure");
        headers.add("Cargo Proximity");
        return headers;
    }
}

package com.oracle.iot.sample.mydriveapp.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.oracle.iot.sample.mydriveapp.R;
import com.oracle.iot.sample.mydriveapp.datalogger.DataLoggerManager;
import com.oracle.iot.sample.mydriveapp.prefs.Constants;
import com.oracle.iot.sample.mydriveapp.service.VehicleTrackerService;

public class HomeActivity extends AppCompatActivity  {
    private final int REQUEST_LOCATION = 222;
    private final static int WRITE_EXTERNAL_STORAGE_REQUEST = 1000;
    private DataLoggerManager dataLogger;
    private boolean logData = false;

    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Bundle extras = intent.getExtras();
            switch(action){
                case Constants.EXCEPTION_ERROR:
                    String messageString = extras.getString("message");
                    showExceptionAlert(messageString + "\n\n- Check Network Connection\n- Check if VPN is needed\n- Check Provisioning File Path/Pwd");
                    break;
                case VehicleTrackerService.ACTION_ACCELERATION_BROADCAST:
                    dataLogger.setAcceleration(extras.getFloatArray(VehicleTrackerService.EXTRA_ACCELVALUES));
                    break;
                case VehicleTrackerService.ACTION_LOCATION_BROADCAST:
                    dataLogger.setLocation(extras.getDoubleArray(VehicleTrackerService.EXTRA_LOCATIONVALUES));
                    break;
                case VehicleTrackerService.ACTION_CARGODATA_BROADCAST:
                    dataLogger.setCargoData(extras.getFloatArray(VehicleTrackerService.EXTRA_CARGODATAVALUES));
                    break;
                case VehicleTrackerService.ACTION_ALERT:
                    String alertType = extras.getString(VehicleTrackerService.EXTRA_ALERTTYPE);
                    dataLogger.setAlert(alertType, extras.getDouble(VehicleTrackerService.EXTRA_ALERTVALUE));
                    Toast.makeText(getApplicationContext(), alertType + " Alert", Toast.LENGTH_LONG).show();
                    break;

            }
        }
    };

    private static IntentFilter makeBroadcastFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.EXCEPTION_ERROR);
        intentFilter.addAction(VehicleTrackerService.ACTION_ACCELERATION_BROADCAST);
        intentFilter.addAction(VehicleTrackerService.ACTION_LOCATION_BROADCAST);
        intentFilter.addAction(VehicleTrackerService.ACTION_CARGODATA_BROADCAST);
        intentFilter.addAction(VehicleTrackerService.ACTION_ALERT);
        return intentFilter;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_home);

        android.support.v7.widget.Toolbar toolbar = findViewById(R.id.action_toolbar);
        //toolbar.setLogo(R.drawable.iot_fm);
        toolbar.setContentInsetStartWithNavigation(0);
        toolbar.setTitle("MyDrive");
        setSupportActionBar(toolbar);
        //getSupportActionBar().setIcon(R.drawable.iot_fm);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        requestLocationPermission();
        dataLogger = new DataLoggerManager(this);
        initButtonLogger();
        initStartButton();
        checkGPSSetting();

        /*
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit().remove(ConfigActivity.ODOMETER_VALUE).commit();
        */
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_home, menu);
        return true;
    }

    /**
     * Event Handling for Individual menu item selected Identify single menu
     * item by it's id
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            // Start the vector activity
            case R.id.action_help:
                //showHelpDialog();
                return true;
            case R.id.settings:
                Intent intent = new Intent(HomeActivity.this,
                        ConfigActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPause() {
        stopDataLog();
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, makeBroadcastFilter());
        //requestPermissions();

        final Button button = (Button)findViewById(R.id.button_start);
        if (VehicleTrackerService.IS_SERVICE_RUNNING){
            button.setText("Stop Service");
        } else {
            button.setText("Start Service");
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    // permission denied
                    finish();
                    return;
                }
                return;
            }
            case WRITE_EXTERNAL_STORAGE_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {

                }
            }

        }
    }

    private void initButtonLogger() {
        final Button button = this.findViewById(R.id.button_logger_mode);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(HomeActivity.this, Manifest.permission
                        .WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    if (!logData) {
                        button.setText("Stop Log");
                        startDataLog();
                    } else {
                        button.setText("Start Log");
                        stopDataLog();
                    }
                } else {
                    requestStoragePermission();
                }
            }
        });
    }

    private void startDataLog() {
        logData = true;
        dataLogger.startDataLog();
    }

    private void stopDataLog() {
        logData = false;
        dataLogger.stopDataLog();
    }

    private void checkGPSSetting(){
        LocationManager locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);

        // getting GPS status
        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!isGPSEnabled) {
            //Location setting is disabled, request permission to enable it
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Location Permission");
            builder.setMessage("MyDrive App needs GPS to be turned ON");
            builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent enableLocationIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    getApplicationContext().startActivity(enableLocationIntent);
                }
            });
            builder.setNegativeButton(android.R.string.no, null);
            builder.show();
        }
    }

    private void initStartButton() {
        final Button button = findViewById(R.id.button_start);

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Intent service = new Intent(HomeActivity.this, VehicleTrackerService.class);
                if (!VehicleTrackerService.IS_SERVICE_RUNNING) {
                    service.setAction(Constants.ACTION.STARTFOREGROUND_ACTION);
                    VehicleTrackerService.IS_SERVICE_RUNNING = true;
                    button.setText("Stop Service");
                } else {
                    service.setAction(Constants.ACTION.STOPFOREGROUND_ACTION);
                    VehicleTrackerService.IS_SERVICE_RUNNING = false;
                    button.setText("Start Service");
                }
                startService(service);
            }
        });
    }

    private void requestLocationPermission() {

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        }
    }

    private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE_REQUEST);
        }
    }

    private void showExceptionAlert(String msg){
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Something is wrong");
        //alertDialog.setIcon(R.drawable.error);
        alertDialog.setMessage(msg);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        alertDialog.show();
    }
}

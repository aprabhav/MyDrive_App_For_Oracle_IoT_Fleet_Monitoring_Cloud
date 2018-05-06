package com.oracle.iot.sample.mydriveapp.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.oracle.iot.sample.mydriveapp.R;
import com.oracle.iot.sample.mydriveapp.service.VehicleTrackerService;
import java.util.Locale;

public class SpeedBarFragment extends Fragment {

    // Text views for real-time output
    private TextView textViewSpeed;
    private TextView textViewLat;
    private TextView textViewLong;
    private TextView textViewOdometer;
    private TextView textViewDistance;
    private TextView textViewEngineTime;

    private Handler handler;
    private Runnable runnable;

    private double[] location = new double[6];

    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        LocalBroadcastManager.getInstance(this.getActivity()).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        System.arraycopy(intent.getDoubleArrayExtra(VehicleTrackerService.EXTRA_LOCATIONVALUES), 0,
                                location, 0, intent.getDoubleArrayExtra(VehicleTrackerService.EXTRA_LOCATIONVALUES).length);
                    }
                }, new IntentFilter(VehicleTrackerService.ACTION_LOCATION_BROADCAST)
        );

        handler = new Handler();
        runnable = new Runnable()
        {
            @Override
            public void run()
            {
                handler.postDelayed(this, 20);
                updateSpeedText();
            }
        };

        location = new double [6];
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_speed_bar, container, false);

        textViewSpeed = view.findViewById(R.id.value_speed);
        textViewLat = view.findViewById(R.id.value_lat);
        textViewLong = view.findViewById(R.id.value_long);
        textViewOdometer = view.findViewById(R.id.value_odometer);
        textViewDistance = view.findViewById(R.id.value_distance);
        textViewEngineTime = view.findViewById(R.id.value_engineTime);

        return view;
    }

    @Override
    public void onPause() {
        handler.removeCallbacks(runnable);
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        handler.post(runnable);
    }

    private void updateSpeedText()
    {
        if(location.length == 6) {
            // Update the speed data
            textViewSpeed.setText(String.format(Locale.US, "%.2f", location[0]));
            textViewLat.setText(String.format(Locale.US, "%.4f", location[1]));
            textViewLong.setText(String.format(Locale.US, "%.4f", location[2]));
            textViewOdometer.setText(String.format(Locale.US, "%.2f", location[3]));
            textViewDistance.setText(String.format(Locale.US, "%.2f", location[4]));
            textViewEngineTime.setText(String.format(Locale.US, "%.2f", location[5]/60));
        }
    }
}

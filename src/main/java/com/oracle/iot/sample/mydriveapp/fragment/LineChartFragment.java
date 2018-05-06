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

import com.github.mikephil.charting.charts.LineChart;
import com.oracle.iot.sample.mydriveapp.R;
import com.oracle.iot.sample.mydriveapp.plot.DynamicChart;
import com.oracle.iot.sample.mydriveapp.service.VehicleTrackerService;

public class LineChartFragment extends Fragment {

    // Graph plot for the UI outputs
    private DynamicChart dynamicChart;

    private Handler handler;
    private Runnable runnable;

    private float[] acceleration;

    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        LocalBroadcastManager.getInstance(this.getActivity()).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        System.arraycopy(intent.getFloatArrayExtra(VehicleTrackerService.EXTRA_ACCELVALUES), 0,
                                acceleration, 0, intent.getFloatArrayExtra(VehicleTrackerService.EXTRA_ACCELVALUES).length);
                    }
                }, new IntentFilter(VehicleTrackerService.ACTION_ACCELERATION_BROADCAST)
        );

        handler = new Handler();
        runnable = new Runnable()
        {
            @Override
            public void run()
            {
                handler.postDelayed(this, 20);
                dynamicChart.setAcceleration(acceleration);
            }
        };

        acceleration = new float[3];
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_line_chart, container, false);

        // Create the graph plot
        LineChart plot = (LineChart) view.findViewById(R.id.line_chart);
        dynamicChart = new DynamicChart(getContext(), plot);

        return view;
    }

    @Override
    public void onPause() {
        handler.removeCallbacks(runnable);
        dynamicChart.onStopPlot();
        dynamicChart.onPause();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        handler.post(runnable);
        dynamicChart.onResume();
        dynamicChart.onStartPlot();
    }
}

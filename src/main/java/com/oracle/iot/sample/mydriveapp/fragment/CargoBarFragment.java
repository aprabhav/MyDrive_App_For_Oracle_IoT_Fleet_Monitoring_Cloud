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

public class CargoBarFragment extends Fragment {

    // Text views for real-time output
    private TextView textViewCargo;
    private Handler handler;
    private Runnable runnable;

    private float[] mCargoData = new float[5];

    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        LocalBroadcastManager.getInstance(this.getActivity()).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        System.arraycopy(intent.getFloatArrayExtra(VehicleTrackerService.EXTRA_CARGODATAVALUES), 0,
                                mCargoData, 0, intent.getFloatArrayExtra(VehicleTrackerService.EXTRA_CARGODATAVALUES).length);
                    }
                }, new IntentFilter(VehicleTrackerService.ACTION_CARGODATA_BROADCAST)
        );

        handler = new Handler();
        runnable = new Runnable()
        {
            @Override
            public void run()
            {
                handler.postDelayed(this, 20);
                updateCargoText();
            }
        };

        mCargoData = new float [5];
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cargo_bar, container, false);

        textViewCargo = view.findViewById(R.id.value_cargo);
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

    private void updateCargoText()
    {
        if(mCargoData.length == 5) {
            // Update the speed data
            String cargoDataValues = ("t: "+ String.format(Locale.US, "%.0f", mCargoData[0])) + "°C  " +
                    ("h: "+ String.format(Locale.US, "%.0f", mCargoData[1])) + "%  " +
                    ("l: "+ String.format(Locale.US, "%.0f", mCargoData[2])) + "lx  " +
                    ("p: "+ String.format(Locale.US, "%.2f", mCargoData[3])) + "mbar  " +
                    ("px: "+ String.format(Locale.US, "%.0f", mCargoData[4]));
            textViewCargo.setText(cargoDataValues);
        }
    }
}


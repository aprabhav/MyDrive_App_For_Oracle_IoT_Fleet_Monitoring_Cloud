package com.oracle.iot.sample.mydriveapp.service.Filters;

public class LinearAccelerationFilter {

    private float[] output = new float[] {0, 0, 0};
    private Filter gravityFilter;

    public LinearAccelerationFilter() {
        gravityFilter = new LowPassFilter();
    }

    public float[] filter(float[] values) {

        float[] gravity = gravityFilter.filter(values);

        // Determine the linear acceleration
        output[0] = values[0] - gravity[0];
        output[1] = values[1] - gravity[1];
        output[2] = values[2] - gravity[2];

        return output;
    }

    public void setTimeConstant(float timeConstant) {
        gravityFilter.setTimeConstant(timeConstant);
    }

    public float[] getGravity() {
        return gravityFilter.getOutput();
    }

    public void reset() {
        gravityFilter.reset();
    }
}
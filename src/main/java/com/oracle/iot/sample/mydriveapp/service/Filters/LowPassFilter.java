package com.oracle.iot.sample.mydriveapp.service.Filters;

public class LowPassFilter implements Filter
{
    public static float DEFAULT_TIME_CONSTANT = 0.18f;

    private float timeConstant;
    private long startTime;
    private long timestamp;
    private int count;

    private static final String tag = LowPassFilter.class.getSimpleName();

    // Gravity and linear accelerations components for the
    // Wikipedia low-pass filter
    private float[] output;

    public LowPassFilter() {
        this(DEFAULT_TIME_CONSTANT);
    }

    public LowPassFilter(float timeConstant) {
        this.timeConstant = timeConstant;
        reset();
    }

    /**
     * Add a sample.
     *
     * @param values
     *            The acceleration data. A 1x3 matrix containing the data from the X, Y and Z axis of the sensor
     *            noting that order is arbitrary.
     * @return Returns the output of the filter.
     */
    public float[] filter(float[] values)
    {
        // Initialize the start time.
        if (startTime == 0)
        {
            startTime = System.nanoTime();
        }

        timestamp = System.nanoTime();

        // Find the sample period (between updates) and convert from
        // nanoseconds to seconds. Note that the sensor delivery rates can
        // individually vary by a relatively large time frame, so we use an
        // averaging technique with the number of sensor updates to
        // determine the delivery rate.
        float dt = 1 / (count++ / ((timestamp - startTime) / 1000000000.0f));

        float alpha = timeConstant / (timeConstant + dt);

        output[0] = alpha * output[0] + (1 - alpha) * values[0];
        output[1] = alpha * output[1] + (1 - alpha) * values[1];
        output[2] = alpha * output[2] + (1 - alpha) * values[2];

        return output;
    }

    public float[] getOutput() {
        return output;
    }

    public void setTimeConstant(float timeConstant)
    {
        this.timeConstant = timeConstant;
    }

    public void reset()
    {
        startTime = 0;
        timestamp = 0;
        count = 0;
        this.output = new float[]
                { 0, 0, 0 };

    }
}
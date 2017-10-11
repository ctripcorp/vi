package com.ctrip.framework.cs.metrics;

import java.util.Arrays;

/**
 * Created by jiang.j on 2016/8/18.
 */
public class MetricsStatsBuffer {
    private transient int count;
    private double mean;
    private double sumSquares;
    private double variance;
    private double stddev;
    private long min;
    private long max;
    private long total;

    private boolean needPercentiles = false;
    private transient final int MAXSIZE=800;
    private transient final int MINSIZE =50;
    private transient double[] percentiles;
    private transient double[] percentileValues;
    private transient int size = MINSIZE;
    private transient long[] values;

    /**
     * Create a circular buffer that will be used to record values and compute useful stats.
     *
     * @param percentiles Array of percentiles to compute. For example { 95.0, 99.0 }.
     *                    If no percentileValues are required pass a 0-sized array.
     */
    public MetricsStatsBuffer(double[] percentiles) {
        if(percentiles!=null && percentiles.length>0){
            needPercentiles = true;
            values = new long[size];
            this.percentiles = Arrays.copyOf(percentiles, percentiles.length);
            this.percentileValues = new double[percentiles.length];
        }
    }


    /**
     * drain dry current buffer
     */
    public MetricsSnapshot drain() {
        computeStats();
        MetricsSnapshot snapshot = new MetricsSnapshot();
        snapshot.total = total;
        snapshot.count = count;
        snapshot.max = max;
        snapshot.min = min;
        snapshot.stddev =stddev;
        if(needPercentiles) {
            snapshot.percentileValues = this.getPercentileValues();
        }

        if(needPercentiles) {
            for (int i = 0; i < percentileValues.length; ++i) {
                percentileValues[i] = 0.0;
            }

            if (this.count > this.size * 1.5) {
                this.size = (int) (this.count * 1.5);
                if(this.size> MAXSIZE) this.size = MAXSIZE;

                this.values = new long[this.size];
            } else if (this.count < this.size * 0.3) {
                this.size = (int) (this.size * 0.3);
                if(this.size<MINSIZE) this.size = MINSIZE;

                this.values = new long[this.size];
            } else {
                for (int i = 0; i < this.size; i++) {
                    this.values[i] = 0;
                }
            }
        }
        count = 0;
        total = 0L;
        mean = 0.0;
        variance = 0.0;
        stddev = 0.0;
        min = 0L;
        max = 0L;
        sumSquares = 0.0;

        return snapshot;
    }

    /**
     * Record a new value for this buffer.
     */
    public void record(long n) {
        if(needPercentiles) {
            values[count++ % size] = n;
        }else{
            count++;
            if(n > this.max){
                this.max = n;
            }
            if(n < this.min){
                this.min = n;
            }
        }
        total += n;
        sumSquares += n * n;
    }

    /**
     * Compute stats for the current set of values.
     */
    private void computeStats() {
        if (count == 0) {
            return;
        }

        int curSize = Math.min(count, size);
        mean = (double) total / count;
        variance = (sumSquares / curSize) - (mean * mean);
        stddev = Math.sqrt(variance);

        if(needPercentiles) {
            Arrays.sort(values, 0, curSize); // to compute percentileValues
            min = values[0];
            max = values[curSize - 1];
            computePercentiles(curSize);
        }
    }

    private void computePercentiles(int curSize) {
        for (int i = 0; i < percentiles.length; ++i) {
            percentileValues[i] = calcPercentile(curSize, percentiles[i]);
        }
    }

    private double calcPercentile(int curSize, double percent) {
        if (curSize == 0) {
            return 0.0;
        }
        if (curSize == 1) {
            return values[0];
        }

        /*
         * We use the definition from http://cnx.org/content/m10805/latest
         * modified for 0-indexed arrays.
         */
        final double rank = percent * curSize / 100.0; // SUPPRESS CHECKSTYLE MagicNumber
        final int ir = (int) Math.floor(rank);
        final int irNext = ir + 1;
        final double fr = rank - ir;
        if (irNext >= curSize) {
            return values[curSize - 1];
        } else if (fr == 0.0) {
            return values[ir];
        } else {
            // Interpolate between the two bounding values
            final double lower = values[ir];
            final double upper = values[irNext];
            return fr * (upper - lower) + lower;
        }
    }

    /**
     * Get the number of entries recorded.
     */
    public int getCount() {
        return count;
    }

    /**
     * Get the average of the values recorded.
     *
     * @return The average of the values recorded, or 0.0 if no values were recorded.
     */
    public double getMean() {
        return mean;
    }

    /**
     * Get the variance for the population of the recorded values present in our buffer.
     *
     * @return The variance.p of the values recorded, or 0.0 if no values were recorded.
     */
    public double getVariance() {
        return variance;
    }

    /**
     * Get the standard deviation for the population of the recorded values present in our buffer.
     *
     * @return The stddev.p of the values recorded, or 0.0 if no values were recorded.
     */
    public double getStdDev() {
        return stddev;
    }

    /**
     * Get the minimum of the values currently in our buffer.
     *
     * @return The min of the values recorded, or 0.0 if no values were recorded.
     */
    public long getMin() {
        return min;
    }

    /**
     * Get the max of the values currently in our buffer.
     *
     * @return The max of the values recorded, or 0.0 if no values were recorded.
     */
    public long getMax() {
        return max;
    }

    /**
     * Get the total sum of the values recorded.
     *
     * @return The sum of the values recorded, or 0.0 if no values were recorded.
     */
    public long getTotalTime() {
        return total;
    }

    public double[] getPercentileValues() {
        return Arrays.copyOf(percentileValues, percentileValues.length);
    }

    /**
     * Return the percentiles we will compute: For example: 95.0, 99.0.
     */
    public double[] getPercentiles() {
        return Arrays.copyOf(percentiles, percentiles.length);
    }

    /**
     * Return the value for the percentile given an index.
     * @param index If percentiles are [ 95.0, 99.0 ] index must be 0 or 1 to get the 95th
     *              or 99th percentile respectively.
     *
     * @return The value for the percentile requested.
     */
    public double getPercentileValueForIdx(int index) {
        return percentileValues[index];
    }

    public int getBufferSize(){
        return needPercentiles?size:0;
    }
}

package com.ctrip.framework.cornerstone.metrics;

/**
 * Created by jiang.j on 2016/12/30.
 */
public final class Metrics {

    public static final class VIAPI implements MetricDefine{

        @Override
        public String valueDescription() {
            return "cost time(ms)";
        }

        @Override
        public String valueTransFunc() {
            return "x/1000";
        }

        @Override
        public Class<? extends Enum> tags() {
            return Tags.class;
        }

        public enum Tags{
            Size,Path
        }


    }
    public static final String VIAPI="vi.api";
}

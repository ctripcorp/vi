package com.ctrip.framework.vi.asm;

import com.ctrip.framework.vi.metrics.Metrics;
import com.ctrip.framework.vi.metrics.MetricsCollector;

import java.io.*;
import java.util.Scanner;

/**
 * Created by jiang.j on 2017/5/2.
 */
public class MyTest {

    public static void main(String[] args) throws IOException {
        System.out.println("get some");
        File file = new File("/opt/status/server.status");
        Scanner s = new Scanner(new FileInputStream(file),"UTF-8").useDelimiter("\\A");
        System.out.println(s.hasNext()?s.next():"");


    }
}

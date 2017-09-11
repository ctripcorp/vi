package com.ctrip.framework.vi;

import com.ctrip.framework.vi.analyzer.GCLogAnalyzer;
import com.ctrip.framework.vi.metrics.MetricsValueType;
import com.google.gson.Gson;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;

import static junit.framework.TestCase.assertEquals;

/**
 * Created by jiang.j on 2017/5/3.
 */
public class GCLogAnalyzerTest {

    @Test
    public void test() throws IOException {
        short n = -123;
        System.out.println(Short.reverseBytes(n));

    }
    @Test
    public void testParseFullGCInfo1(){
        String data="2017-05-03T10:53:06.881+0800: 2174.037: [GC[YG occupancy: 259391 K (471872 K)]2017-05-03T10:53:06.882+0800: 2174.037: [Rescan (parallel) , 0.0415120 secs]2017-05-03T10:53:06.923+0800: 2174.079: [weak refs processing, 0.0394110 secs]2017-05-03T10:53:06.963+0800: 2174.118: [class unloading, 0.0196060 secs]2017-05-03T10:53:06.983+0800: 2174.138: [scrub symbol table, 0.0143320 secs]2017-05-03T10:53:06.997+0800: 2174.152: [scrub string table, 0.0018640 secs] [1 CMS-remark: 395107K(523264K)] 654498K(995136K), 0.1266980 secs] [Times: user=0.38 sys=0.00, real=0.13 secs]";
        InputStream is = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));
        String result = GCLogAnalyzer.parseToJson(is);
        Gson gson = new Gson();
        ArrayList<ArrayList<Object>> analyzeResult = new ArrayList<ArrayList<Object>>();
         analyzeResult = gson.fromJson(result, analyzeResult.getClass());
        assertEquals(1, analyzeResult.size());
        ArrayList<Object> detail = analyzeResult.get(0);
        assertEquals(9, detail.size());
        assertEquals(true,detail.get(1));
        assertEquals(0.126698,detail.get(2));
        assertEquals(995136.0,detail.get(8));
    }

    @Test
    public void testParseFullGCInfo2(){
        String data="2017-05-03T12:34:22.733+0800: 8249.888: [Full GC2017-05-03T12:34:22.734+0800: 8249.890: [CMS: 347271K->337480K(603532K), 1.5611760 secs] 465044K->337480K(1075404K), [CMS Perm : 262143K->258016K(262144K)], 1.5635530 secs] [Times: user=1.56 sys=0.01, real=1.57 secs]";
        InputStream is = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));
        String result = GCLogAnalyzer.parseToJson(is);
        Gson gson = new Gson();
        ArrayList<ArrayList<Object>> analyzeResult = new ArrayList<ArrayList<Object>>();
        analyzeResult = gson.fromJson(result, analyzeResult.getClass());
        assertEquals(1, analyzeResult.size());
        ArrayList<Object> detail = analyzeResult.get(0);
        assertEquals(12, detail.size());
        assertEquals(true,detail.get(1));
        assertEquals(1.5635530,detail.get(2));
        assertEquals(1075404.0, detail.get(8));
        assertEquals(262144.0, detail.get(11));
    }

    @Test
    public void testParseYoungGCInfo(){
        String data="2017-05-03T10:16:55.628+0800: 2.784: [GC2017-05-03T10:16:55.628+0800: 2.784: [ParNew: 419456K->52415K(471872K), 0.0626010 secs] 419456K->56081K(995136K), 0.0627660 secs] [Times: user=0.14 sys=0.06, real=0.06 secs]";
        InputStream is = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));
        String result = GCLogAnalyzer.parseToJson(is);
        Gson gson = new Gson();
        ArrayList<ArrayList<Object>> analyzeResult = new ArrayList<ArrayList<Object>>();
        analyzeResult = gson.fromJson(result, analyzeResult.getClass());
        assertEquals(1, analyzeResult.size());
        ArrayList<Object> detail = analyzeResult.get(0);
        assertEquals(9, detail.size());
        assertEquals(false,detail.get(1));
        assertEquals(0.0627660,detail.get(2));
        assertEquals(419456.0,detail.get(3));
        assertEquals(995136.0,detail.get(8));
    }
}

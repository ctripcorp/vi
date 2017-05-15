package com.ctrip.framework.cornerstone.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by jiang.j on 2016/7/27.
 */
public class LinuxInfoUtil {

    static final String MEMAVAKEY="MemAvailable";
    static final String MEMFRESSKEY="MemFree";
    static final String MEMBUFKEY="Buffers";
    static final String MEMCACHKEY="Cached";
    public static int getAvailableMemKB(Map<String,String> meminfos) throws IOException {

        if(meminfos==null)
            meminfos = getMemInfo();
        int avaiMem = 0;
        Pattern p = Pattern.compile("\\d+");

        Matcher m;
        if(meminfos.containsKey(MEMAVAKEY)){
            m = p.matcher(meminfos.get(MEMAVAKEY));
            if(m.find())
                avaiMem = Integer.parseInt(m.group());
        }else{
            int free = 0;
            m = p.matcher(meminfos.get(MEMFRESSKEY));
            if(m.find())
                free = Integer.parseInt(m.group());
            int buffer = 0;
            m = p.matcher(meminfos.get(MEMBUFKEY));
            if(m.find())
                buffer = Integer.parseInt(m.group());
            int cache = 0;
            m = p.matcher(meminfos.get(MEMCACHKEY));
            if(m.find())
                cache = Integer.parseInt(m.group());

            avaiMem = free + cache +buffer;
        }

        return avaiMem;
    }
    public static int getAvailableMemKB() throws IOException {

        return getAvailableMemKB(null);
    }

    public static Map<String,String> getMemInfo() throws IOException {

        Map<String,String> rtn = new HashMap<>();
        Path meminfoPath = Paths.get("/proc/meminfo");
        if (Files.exists(meminfoPath)) {
            List<String> lines = Files.readAllLines(meminfoPath, Charset.defaultCharset());
            for(String line :lines){
                String[] parts = line.split(":");
                if(parts.length>1) {
                    rtn.put(parts[0], parts[1].trim());
                }

            }


        }
        return rtn;
    }

    public static String getOSInfo() throws IOException {

        String rtn=null;
        Path fpath = Paths.get("/etc/redhat-release");

        if (!Files.exists(fpath)) {
            fpath = Paths.get("/etc/issue");
        }

        if (Files.exists(fpath)) {
            List<String> lines = Files.readAllLines(fpath, Charset.defaultCharset());
            if(lines.size()>0) {
                rtn = lines.get(0);
            }

        }
        return rtn;
    }

    public static String getCpuLoadAverage() throws IOException {
        Path loadavgPath = Paths.get("/proc/loadavg");
        if (Files.exists(loadavgPath)) {
            return IOUtils.readAll(new FileInputStream(loadavgPath.toFile()));
        }
        return null;
    }
    public static Object[] parseIp(String ip){
        Object[] rtn= new Object[2];
        if(ip.length()>20){
            ip = ip.substring(ip.length()-13);
        }
        rtn[0]=Integer.valueOf(ip.substring(6,8),16)+"."+
                Integer.valueOf(ip.substring(4,6),16)+"."+
                Integer.valueOf(ip.substring(2,4),16)+"."+
                Integer.valueOf(ip.substring(0,2),16);

        rtn[1]=Integer.valueOf(ip.substring(9),16);
        return rtn;
    }

}

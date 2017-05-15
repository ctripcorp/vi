package com.ctrip.framework.cornerstone.localLog;


import com.ctrip.framework.cornerstone.configuration.ConfigurationManager;
import com.ctrip.framework.cornerstone.util.IOUtils;
import com.ctrip.framework.cornerstone.util.Tools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URLDecoder;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

/**
 * Created by jiang.j on 2016/5/3.
 */
public final class LocalLogManager {

    public static class FileLogInfo {
        public String name;
        public long modifiedTime;
        public long size;
    }

    private static final String[] LOGEXTS = new String[]{".log",".txt",".out",".gz"};
    private static final Logger logger = LoggerFactory.getLogger(LocalLogManager.class);
    private static int MB = 1024*1024;

    private static String getLogPath() {

        String logPath = System.getProperty("catalina.home") + "/logs/";
        try {
            String path = ConfigurationManager.getConfigInstance().getString("app.localLog.path");
            if(path!=null && path.length()>5){
                logPath = path;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return logPath;
    }
    public static List<FileLogInfo> getLogList(){

        final File file = new File(getLogPath());
        File[] allFiles = file.listFiles();
        List<FileLogInfo> logs = new ArrayList<>(allFiles.length);


        for(final File f:allFiles){
            String fileName = f.getName().toLowerCase();
            boolean canView =false;
            for(String ext:LOGEXTS){
                if(fileName.endsWith(ext)){
                   canView = true;
                    break;
                }
            }

            if(canView) {

                FileLogInfo info = new FileLogInfo();
                if(fileName.endsWith(".gz")) {
                    try(RandomAccessFile raf = new RandomAccessFile(f, "r")) {
                        raf.seek(raf.length() - 4);
                        int b4 = raf.read();
                        int b3 = raf.read();
                        int b2 = raf.read();
                        int b1 = raf.read();
                        int val = (b1 << 24) | (b2 << 16) + (b3 << 8) + b4;
                        info.name = fileName +"|size:"+ Tools.byteToKB(f.length());
                        info.size = val;
                    }catch (Throwable e){
                        logger.warn("read gz log failed!",e);
                    }
                }else {
                    info.name = fileName;
                    info.size = f.length();
                }
                info.modifiedTime = f.lastModified();
                if (info.size > 0)
                    logs.add(info);
            }
        }
        return logs;
    }

    public static String getLogConent(String name,int partitionIndex,String encoding) throws IOException {


        int sIndex = name.indexOf("|");
        boolean isGZ = false;
        if(sIndex>0){
            name = name.substring(0,sIndex);
            isGZ = true;
        }

        String logDir = getLogPath();
        Path path = Paths.get(logDir, name);
        String defaultCharset = System.getProperty("sun.jnu.encoding");
        Charset fileCharset  = defaultCharset!=null?Charset.forName(defaultCharset):Charset.defaultCharset();


       if(encoding!=null&&encoding.trim().length()>0){
           fileCharset = Charset.forName(encoding);
       }
        if(!Files.exists(path)){
            return new String(path.toString().getBytes("ISO8859-1"),"UTF-8") + " is not exists.";
        }

        byte[] raw = new byte[]{};
        if(!isGZ) {
            try (SeekableByteChannel sbc = Files.newByteChannel(path)) {
                if (sbc.size() < 1024 * 1024) {
                    raw = Files.readAllBytes(path);
                } else {
                    if (partitionIndex > 0) {
                        raw = IOUtils.partitionRead(path, 1024 * 1024, partitionIndex);
                    } else {
                        raw = IOUtils.reverseRead(path, fileCharset, 1024 * 1024);
                    }
                }

            }
        }else {
            File f = path.toFile();
            int fLen=0;
            int readCount = 0;
            int skipCount = 0;
            try(RandomAccessFile raf = new RandomAccessFile(f, "r")) {
                raf.seek(4);
                logger.info(Integer.toBinaryString(raf.read()));
                raf.seek(raf.length() - 4);
                int b4 = raf.read();
                int b3 = raf.read();
                int b2 = raf.read();
                int b1 = raf.read();
                fLen = (b1 << 24) | (b2 << 16) + (b3 << 8) + b4;

                if(fLen<MB) {
                    readCount = fLen;
                }else {
                    readCount = MB;
                    int totalCount =(int) Math.ceil((fLen + 0.00) / MB);
                    if(partitionIndex<0){
                        partitionIndex = totalCount;
                    }
                    skipCount = (partitionIndex-1)*MB;

                }
            }catch (Throwable e){
                logger.warn("read gz log ("+path+") size failed!");
            }
            try(GZIPInputStream gzipInputStream = new GZIPInputStream(new FileInputStream(f),readCount)){
                raw = new byte[readCount];
                gzipInputStream.skip(skipCount);
                gzipInputStream.read(raw, 0, readCount);
            }catch (Throwable e){
                logger.warn("read gz log("+path+") content failed!",e);
            }
        }
        return new String(raw, fileCharset);

    }

    public static File  getLogFile(String name){

        String logDir = getLogPath();
        Path path = Paths.get(logDir, name);
        return  path.toFile();
    }
}

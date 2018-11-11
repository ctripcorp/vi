package com.ctrip.framework.cs.component.defaultComponents;

import com.ctrip.framework.cs.SysKeys;
import com.ctrip.framework.cs.annotation.ComponentStatus;
import com.ctrip.framework.cs.component.Refreshable;
import com.ctrip.framework.cs.enterprise.EnFactory;
import com.ctrip.framework.cs.util.LinuxInfoUtil;
import com.ctrip.framework.cs.annotation.FieldInfo;

import java.io.*;
import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.lang.reflect.Method;


/**
 * Created by jiang.j on 2016/3/28.
 */
@ComponentStatus(id="vi.hostinfo",name="Host info",description = "应用基本信息",singleton = true)
public class HostInfo implements Refreshable{

    @FieldInfo(name = "Host Name",description = "主机名")
    protected  final String HostName;

    @FieldInfo(name = "IP",description = "")
    protected  final String IP;

    @FieldInfo(name = "Container",description = "容器")
    protected String Container;
    @FieldInfo(name = "Available Memory",description = "可用内存(仅linux下有值)",type = FieldInfo.FieldType.Bytes)
    protected long availableMem;
    @FieldInfo(name = "Physical Memory Avaliable",description = "可用物理内存",type = FieldInfo.FieldType.Bytes)
    protected long PhysicalMemoryAvaliable;
    @FieldInfo(name = "Physical Memory Total",description = "总物理内存",type = FieldInfo.FieldType.Bytes)
    protected long PhysicalMemoryTotal;
    @FieldInfo(name = "System CPU Load",description = "cpu负载")
    protected  float systemCpuLoad;
    @FieldInfo(name = "CPU Load Averages",description = "cpu 平均负载(仅linux下有值，1分钟、5分钟、15分钟、活动进程数/总进程数、当前运行进程ID)")
    protected String cpuLoadAverages="N/A";
    @FieldInfo(name = "Process Cpu Time",description = "进程cpu时间",type = FieldInfo.FieldType.Number)
    protected long processCpuTime;
    @FieldInfo(name = "Process Cpu Load",description = "进程cpu load",type = FieldInfo.FieldType.Number)
    protected double processCpuLoad;
    @FieldInfo(name = "Disk Avaliable",description = "可用磁盘空间",type = FieldInfo.FieldType.Bytes)
    protected long DiskAvaliable;
    @FieldInfo(name = "Disk Total",description = "总磁盘空间",type = FieldInfo.FieldType.Bytes)
    protected  long DiskTotal;
    @FieldInfo(name = "Open File Descriptor Count",description = "打开的文件描述符数目(仅类unix系统有值)")
    protected  long openFileDescriptorCount;
    @FieldInfo(name = "Max File Descriptor Count",description = "最大文件描述符数目(仅类unix系统有值)")
    protected  long maxFileDescriptorCount;
    @FieldInfo(name = "OS",description = "操作系统")
    protected  final String OSName;
    @FieldInfo(name = "Linux OS",description = "Linux操作系统")
    protected  String linuxOS;
    @FieldInfo(name = "Java Version",description = "Java 版本")
    protected  final String JavaVersion;
    @FieldInfo(name = "Number of processors",description = "cpu核数")
    protected  String numberOfProcessors;

    @FieldInfo(name = "ENV",description = "服务器所在环境")
    protected  final String env;

    protected transient Logger logger = LoggerFactory.getLogger(getClass());
    private static transient boolean _isLinux;
    private static transient boolean _isTomcat;
    static {

        if ("linux".equalsIgnoreCase(System.getProperty("os.name"))) {
            _isLinux = true;
        }
        if(System.getProperty("catalina.home") != null){
            _isTomcat = true;
        }
    }


    private long invoke(OperatingSystemMXBean os, String name) {
        try {
            final Method method = os.getClass().getDeclaredMethod(name);
            method.setAccessible(true);
            return (Long) method.invoke(os);
        } catch (Throwable e) {
            return -1;
        }
    }

    public static boolean isLinux(){

        return _isLinux;
    }

    public static boolean isTomcat(){

        return _isTomcat;
    }

    public HostInfo() {

        OperatingSystemMXBean bean= (OperatingSystemMXBean ) ManagementFactory.getOperatingSystemMXBean();

        OSName = bean.getName() + " " + bean.getVersion()+"/"+bean.getArch();
        try {
            Container = System.getProperty(SysKeys.ServerInfo);
        }catch (Throwable e){
            logger.info("No servlet api");
        }
        HostName= EnFactory.getEnHost().getHostName();
        IP = EnFactory.getEnHost().getHostAddress();
        JavaVersion = System.getProperty("java.version");
        numberOfProcessors = System.getenv("NUMBER_OF_PROCESSORS");

        if(numberOfProcessors == null){
            numberOfProcessors = String.valueOf(bean.getAvailableProcessors());
        }

        env = EnFactory.getEnBase().getEnvType();

        if(isLinux()){
            try {
                linuxOS = LinuxInfoUtil.getOSInfo();
            } catch (IOException e) {

                logger.warn("get linux os info failed!",e);
            }
        }


    }

    @Override
    public void refresh() {
        OperatingSystemMXBean bean= (OperatingSystemMXBean ) ManagementFactory.getOperatingSystemMXBean();
        maxFileDescriptorCount = invoke(bean,"getMaxFileDescriptorCount");
        openFileDescriptorCount = invoke(bean, "getOpenFileDescriptorCount");
        PhysicalMemoryAvaliable = bean.getFreePhysicalMemorySize();
        PhysicalMemoryTotal = bean.getTotalPhysicalMemorySize();
        systemCpuLoad = (float) bean.getSystemCpuLoad();
        processCpuTime = bean.getProcessCpuTime();
        processCpuLoad = bean.getProcessCpuLoad();

        File[] roots = File.listRoots();
        long diskAvaliable=0,diskTotal=0;
        for(File file:roots){
            diskAvaliable += file.getFreeSpace();
            diskTotal += file.getTotalSpace();
        }
        DiskAvaliable = diskAvaliable;
        DiskTotal = diskTotal;
        if(isLinux()) {
            try {
                availableMem =(LinuxInfoUtil.getAvailableMemKB()*1024l);
                cpuLoadAverages = LinuxInfoUtil.getCpuLoadAverage();
            } catch (Throwable e) {
                logger.warn("get linux info failed!");
            }
        }

    }
}

package com.ctrip.framework.vi.ignite;

import com.ctrip.framework.vi.*;
import com.ctrip.framework.vi.annotation.Ignite;
import com.ctrip.framework.vi.component.ComponentManager;
import com.ctrip.framework.vi.component.defaultComponents.*;
import com.ctrip.framework.vi.component.defaultComponents.linux.LinuxSocketInfo;
import com.ctrip.framework.vi.component.defaultComponents.linux.ProcOpenFile;
import com.ctrip.framework.vi.enterprise.EnFactory;
import com.ctrip.framework.vi.instrument.AgentMain;
import com.ctrip.framework.vi.instrument.AgentStatus;
import com.ctrip.framework.vi.instrument.AgentTool;
import com.ctrip.framework.vi.util.VIThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by jiang.j on 2016/8/11.
 */
@Ignite(id="vi-core.ignite",type = Ignite.PluginType.Component)
public class VICoreIgnite implements IgnitePlugin {

    private Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    int waitTimes = 0;
    private final int DEFAULTINTERVAL=5;
    final int MAXTIMES = 120;

    @Override
    public boolean run(IgniteManager.SimpleLogger logger) {

        logger.info("Start auto register thread, register interval is " + DEFAULTINTERVAL + " seconds.");
        runCheckThread();
        logger.info("Register component - AppInfo, IsSuccess: " +ComponentManager.add(AppInfo.class));
        logger.info("Register component - EnvInfo, IsSuccess: " +ComponentManager.add(EnvInfo.class));
        logger.info("Register component - HostInfo, IsSuccess: " +ComponentManager.add(HostInfo.class));
        logger.info("Register component - PerformanceStatus, IsSuccess: " +ComponentManager.add(PerformanceStatus.class));
        logger.info("Register component - VMSummary, IsSuccess: " +ComponentManager.add(VMSummary.class));
        try {
            if (System.getProperty(SysKeys.ServerInfo)!= null) {
                logger.info("Register component - TomcatInfo, IsSuccess: " + ComponentManager.add(TomcatInfo.class));
            }
        }catch (Throwable e){
            logger.info("No servlet api");
        }
        logger.info("Register component - SystemProperties, IsSuccess: " +ComponentManager.add(SystemProperties.class));
        logger.info("Register component - MemoryPoolInfo, IsSuccess: " +ComponentManager.add(MemoryPoolInfo.class));
        logger.info("Register component - VIMetricsInfo, IsSuccess: " +ComponentManager.add(VIMetricsInfo.class));
        logger.info("Register component - AllConfigFiles, IsSuccess: " +ComponentManager.add(AllConfigFiles.class));
        logger.info("Register component - AgentStatus, IsSuccess: " +ComponentManager.add(AgentStatus.class));

        if(HostInfo.isLinux()) {
            logger.info("add component -LinuxSocketInfo, IsSuccess: " +ComponentManager.add(LinuxSocketInfo.class));
            logger.info("add component -ProcOpenFile, IsSuccess: " +ComponentManager.add(ProcOpenFile.class));
        }

        AppInfo.getInstance().addStatusSource(OwnerJudge.getInstance());
        logger.info("Add OwnerJudge to AppStatusSources success!");
        if(HostInfo.isTomcat()) {
            File tomcatConfs = new File(System.getProperty("catalina.home") + "/conf/");
            if(tomcatConfs.exists() && tomcatConfs.isDirectory()){
                File[] files = tomcatConfs.listFiles();
                if(files != null) {
                    for (File f : files) {
                        if (AllConfigFiles.addConfigFile(f)) {
                            logger.info("Add config file " + f.getAbsolutePath() + " to allConfigs.");
                        }
                    }
                }
            }
        }

        return true;
    }

    void runCheckThread(){

        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor(new VIThreadFactory("vi-envIgnite"));
        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try{
                    if(waitTimes == 0 || waitTimes >MAXTIMES){

                        if(waitTimes != 0){
                            waitTimes = 0;
                        }
                        EnFactory.getEnApp().register();
                    }
                }
                catch (Throwable e){
                    logger.warn("self registration failed!", e);
                }finally {
                    waitTimes++;
                }

            }
        }, 0, DEFAULTINTERVAL, TimeUnit.SECONDS);
    }
}

package com.ctrip.framework.cornerstone.ignite;

import com.ctrip.framework.cornerstone.AppInfo;
import com.ctrip.framework.cornerstone.IgniteManager;
import com.ctrip.framework.cornerstone.OwnerJudge;
import com.ctrip.framework.cornerstone.VIServletContextListener;
import com.ctrip.framework.cornerstone.annotation.Ignite;
import com.ctrip.framework.cornerstone.component.ComponentManager;
import com.ctrip.framework.cornerstone.component.defaultComponents.*;
import com.ctrip.framework.cornerstone.component.defaultComponents.linux.LinuxSocketInfo;
import com.ctrip.framework.cornerstone.component.defaultComponents.linux.ProcOpenFile;

/**
 * Created by jiang.j on 2016/8/11.
 */
@Ignite(id="vi-core.ignite",type = Ignite.PluginType.Component)
public class VICoreIgnite implements IgnitePlugin {
    @Override
    public boolean run(IgniteManager.SimpleLogger logger) {
        logger.info("Register component - AppInfo, IsSuccess: " +ComponentManager.add(AppInfo.class));
        logger.info("Register component - EnvInfo, IsSuccess: " +ComponentManager.add(EnvInfo.class));
        logger.info("Register component - HostInfo, IsSuccess: " +ComponentManager.add(HostInfo.class));
        logger.info("Register component - PerformanceStatus, IsSuccess: " +ComponentManager.add(PerformanceStatus.class));
        logger.info("Register component - VMSummary, IsSuccess: " +ComponentManager.add(VMSummary.class));
        try {
            if (VIServletContextListener.ServerInfo != null) {
                logger.info("Register component - TomcatInfo, IsSuccess: " + ComponentManager.add(TomcatInfo.class));
            }
        }catch (Throwable e){
            logger.info("No servlet api");
        }
        logger.info("Register component - SystemProperties, IsSuccess: " +ComponentManager.add(SystemProperties.class));
        logger.info("Register component - MemoryPoolInfo, IsSuccess: " +ComponentManager.add(MemoryPoolInfo.class));
        logger.info("Register component - VIMetricsInfo, IsSuccess: " +ComponentManager.add(VIMetricsInfo.class));
        logger.info("Register component - AllConfigFiles, IsSuccess: " +ComponentManager.add(AllConfigFiles.class));

        if(HostInfo.isLinux()) {
            logger.info("add component -LinuxSocketInfo, IsSuccess: " +ComponentManager.add(LinuxSocketInfo.class));
            logger.info("add component -ProcOpenFile, IsSuccess: " +ComponentManager.add(ProcOpenFile.class));
        }

        AppInfo.getInstance().addStatusSource(OwnerJudge.getInstance());
        logger.info("Add OwnerJudge to AppStatusSources success!");
        return true;
    }
}

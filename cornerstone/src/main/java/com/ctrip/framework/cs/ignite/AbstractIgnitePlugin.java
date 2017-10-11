package com.ctrip.framework.cs.ignite;

import com.ctrip.framework.cs.IgniteManager;
import com.ctrip.framework.cs.util.LogHelper;
import com.ctrip.framework.cs.watcher.EventLogger;
import com.ctrip.framework.cs.watcher.EventLoggerFactory;

import java.util.Map;

/**
 * 点火插件基础类
 * Created by jiang.j on 2017/6/30.
 */
public abstract class AbstractIgnitePlugin implements IgnitePlugin {

    /**
     * 组件或应用的帮助页面链接，帮助页面里最好包含联系人和基本介绍以及如何利用点火日志排错的信息
     * @return 组件或应用的帮助wiki链接
     */
    public abstract String helpUrl();

    /**
     *可以将组件或应用的初始化动作放在里面，初始化中有致命问题时，可返回false。返回false会导致点火失败，点火失败的应用不会被拉入集群
     * @param logger 用来记录点火日志
     * @return true时，初始化成功。false时，初始化失败，导致点火失败。
     */
    public abstract boolean warmUP(IgniteManager.SimpleLogger logger);

    /**
     * 对组件或应用极为重要的一些配置（比如组件或应用的初始化参数）
     * @return 包含重要配置的map
     */
    public abstract Map<String,String> coreConfigs();

    /**
     * 用来执行组件或应用的自检操作
     * @param logger 用来记录点火日志
     * @return true时，自检成功。false时，自检失败，导致点火失败。
     */
    public abstract boolean selfCheck(IgniteManager.SimpleLogger logger);

    final public boolean run(IgniteManager.SimpleLogger logger){
        EventLogger eventLogger = EventLoggerFactory.getLogger(getClass(), "VI.ignitePlugin");
        eventLogger.fireEvent(getClass().getName());
        Map<String,String> configs = this.coreConfigs();
        String helpUrl = this.helpUrl();
        if(helpUrl == null){
            logger.error("help url is required!");
            return false;
        }
        logger.info("Help url: "+helpUrl);

        try {
            String warmTag = "Warm up";
            logger.info(LogHelper.beginBlock(warmTag));
            boolean warmUPResult = this.warmUP(logger);
            logger.info(LogHelper.endBlock(warmTag));
            if (!warmUPResult) {
                return false;
            }
        }finally {
            String configTag = "Print core configs";
            logger.info(LogHelper.beginBlock(configTag));
            if(configs!=null) {
                for (Map.Entry<String, String> entry : configs.entrySet()) {
                    logger.info("Config: " + entry.getKey() + " : " + entry.getValue());
                }
            }

            logger.info(LogHelper.endBlock(configTag));
        }


        String selfcheckTag = "Self-check";
        logger.info(LogHelper.beginBlock(selfcheckTag));
        boolean isPass = this.selfCheck(logger);
        logger.info(LogHelper.endBlock(selfcheckTag,new String[]{"isPass",String.valueOf(isPass)}));
        return isPass;
    }
}

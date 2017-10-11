package com.ctrip.framework.cs.helloworld.plugins;

import com.ctrip.framework.cs.IgniteManager;
import com.ctrip.framework.cs.annotation.Ignite;
import com.ctrip.framework.cs.configuration.ConfigurationManager;
import com.ctrip.framework.cs.helloworld.SOASelfCheck;
import com.ctrip.framework.cs.helloworld.ServiceMetaCache;
import com.ctrip.framework.cs.ignite.IgnitePlugin;

/**
 * Created by jiang.j on 2016/8/22.
 */
@Ignite(id = "dal.ignite",before = "redis.ignite")
public class DALIgnite implements IgnitePlugin{
    @Override
    public boolean run(IgniteManager.SimpleLogger logger) {
        logger.info("some help page: http://**.yourcorp.com/help");
        try {
            String coreUrl = ConfigurationManager.getConfigInstance().getString("some.registry.url");
            logger.info("Some registry url is " + coreUrl);
        } catch (Throwable e) {
            logger.error("Get registry url failed! Some initialize failed!");
            return false;
        }
        logger.info("Begin load metadata to cache");
        ServiceMetaCache.loadMeta();
        logger.info("End load metadata to cache");
        try{
            logger.info("Begin Self-check");
            if(!SOASelfCheck.check()){
                logger.error("Self-check failed! SOA initialize failed!");
                return false;
            }
            logger.info("Self-check success!");
        }finally {
            logger.info("End Self-check");
        }

        return true;
    }
}

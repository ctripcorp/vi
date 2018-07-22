package com.ctrip.framework.cs.spring.example.ignite;

import com.ctrip.framework.cs.AppInfo;
import com.ctrip.framework.cs.IgniteManager;
import com.ctrip.framework.cs.annotation.Ignite;
import com.ctrip.framework.cs.cacheRefresh.CacheCell;
import com.ctrip.framework.cs.cacheRefresh.CacheManager;
import com.ctrip.framework.cs.component.ComponentManager;
import com.ctrip.framework.cs.configuration.ConfigurationManager;
import com.ctrip.framework.cs.configuration.InitConfigurationException;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by jiang.j on 2016/8/17.
 */
@Ignite(id = "vi.example.ignite")
public class MyIgnitePlugin implements com.ctrip.framework.cs.ignite.IgnitePlugin {
    public class MyCacheCell implements CacheCell{

        private String _id;
        Map<String,Object> _status = new HashMap<>();
        public MyCacheCell(String id){
            _id = id;
            _status.put("refresh time",new Date());
        }
        @Override
        public String id() {
            return _id;
        }

        @Override
        public boolean refresh() {
            _status.put("refresh time",new Date());
            return true;
        }

        @Override
        public Map<String, Object> getStatus() {
            return _status;
        }

        @Override
        public Object getByKey(String key) {
            return null;
        }

        @Override
        public Iterable<String> keys() {
            return null;
        }

        @Override
        public int size() {
            return 0;
        }
    }
    @Override
    public boolean run(IgniteManager.SimpleLogger logger) {

        logger.info("prepare cache manager");
        CacheManager.add(new Memcache("hello world"));
        Memcache memcache = new Memcache("cities cache");
        memcache.getStatus().put("hitCount",9898);
        memcache.getStatus().put("lastOperator","idea");
        memcache.getStatus().put("refreshTime", new Date());
        CacheManager.add(memcache);
        logger.info("cache manager ready");
        Properties pros = new Properties();
        pros.put("env.status","good");
        pros.put("env.app","config");
        pros.put("app.status","normal");

        CacheManager.add(new MyCacheCell("cache cell 1"));
        CacheManager.add(new MyCacheCell("cache cell 2"));

        try {
            ConfigurationManager.installReadonlyProperties(pros);
        } catch (InitConfigurationException e) {
            e.printStackTrace();
        }
        return true;
    }
}

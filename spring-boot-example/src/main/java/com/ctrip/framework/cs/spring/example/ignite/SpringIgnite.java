package com.ctrip.framework.cs.spring.example.ignite;

import com.ctrip.framework.cs.IgniteManager;
import com.ctrip.framework.cs.annotation.Ignite;
import com.ctrip.framework.cs.configuration.ConfigurationManager;
import com.ctrip.framework.cs.configuration.InitConfigurationException;
import com.ctrip.framework.cs.ignite.AbstractIgnitePlugin;
import com.ctrip.framework.cs.localLog.LocalLogManager;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Ignite(id = "spring.ignite",type = Ignite.PluginType.Component)
public class SpringIgnite extends AbstractIgnitePlugin{
   @Override
   public String helpUrl() {
      return "http://www.spring.com/iginite/help.html";
   }

   @Override
   public boolean warmUP(IgniteManager.SimpleLogger logger) {
      logger.info("加载缓存....");
      String userDir = System.getProperty("user.dir");
      String demoLogPath = userDir + File.separator + "demo"+File.separator+"logs";
      try {
         logger.info("设置本地日志路径为："+demoLogPath);
         ConfigurationManager.getConfigInstance().setProperty("app.localLog.path",demoLogPath);
      } catch (InitConfigurationException e) {
         e.printStackTrace();
      }
      return true;
   }

   @Override
   public Map<String, String> coreConfigs() {
      Map<String,String> rtn = new HashMap<>();
      rtn.put("cache.size","100MB");
      rtn.put("cache.some","test some");

      return rtn;
   }

   @Override
   public boolean selfCheck(IgniteManager.SimpleLogger logger) {
      logger.info("检查各项配置");
      logger.info("配置服务正常");
      return true;
   }
}

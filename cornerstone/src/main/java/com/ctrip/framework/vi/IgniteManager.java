package com.ctrip.framework.vi;

import com.ctrip.framework.vi.annotation.EventSource;
import com.ctrip.framework.vi.annotation.Ignite;
import com.ctrip.framework.vi.component.ComponentManager;
import com.ctrip.framework.vi.component.defaultComponents.HostInfo;
import com.ctrip.framework.vi.configuration.Configuration;
import com.ctrip.framework.vi.configuration.ConfigurationManager;
import com.ctrip.framework.vi.configuration.InitConfigurationException;
import com.ctrip.framework.vi.enterprise.EnApp;
import com.ctrip.framework.vi.enterprise.EnFactory;
import com.ctrip.framework.vi.ignite.*;
import com.ctrip.framework.vi.ui.AutoRegister;
import com.ctrip.framework.vi.util.DepSortUtil;
import com.ctrip.framework.vi.util.LogHelper;
import com.ctrip.framework.vi.util.LoopReferenceNodeException;
import com.ctrip.framework.vi.watcher.EventLogger;
import com.ctrip.framework.vi.watcher.EventLoggerFactory;
import com.google.gson.internal.LinkedTreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by jiang.j on 2016/8/11.
 */
@EventSource(name = "VI.ignite")
public final class IgniteManager {
    static Logger logger = LoggerFactory.getLogger("JAVA-VI.Ignite");
    static IgniteStatus igniteStatus ;
    static final String CONFNAME="ignite";
    static final String IGNITESYNCKEY="ignite.sync";
    static  SimpleLogger simpleLogger ;
    static Map<String,IgnitePlugin> readonlyPluginContainers;
    static final String EXECUTEPLUGIN ="execute plugin" ;

    static {
            igniteStatus = ComponentManager.getStatus(IgniteStatus.class);
            igniteStatus.status = Status.Uninitiated;
            simpleLogger = new SimpleLogger(igniteStatus.getMessages(),logger);
    }


    public static Map<String,IgnitePlugin> getPluginMap(){
        return readonlyPluginContainers;
    }

    public static class SimpleLogger{
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        List<String> msgContainer;
        Logger logger;
        private SimpleLogger(List<String> messageContainer,Logger logger) {
            this.msgContainer = messageContainer;
            this.logger = logger;
        }

        public SimpleLogger(){
            msgContainer = new ArrayList<>();
            logger = LoggerFactory.getLogger(getClass());
        }


        void blankLine(){
            this.msgContainer.add("\r\n");
        }

        public List<String> getMsgs(int index){
            if(msgContainer.size()>0) {
                return msgContainer.subList(index, msgContainer.size());
            }else{
                return null;
            }
        }

        private Pattern pattern = Pattern.compile(".+<time>(.+)</time>$");
        private final int TIMETAGLEN = 14;
        public void info(String msg){
            Matcher matcher = pattern.matcher(msg);
            String time = null;
            if(matcher.matches()){
               time = matcher.group(1);
                msg = msg.substring(0,msg.length()-time.length()-TIMETAGLEN);
            }
            if(msg.startsWith(LogHelper.SPECIALINFOPREFIX)){
                logger.info(msg.substring(LogHelper.SPECIALINFOPREFIX.length()));
                this.msgContainer.add( msg + " #" +dateFormat.format(new Date()));
            } else{
                logger.info(msg);
                this.msgContainer.add("[INFO] " + (time==null?dateFormat.format(new Date()):time) + " - " + msg + "\r\n");
            }

        }
        public void warn(String msg){
            Matcher matcher = pattern.matcher(msg);
            String time = null;
            if(matcher.matches()){
                time = matcher.group(1);
                msg = msg.substring(0,msg.length()-time.length()-TIMETAGLEN);
            }
            logger.warn(msg);
            this.msgContainer.add("[WARN] " +(time==null?dateFormat.format(new Date()):time) + " - " + msg + "\r\n");
        }
        public void error(String msg){
            Matcher matcher = pattern.matcher(msg);
            String time = null;
            if(matcher.matches()){
                time = matcher.group(1);
                msg = msg.substring(0,msg.length()-time.length()-TIMETAGLEN);
            }
            logger.error(msg);
            this.msgContainer.add("[ERROR] " +(time==null?dateFormat.format(new Date()):time)+ " - " + msg + "\r\n");
        }
        public void error(String msg,Throwable throwable){
            Matcher matcher = pattern.matcher(msg);
            String time = null;
            if(matcher.matches()){
                time = matcher.group(1);
                msg = msg.substring(0,msg.length()-time.length()-TIMETAGLEN);
            }
            StringWriter sw = new StringWriter();
            throwable.printStackTrace(new PrintWriter(sw));
            logger.error(msg,throwable);
            this.msgContainer.add("[ERROR] " +(time==null?dateFormat.format(new Date()):time)+ " - " + msg + "\r\n" + sw.toString());
        }
    }
    public static IgniteStatus getStatus(){
        return igniteStatus;
    }

    private static Map<String,IgnitePlugin> getSortedPlugins(SimpleLogger logger) throws InitConfigurationException, RuntimeException, WrongPluginIdException, IgnitePluginNoIgniteAnnotationException {

        logger.blankLine();
        logger.info("Begin init and sort plugins");
        Set<String> keys = ConfigurationManager.getConfigKeys(CONFNAME);
        int skipCount = CONFNAME.length()+1;
        String viPackagePrex = "com.ctrip.framework.vi.";

        List<String> needRemoves = new ArrayList<>();

        if(keys != null) {
            for (String key : keys) {
                if (key.charAt(skipCount) == '-') {
                    needRemoves.add(key);
                }
            }
        }

        for(String key:needRemoves){
            keys.remove(key);
            String rkey = key.substring(0,skipCount)+key.substring(skipCount+1);
            if(rkey.substring(skipCount).startsWith(viPackagePrex)){
               logger.warn("remove ignitePlugin "+rkey.substring(skipCount) +" failed! because is belong to VI");
            }else {
                logger.warn("remove ignitePlugin " + rkey.substring(skipCount) + ",IsSuccess:" + keys.remove(rkey));
            }
        }

        Map<String,IgnitePlugin> plugins = new HashMap<>();
        List<Ignite> pluginInfos = new ArrayList<>();
        List<String> sortedPluginIds;

        logger.info("Begin init");
        VICoreIgnite viIgnitePlugin = new VICoreIgnite();
        Ignite viIgniteInfo = VICoreIgnite.class.getAnnotation(Ignite.class);
        pluginInfos.add(viIgniteInfo);
        plugins.put(viIgniteInfo.id(),viIgnitePlugin);

        if(keys != null) {
            for (String key : keys) {
                String className = key.substring(CONFNAME.length() + 1);
                try {
                    Class<?> pClass = Class.forName(className);
                    Ignite pInfo = pClass.getAnnotation(Ignite.class);
                    if (pInfo == null) {
                        throw new IgnitePluginNoIgniteAnnotationException(className);
                    }
                    IgnitePlugin plugin = (IgnitePlugin) pClass.newInstance();
                    String pId = pInfo.id();
                    if (plugins.containsKey(pId)) {
                        throw new DuplicatePluginIdException(pId + " must be unique, but it be used in[" + className + "," + plugins.get(pId).getClass().getName() + "]");
                    }

                    pluginInfos.add(pInfo);

                    plugins.put(pId, plugin);
                } catch (Throwable e) {
                    throw new RuntimeException("init ignite plugin, " + className + " failed!", e);
                }

            }
        }

        logger.info("End init");



        logger.info("Begin sort");
        List<DepSortUtil.Node> nodes = new ArrayList<>();
        List<Ignite> compList = new ArrayList<>();
        List<Ignite> appList = new ArrayList<>();
        for(Ignite info:pluginInfos){
            if(info.type() == Ignite.PluginType.Component){
               compList.add(info);
            }else{
                appList.add(info);
            }
        }
        compList.addAll(appList);
        pluginInfos = compList;

        for(Ignite info:pluginInfos){
            String className = plugins.get(info.id()).getClass().getName();
            for(String[] ids : new String[][]{info.before(), info.after()}) {
                for (String id : ids) {
                    if (!plugins.containsKey(id)) {
                        throw new WrongPluginIdException(className, id);
                    }
                }
            }
            nodes.add(new DepSortUtil.Node(info.id(),info.before(),info.after()));
        }

        try {
            sortedPluginIds = DepSortUtil.sort(nodes);
        } catch (LoopReferenceNodeException e) {
            StringBuilder builder = new StringBuilder("Loop reference be found in [");
            for(String id:e.ids()){
                builder.append("plugin class: ").append(plugins.get(id).getClass().getName()).append(", id: ").append(id).append(";\r\n");
            }

            builder.append("]");
            throw new RuntimeException(builder.toString(),e);
        }
        logger.info("End sort");
        Map<String,IgnitePlugin> tmpPlugins = new LinkedTreeMap<>();
        for(String id:sortedPluginIds){
            IgnitePlugin plugin = plugins.get(id);
            tmpPlugins.put(id, plugin);

            Ignite igniteInfo=null;
            for(Ignite item:pluginInfos){
                if(item.id().equals(id)){
                    igniteInfo = item;
                    break;
                }
            }

            if(igniteInfo == null){
                throw  new IgnitePluginNoIgniteAnnotationException(plugin.getClass().getName());
            }

            igniteStatus.infos.add(new IgniteStatus.PluginInfo(id, plugin.getClass().getName(),
                    igniteInfo.before(),igniteInfo.after()));
        }
        plugins = tmpPlugins;

        logger.info("End init and sort plugins");
        return plugins;
    }

    public static synchronized void ignite(){

        if(igniteStatus.status== Status.Uninitiated){
            igniteStatus.startTime = new Date();
            simpleLogger.info("Begin ignite");
            simpleLogger.blankLine();
            igniteStatus.status = Status.Running;
            final AppInfo appinfo;
            boolean igniteSync = false;
            try {
                simpleLogger.info("Current EnBase Class is " + EnFactory.getEnBase().getClass().getName());
                EnApp currentApp = EnFactory.getEnApp();
                simpleLogger.info("Current EnApp Class is " + currentApp.getClass().getName());
                simpleLogger.info("Current EnHost Class is " + EnFactory.getEnHost().getClass().getName());
                simpleLogger.info("Current EnAuthentication Class is " + EnFactory.getEnAuthentication().getClass().getName());
                simpleLogger.info("Current EnFC Class is " + EnFactory.getEnFC().getClass().getName());
                simpleLogger.info("Current EnMaven Class is " + EnFactory.getEnMaven().getClass().getName());
                simpleLogger.info("Current EnUI Class is " + EnFactory.getEnUI().getClass().getName());
                simpleLogger.info("Set HostName");
                System.setProperty("HOSTNAME", EnFactory.getEnHost().getHostName());
                System.setProperty("host.ip",EnFactory.getEnHost().getHostAddress());
                simpleLogger.info("End Set HostName");

                simpleLogger.info("AppInfo init");
                appinfo = ComponentManager.getStatus(AppInfo.class);
                simpleLogger.info("AppInfo ready");
                appinfo.setAppStatus(AppStatus.Initiating);
                appinfo.setLatestNews(String.format("Ignite start at %s", igniteStatus.getStartTime()));
                Properties properties =currentApp.getProperties();
                if(properties != null && properties.containsKey(IGNITESYNCKEY)){
                    igniteSync = "true".equalsIgnoreCase(properties.getProperty(IGNITESYNCKEY));
                }
                simpleLogger.info("Iginite sync is "+igniteSync);
                simpleLogger.info("Default HostInfo init");
                ComponentManager.add(HostInfo.class);
                simpleLogger.info("Default HostInfo ready");

            }catch (Throwable e){
               simpleLogger.error("ignite failed",e);
                return;
            }

            Thread igniteThread = new Thread("igniteThread"){
                @Override
                public void run(){
                    Map<String,IgnitePlugin> tmpMap = new HashMap<>();
                    try {
                        Configuration configuration = ConfigurationManager.getConfigInstance();

                        String scanKey = "vi.scan.enable";
                        if(configuration.containsKey(scanKey) && "false".equalsIgnoreCase(configuration.getString(scanKey))){
                            simpleLogger.warn("Auto scan be disabled!");
                        }else {
                            simpleLogger.info("Begin annotation auto scan.");
                            AutoRegister.autoRegister(simpleLogger);
                            simpleLogger.info("End annotation auto scan.");
                        }

                        Map<String,IgnitePlugin> pluginMap;
                        Throwable sortedError = null;
                        try{
                             pluginMap = getSortedPlugins(simpleLogger);
                        }catch (Throwable e){
                            sortedError = e;
                            pluginMap = new HashMap<>();
                            VICoreIgnite viIgnitePlugin = new VICoreIgnite();
                            Ignite viIgniteInfo = VICoreIgnite.class.getAnnotation(Ignite.class);
                            pluginMap.put(viIgniteInfo.id(), viIgnitePlugin);

                        }
                        int i=0;

                        for (Map.Entry<String,IgnitePlugin> plugin:pluginMap.entrySet()){
                            simpleLogger.blankLine();
                            //long pStartTime = System.currentTimeMillis();
                            String pKey = plugin.getKey();
                            IgnitePlugin currentPlugin = plugin.getValue();
                            igniteStatus.currentPluginIndex = i++;
                            EventLogger transLogger = EventLoggerFactory.getTransLogger(IgniteManager.class);
                            appinfo.setLatestNews("executing plugin id:" + pKey);
                            simpleLogger.info(LogHelper.beginBlock(EXECUTEPLUGIN, new String[]{"id", pKey, "class", currentPlugin.getClass().getName()}));
                            boolean outcome = false;
                            try {
                                transLogger.fireEvent(EventLogger.TRANSSTART,pKey);
                                outcome = currentPlugin.run(simpleLogger);
                                if(currentPlugin instanceof  AbstractIgnitePlugin){
                                    tmpMap.put(pKey,currentPlugin);
                                }
                                transLogger.fireEvent(EventLogger.TRANSEND);
                                if(!outcome){
                                    String msg = "Ignite Plugin " + pKey + " finish execute, but return false";
                                    simpleLogger.error(msg);
                                    igniteStatus.status = Status.Failure;
                                    appinfo.setAppStatus(AppStatus.InitiatedFailed);
                                    return;
                                }
                            }catch (Throwable e){
                                transLogger.fireEvent(EventLogger.TRANSEND,e);
                                String msg = "Ignite Plugin " + pKey + " execute failed";
                                simpleLogger.error(msg,e);
                                igniteStatus.status = Status.Failure;
                                appinfo.setAppStatus(AppStatus.InitiatedFailed);
                                return;

                            }finally {
                                transLogger.fireEvent(EventLogger.TRANSFINALLY);
                                simpleLogger.info(LogHelper.endBlock(EXECUTEPLUGIN,new String[]{"isPass",String.valueOf(outcome)}));
                            }
                        }

                        if(sortedError != null){
                            throw sortedError;
                        }
                        igniteStatus.status= Status.Success;
                        appinfo.setAppStatus(AppStatus.Initiated);
                    } catch (Throwable e) {
                        simpleLogger.error("Execute ignite plugins failed",e);
                        igniteStatus.status = Status.Failure;
                        appinfo.setAppStatus(AppStatus.InitiatedFailed);
                    }finally {
                        readonlyPluginContainers = Collections.unmodifiableMap(tmpMap);
                        simpleLogger.blankLine();
                        simpleLogger.info("Ignite result:" + igniteStatus.status);
                        String initError = EnFactory.getInitError();
                        if(initError != null){
                            simpleLogger.error(initError);
                        }
                        simpleLogger.info("End ignite");
                        simpleLogger.blankLine();
                        logger = null;
                        igniteStatus.cost = System.currentTimeMillis()-igniteStatus.startTime.getTime();
                        appinfo.setLatestNews("Ignite end. result:" + igniteStatus.status + ", cost:" + igniteStatus.cost+"ms");
                    }

                }

            };
            if(!igniteSync) {
                igniteThread.start();
            }else{
                igniteThread.run();
            }
        }

    }

    public static void reset(){

        AppInfo.getInstance().setAppStatus(AppStatus.Uninitiated);
        AppInfo.getInstance().cleanStatusSource();
        igniteStatus.status = Status.Uninitiated;
        //simpleLogger = new SimpleLogger(new ArrayList<String>(),logger);
    }

}

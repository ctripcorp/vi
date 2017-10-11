package com.ctrip.framework.cs;

import com.ctrip.framework.cs.component.ComponentManager;
import com.ctrip.framework.cs.component.defaultComponents.HostInfo;
import com.ctrip.framework.cs.component.Refreshable;
import com.ctrip.framework.cs.annotation.ComponentStatus;
import com.ctrip.framework.cs.annotation.FieldInfo;
import com.ctrip.framework.cs.enterprise.EnApp;
import com.ctrip.framework.cs.enterprise.EnFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.ctrip.framework.cs.util.TextUtils.nullToNA;

/**
 * Created by jiang.j on 2016/4/5.
 */
@ComponentStatus(id = "vi.appinfo", name = "app info", description = "应用信息",singleton = true,jmx = true)
public class AppInfo implements Refreshable{

    public interface StatusSource{
        boolean normal();
    }

    public interface StatusChangeListener{
        void statusChanged(AppStatus oldStatus,AppStatus newStatus);
    }
    public static final int UNINITIATED=528,INITIATING=526,INITIATED=200,INITIATEDFAILED=527,MARKDOWN=530;

    @FieldInfo(name = "Application ID", description = "应用ID")
    private  String appid="";
    @FieldInfo(name = "App Name",description = "应用名称")
    private  String appName;
    @FieldInfo(name = "App Chinese Name",description = "应用中文名")
    private  String appChineseName;
    @FieldInfo(name = "App Startup Time",description = "应用启动时间")
    private  final String AppStartUpTime;
    @FieldInfo(name = "App Up Time",description = "应用运行时间")
    private long upTime;
    @FieldInfo(name = "Application Version", description = "应用版本")
    private  String appVersion="";
    @FieldInfo(name = "Packing Time", description = "编译时间")
    private  String buildTime;
    @FieldInfo(name = "App Owner",description = "应用负责人")
    private  String appOwner;
    @FieldInfo(name = "App Owner Email",description = "应用负责人Email")
    private  String appOwnerEmail;
    @FieldInfo(name = "App Backup Email",description = "应用后备负责人")
    private  String appBackup;
    @FieldInfo(name = "App Notes",description = "应用说明")
    private  String appNotes;
    @FieldInfo(name = "App Status",description = "应用状态")
    private AppStatus appStatus=AppStatus.Uninitiated;


    @FieldInfo(name = "App Latest News",description = "应用最新消息")
    private String latestNews ="";
    @FieldInfo(name = "Tomcat Web Xml",description = "Tomcat Web.xml")
    private String webInfo;

    @FieldInfo(name = "MarkDown Reason",description = "下线原因")
    private String markDownReason;
    private transient AppStatus oldStatus;
    private transient ConcurrentMap<String,StatusChangeListener> statusListeners = new ConcurrentHashMap<>();
    private transient ConcurrentMap<String,StatusSource> statusSources = new ConcurrentHashMap<>();
    @FieldInfo(name="Status Sources",description = "应用状态源")
    private Set<String> statusSourceNames = statusSources.keySet();
    @FieldInfo(name="Status Sources Enabled",description = "应用状态源是否有效")
    private boolean statusSourceEnabled = true;


    public void setNote(String remark){
        appNotes = remark;
    }

    public boolean addStatusSource(StatusSource statusSource){
        if(statusSource == null){
            return false;
        }
        return this.statusSources.putIfAbsent(statusSource.getClass().getName(),statusSource) == null;
    }

    protected  void cleanStatusSource(){
        this.statusSources.clear();
    }

    public static AppInfo getInstance(){
        return ComponentManager.getStatus(AppInfo.class);
    }

    public String getNotes(){
        return appNotes;
    }

    protected void setLatestNews(String detail){
        latestNews = detail;
    }

    protected void setAppStatus(AppStatus status){

        oldStatus = appStatus;
        appStatus = status;
        notifyStatusListener(oldStatus,appStatus);
    }

    public boolean isNormal(){
        return getStatus() == AppStatus.Initiated;
    }

    protected void disableStatusSource(){
        this.statusSourceEnabled = false;
    }

    protected void enableStatusSource(){
        this.statusSourceEnabled = true;
    }

    public boolean isStatusSourceEnabled(){
        return this.statusSourceEnabled;
    }

      public AppStatus getStatus(){

        boolean hasAbnormal = false;
        switch (appStatus){
            case Uninitiated:
            case Initiating:
            case InitiatedFailed:
                return appStatus;
        }

        if(!this.statusSourceEnabled){
            return AppStatus.Initiated;
        }
        for (StatusSource statusSource : this.statusSources.values()) {
            boolean isNormal;
            try {
                isNormal = statusSource.normal();
            } catch (Throwable e) {
                logger.error("Status source execute failed!", e);
                isNormal = false;
            }

            if (!isNormal) {
                hasAbnormal = true;
                String statusClassName = statusSource.getClass().getName();
                if (appStatus != AppStatus.MarkDown || (markDownReason != null && !markDownReason.startsWith("class:"+statusClassName))) {
                    markDownReason = "class:"+statusClassName;
                    if(statusSource instanceof  Reason){
                        markDownReason += ", reason:"+((Reason)statusSource).reason();
                    }
                    latestNews = statusClassName + " mark down the app at " + new Date();
                    logger.warn(latestNews);
                    logger.warn(markDownReason);
                    if(appStatus != AppStatus.MarkDown) {
                        appStatus = AppStatus.MarkDown;
                        notifyStatusListener(AppStatus.Initiated, appStatus);
                    }
                }
                break;
            }
        }

        if(appStatus == AppStatus.MarkDown && !hasAbnormal){
            markDownReason = null;
            appStatus = AppStatus.Initiated;
            latestNews = "restore the app status from markdown  at " + new Date();
            logger.warn(latestNews);
            notifyStatusListener(AppStatus.MarkDown,appStatus);
        }

        return appStatus;
    }

    public String getAppOwner(){
        return appOwner;
    }

    private void notifyStatusListener(AppStatus old,AppStatus current){

        if(old != current){
            for (Map.Entry<String,StatusChangeListener> entry : statusListeners.entrySet()) {
                try {
                    logger.info("Execute status change listener:"+entry.getKey());
                    entry.getValue().statusChanged(old, current);
                }catch (Throwable e){
                    logger.warn("execute statuschangelistener:"+entry.getKey()+" failed!",e);
                }
            }
        }
    }


    @Override
    public void refresh() {
        if(HostInfo.isTomcat()){
            webInfo = "view";
        }
        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
        upTime = runtimeBean.getUptime();
        EnApp enApp = EnFactory.getEnApp();
        enApp.refresh();
        appid = nullToNA(EnFactory.getEnBase().getAppId());
        appName = nullToNA(enApp.getName());
        appChineseName = nullToNA(enApp.getChineseName());
        appOwner = nullToNA(enApp.getOwner());
        appOwnerEmail = nullToNA(enApp.getOwnerContact());
        appBackup = nullToNA(enApp.getBackup());
        appVersion = nullToNA(enApp.getVersion());
        buildTime = nullToNA(enApp.getBuildTime());
        getStatus();
    }


    private transient Logger logger = LoggerFactory.getLogger(this.getClass());
    ///opt/data/<app-id>/config/app.properties

    public AppInfo() {

        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
        AppStartUpTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(runtimeBean.getStartTime()));
        upTime = runtimeBean.getUptime();


    }

    public  String getAppVersion(){
        return appVersion;
    }

    public String getStartUpTime(){
        return AppStartUpTime;
    }

    public String getAppId(){
        return appid;
    }

    public String getLatestNews(){
        return this.latestNews;
    }
    public boolean addStatusChangeListener(StatusChangeListener listener){
        return statusListeners.putIfAbsent(listener.getClass().getName(), listener)==null;
    }

    public String getMarkDownReason(){
        return markDownReason;
    }

}

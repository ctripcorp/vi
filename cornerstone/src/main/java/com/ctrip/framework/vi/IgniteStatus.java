package com.ctrip.framework.cornerstone;

import com.ctrip.framework.cornerstone.annotation.ComponentStatus;
import com.ctrip.framework.cornerstone.component.ComponentManager;
import com.ctrip.framework.cornerstone.component.Refreshable;
import com.ctrip.framework.cornerstone.ignite.Status;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by jiang.j on 2016/8/16.
 */
@ComponentStatus(id = "vi.ignitestatus",name="ignite status",description = "应用点火详情",custom = true,singleton = true,jmx = true)
public class IgniteStatus implements Refreshable {
    public static class PluginInfo{
        private String id,className;
        private String[] before,after;

        public PluginInfo(String id,String className,String[] before,String[] after){
            this.id = id;
            this.className = className;
            this.before = before;
            this.after = after;

        }
        public String getId(){
            return id;
        }


        public String getClassName(){
            return className;
        }

        public String[] getBefore(){
            return before;
        }

        public String[] getAfter(){
            return after;
        }
    }
    //@FieldInfo(name = "Start Time",description = "开始时间")
    Date startTime;
    //@FieldInfo(name = "Status",description = "状态")
    Status status = Status.Uninitiated;
    //@FieldInfo(name = "Cost Time(ms)",description = "耗时(毫秒)")
    long cost=-1;
    //@FieldInfo(name = "Detail",description = "详情")
    List<String> messages = new ArrayList<>();
    List<PluginInfo> infos = new ArrayList<>();

    int currentPluginIndex = -1;
    public int getCurrentPluginIndex(){
        return currentPluginIndex;
    }
    public Date getStartTime(){
        return startTime;
    }

    public long getCost(){
        return cost;
    }

    public List<String> getMessages(){
        return messages;
    }

    public Status getStatus(){
        return status;
    }

    public List<PluginInfo> getPluginInfos(){
        return infos;
    }

    @Override
    public void refresh() {
        if(startTime!=null && status==Status.Running) {
            cost = System.currentTimeMillis() - startTime.getTime();
        }
    }
    public static class GetLastStatusReq{
        public int msgIndex;
    }
    public static class GetLastStatusResp{
        public List<String> messages;
        public int currentPluginIndex;
        public Status status;
        public long cost=-1;

    }
    public static GetLastStatusResp getLastStatus(GetLastStatusReq req) throws Exception {
        IgniteStatus currentStatus = ComponentManager.getStatus(IgniteStatus.class);

        GetLastStatusResp resp = new GetLastStatusResp();
        List<String> msgs = currentStatus.getMessages();
        if(msgs.size()>=req.msgIndex) {
            currentStatus.refresh();
            resp.status = currentStatus.getStatus();
            resp.currentPluginIndex = currentStatus.getCurrentPluginIndex();
            if(req.msgIndex<msgs.size()-1) {
                resp.messages = msgs.subList(req.msgIndex, msgs.size() - 1);
            }
            resp.cost = currentStatus.getCost();
        }

        return resp;

    }
}

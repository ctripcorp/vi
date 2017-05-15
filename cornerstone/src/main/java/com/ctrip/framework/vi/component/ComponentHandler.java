package com.ctrip.framework.cornerstone.component;

import com.ctrip.framework.cornerstone.Permission;
import com.ctrip.framework.cornerstone.ViFunctionHandler;
import com.ctrip.framework.cornerstone.util.TextUtils;
import com.ctrip.framework.cornerstone.util.Tools;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jiang.j on 2016/6/13.
 */
public class ComponentHandler implements ViFunctionHandler {

    private String startPath = "/component/";
    private int pathSkipChar=startPath.length();

    @Override
    public Object execute(String path, String user, int permission, Logger logger, Map<String, Object> params) throws Exception{

        Object rtn = null;
        String subPath = path.substring(pathSkipChar);
        Map<String, Class<?>> components = ComponentManager.getAllComponents();

        if(path.equals(startPath+"meta")){
            rtn = ComponentManager.getComponentMeta();
        }else if(path.equals(startPath+"fieldmeta")){
            rtn = ComponentManager.getFieldMeta();
        }else {
            String[] parts = subPath.split("/");
            Class<?> seleComponent = components.get(parts[0]);

            if(seleComponent!=null){
                if(parts.length==1) {
                    rtn = ComponentManager.getStatus(seleComponent);
                }else if(parts.length==2){
                    String sub = parts[1].toLowerCase();
                    if(sub.equals("custom")) {
                        Map<String, String> cdata = new HashMap<>();
                        String folder = "componentstatus";
                        cdata.put("html", Tools.getInnerResources(seleComponent,folder, parts[0], "html"));
                        cdata.put("js", Tools.getInnerResources(seleComponent,folder, parts[0], "js"));
                        rtn = cdata;
                    }else{
                        rtn = Tools.doClassStaticMethod(seleComponent, sub, params);
                    }
                }
            }
        }
        if(null == rtn) {
            rtn = path + " not found "+ TextUtils.join(",",components.keySet());
        }
        return rtn;
    }

    @Override
    public String getStartPath() {
        return startPath;
    }

    @Override
    public Permission getPermission(String user) {
        return Permission.ALL;
    }
}

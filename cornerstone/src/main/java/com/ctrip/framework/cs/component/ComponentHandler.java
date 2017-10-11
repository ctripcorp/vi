package com.ctrip.framework.cs.component;

import com.ctrip.framework.cs.ViFunctionHandler;
import com.ctrip.framework.cs.configuration.ConfigurationManager;
import com.ctrip.framework.cs.util.IOUtils;
import com.ctrip.framework.cs.util.Tools;
import com.ctrip.framework.cs.Permission;
import com.ctrip.framework.cs.configuration.Configuration;
import com.ctrip.framework.cs.util.TextUtils;
import org.slf4j.Logger;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Paths;
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
                    rtn = ComponentManager.getStatus(seleComponent,user);
                }else if(parts.length==2){
                    String sub = parts[1].toLowerCase();
                    if(sub.equals("custom")) {
                        String compId = parts[0];
                        Map<String, String> cdata = new HashMap<>();
                        String folder = "componentstatus";
                        String devKey = "vi.component.dev."+compId;
                        Configuration configuration = ConfigurationManager.getConfigInstance();
                        if(configuration.containsKey(devKey)) {
                            String devPath = configuration.getString(devKey);
                            try(InputStream is= new FileInputStream(Paths.get(devPath,compId+".html").toFile())) {
                                cdata.put("html", IOUtils.readAll(is));
                            }
                            try(InputStream is= new FileInputStream(Paths.get(devPath,compId+".js").toFile())) {
                                cdata.put("js", IOUtils.readAll(is));
                            }

                        }
                        if(!cdata.containsKey("html")) {
                            cdata.put("html", Tools.getInnerResources(seleComponent, folder, compId, "html"));
                            cdata.put("js", Tools.getInnerResources(seleComponent, folder, compId, "js"));
                        }
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

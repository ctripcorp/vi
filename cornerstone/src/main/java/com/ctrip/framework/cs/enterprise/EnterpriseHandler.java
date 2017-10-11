package com.ctrip.framework.cs.enterprise;

import com.ctrip.framework.cs.Permission;
import com.ctrip.framework.cs.ViFunctionHandler;
import com.ctrip.framework.cs.ui.CustomPage;
import com.ctrip.framework.cs.util.Tools;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jiang.j on 2016/9/1.
 */
public class EnterpriseHandler implements ViFunctionHandler {
    String startPath = "/enterprise/";
    @Override
    public Object execute(String path, String user, int permission, Logger logger, Map<String, Object> params) throws Exception {
        Object rtn=null;
        String pageStartPath = startPath+"page/";
        if(path.equals(startPath+"servers")) {
            rtn = EnFactory.getEnApp().getAllServers();
        }else if(path.equals(startPath+"help")) {
            rtn = EnFactory.getEnApp().getHelpLinks();
        }else if(path.startsWith(pageStartPath)) {
            String subPath = path.substring(pageStartPath.length());
            if(subPath.length()>2) {
                String[] parts = subPath.split("/");
                CustomPage customPage = EnFactory.getEnUI().getPageById(parts[0]);

                if(parts.length == 1) {
                    Map<String, String> cdata = new HashMap<>();
                    String folder = "enterprisepages";
                    cdata.put("html", Tools.getInnerResources(customPage.getClass(), folder, parts[0], "html"));
                    cdata.put("js", Tools.getInnerResources(customPage.getClass(), folder, parts[0], "js"));
                    rtn = cdata;
                }else {
                    rtn = Tools.doClassStaticMethod(customPage.getClass(), parts[1], params);
                }
            }
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

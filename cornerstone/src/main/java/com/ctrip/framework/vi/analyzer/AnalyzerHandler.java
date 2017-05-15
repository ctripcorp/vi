package com.ctrip.framework.cornerstone.analyzer;

import com.ctrip.framework.cornerstone.Permission;
import com.ctrip.framework.cornerstone.ViFunctionHandler;
import com.ctrip.framework.cornerstone.util.TextUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Map;

/**
 * Created by jiang.j on 2016/6/13.
 */
public class AnalyzerHandler implements ViFunctionHandler {
    private String startPath ="/analyzer/";
    @Override
    public Object execute(String path, String user, int permission, Logger logger, Map<String, Object> params) throws Exception{
        Object rtn=null;
            if(path.equals(startPath+"deps")) {
                rtn = Analyzer.getAllPomInfo();
            }else if(path.equals(startPath+"heaphisto")){
                rtn = JVMSampler.getCurrentHeapHisto();
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

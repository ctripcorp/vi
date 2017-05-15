package com.ctrip.framework.cornerstone.fc;

import com.ctrip.framework.cornerstone.enterprise.EnFC;
import com.ctrip.framework.cornerstone.enterprise.EnFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jiang.j on 2016/4/8.
 */
public class FCManager {

    private static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static EnFC featureContingency= EnFactory.getEnFC();


    public static boolean isFeatureEnable(String key){
        return featureContingency.isFeatureEnable(key);
    }

     static void setFeatures(Map<String,Boolean> features,String user){

         featureContingency.setFeatures(features,user);
    }

    static int getPermission(String user){
        return featureContingency.getPermission(user);
    }

     static Map<String,Object[]> getAllFeature(){

         Map<String,Object[]> fcs = new HashMap<>();
         Map<String,Boolean> allFeatures =  featureContingency.allFeatures();
         Map<String,String> allRemarks = featureContingency.allCustomRemarks();

         for(String key:allFeatures.keySet()){

             Object[] info = new Object[2];
             info[0]= allFeatures.get(key);
             if(allRemarks != null) {
                 info[1] = allRemarks.get(key);
             }
             fcs.put(key,info);
         }

         return fcs;
    }

}

package com.ctrip.framework.cornerstone.util;

import com.ctrip.framework.cornerstone.enterprise.EnFactory;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by jiang.j on 2016/5/30.
 */
public class PomUtil {

    class NexusPomInfo{
        String groupId;
        String artifactId;
        String version;
    }
    class RepoDetail{
        String repositoryURL;
    }
    class SearchResult{

        RepoDetail[] repoDetails;
        NexusPomInfo[] data;
    }

    static Logger logger = LoggerFactory.getLogger(PomUtil.class);

    public static InputStream getPomInfoByFileName(String[] av,String fileName){
        return EnFactory.getEnMaven().getPomInfoByFileName(av,fileName);
    }

    public static String[] getArtifactIdAndVersion(String fileName){
        boolean needJudege=false;
        int versionBeginIndex =-1;
        for(int i=0;i<fileName.length();i++){
            char c = fileName.charAt(i);
            if(needJudege && Character.isDigit(c)){
                versionBeginIndex = i;
                break;
            }
            if(c == '-'){
                needJudege = true;
            }else{
                needJudege = false;
            }
        }
        if(versionBeginIndex>0){

            return new String[]{
                    fileName.substring(0, versionBeginIndex - 1),
                    fileName.substring(versionBeginIndex)
            };
        }else{
            return null;
        }
    }
}

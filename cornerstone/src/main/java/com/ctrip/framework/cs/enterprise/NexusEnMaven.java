package com.ctrip.framework.cs.enterprise;

import com.ctrip.framework.cs.configuration.ConfigurationManager;
import com.ctrip.framework.cs.configuration.InitConfigurationException;
import com.ctrip.framework.cs.util.HttpUtil;
import com.ctrip.framework.cs.util.PomUtil;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by jiang.j on 2016/10/20.
 */
public class NexusEnMaven implements EnMaven {

    class NexusPomInfo{
        String groupId;
        String artifactId;
        String version;
    }
    class RepoDetail{
        String repositoryURL;
        String repositoryKind;
    }
    class SearchResult{

        RepoDetail[] repoDetails;
        NexusPomInfo[] data;
    }

    class ResourceResult{
        ResourceInfo[] data;
    }
    class ResourceInfo{
        String text;
    }

    Logger logger = LoggerFactory.getLogger(getClass());


    private InputStream getContentByName(String[] av,String fileName) throws InitConfigurationException {
        InputStream rtn = null;
        String endsWith = ".pom";
        if(av == null && fileName == null){
            return null;
        }else if(av==null) {
            endsWith = "-sources.jar";
            av = PomUtil.getArtifactIdAndVersion(fileName);
        }

        if(av == null){
            return null;
        }


        String searchUrl = (ConfigurationManager.getConfigInstance().getString("vi.maven.repository.url") +
                "/nexus/service/local/lucene/search?a=" + av[0] + "&v=" + av[1]);
        if(av.length >2){
            searchUrl += "&g="+av[2];
        }
        logger.debug(searchUrl);

        try {
            URL url = new URL(searchUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(200);
            conn.setReadTimeout(500);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("Accept", "application/json");
            try (Reader rd = new InputStreamReader(conn.getInputStream(), "UTF-8")) {
                Gson gson = new Gson();
                SearchResult results = gson.fromJson(rd, SearchResult.class);
                if(results.repoDetails!=null && results.data !=null && results.repoDetails.length>0 && results.data.length>0){

                    NexusPomInfo pomInfo = results.data[0];
                    String repositoryUrl = null;
                    if(results.repoDetails.length>1){
                        for(RepoDetail repoDetail:results.repoDetails){
                            if("hosted".equalsIgnoreCase(repoDetail.repositoryKind)){
                                repositoryUrl = repoDetail.repositoryURL;
                                break;
                            }
                        }
                    }
                    if(repositoryUrl == null)
                    {
                        repositoryUrl = results.repoDetails[0].repositoryURL;
                    }
                    String pomUrl = repositoryUrl +"/content/"+pomInfo.groupId.replace(".","/")+"/"+pomInfo.artifactId+"/"
                            +pomInfo.version+"/";
                    if(fileName == null){
                        ResourceResult resourceResult = HttpUtil.doGet(new URL(pomUrl), ResourceResult.class);
                        for(ResourceInfo rinfo:resourceResult.data){
                            if(rinfo.text.endsWith(endsWith) && (
                                    fileName == null || fileName.compareTo(rinfo.text)>0)){
                                fileName = rinfo.text;
                            }
                        }
                        pomUrl += fileName;
                    }else {
                        pomUrl += fileName + endsWith;
                    }
                    logger.debug(pomUrl);
                    HttpURLConnection pomConn = (HttpURLConnection) new URL(pomUrl).openConnection();
                    pomConn.setRequestMethod("GET");
                    rtn = pomConn.getInputStream();
                }
            }
        }catch (Throwable e){
            logger.warn("get pominfo by jar name["+av[0] + ' '+av[1]+"] failed",e);
        }
        return rtn;

    }
    @Override
    public InputStream getPomInfoByFileName(String[] av, String fileName) {
        try {
            return getContentByName(av, fileName);
        }catch (Throwable e){
            logger.warn("getPomInfoByFileName failed!",e);
            return null;
        }
    }

    @Override
    public InputStream getSourceJarByFileName(String fileName) {
        try {
            return getContentByName(null,fileName);
        }catch (Throwable e){
            logger.warn("getPomInfoByFileName failed!",e);
            return null;
        }
    }
}

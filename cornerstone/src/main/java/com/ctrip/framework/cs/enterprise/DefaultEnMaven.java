package com.ctrip.framework.cs.enterprise;

import com.ctrip.framework.cs.util.SecurityUtil;
import com.ctrip.framework.cs.util.PomUtil;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HttpsURLConnection;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by jiang.j on 2016/10/20.
 */
public class DefaultEnMaven implements EnMaven {

    class PomDoc{
        String g;
        String a;
        String v;
    }
    class SearchResponse{
        PomDoc[] docs;
    }
    class SearchResult{

        SearchResponse response;
    }
    Logger logger = LoggerFactory.getLogger(getClass());

    private InputStream getContentByName(String[] av,String fileName){

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

        String searchUrl = "http://search.maven.org/solrsearch/select?q=a:%22";
        if(av.length>2){
            searchUrl += av[0] + "%22%20AND%20v:%22" + av[1]+ "%22%20AND%20g:%22" + av[2] + "%22%20AND%20p:%22jar%22&rows=1&wt=json";
        }else {
            searchUrl += av[0] + "%22%20AND%20v:%22" + av[1] + "%22%20AND%20p:%22jar%22&rows=1&wt=json";
        }
        logger.info(searchUrl);

        try {
            URL url = new URL(searchUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(500);
            conn.setReadTimeout(500);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("Accept", "application/json");
            try (Reader rd = new InputStreamReader(conn.getInputStream(), "UTF-8")) {
                Gson gson = new Gson();
                SearchResult results = gson.fromJson(rd, SearchResult.class);
                if(results.response!=null && results.response.docs !=null && results.response.docs.length >0 ){

                    PomDoc pomInfo = results.response.docs[0];
                    String pomUrl = "https://search.maven.org/remotecontent?filepath="+pomInfo.g.replace('.','/')+"/"+pomInfo.a+"/"+pomInfo.v+"/"+pomInfo.a+"-"+pomInfo.v+endsWith;
                    //com/jolira/guice/3.0.0/guice-3.0.0.pom
                    logger.info(pomUrl);
                    HttpsURLConnection pomConn = (HttpsURLConnection) new URL(pomUrl).openConnection();
                    pomConn.setSSLSocketFactory(SecurityUtil.getSSLSocketFactory());
                    pomConn.setRequestMethod("GET");
                    rtn = pomConn.getInputStream();
                }
            }
        }catch (Exception e){
            logger.warn("get pominfo by jar name["+av[0] + ' '+av[1]+"] failed",e);
        }
        return rtn;

    }

    @Override
    public InputStream getPomInfoByFileName(String[] av, String fileName) {

        return getContentByName(av,fileName);
    }

    @Override
    public InputStream getSourceJarByFileName(String fileName) {
        return getContentByName(null,fileName);
    }
}

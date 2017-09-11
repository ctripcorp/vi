package com.ctrip.framework.vi;

import com.ctrip.framework.vi.analyzer.*;
import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by jiang.j on 2016/5/19.
 */
public class AnalyzerTest {

    @Test
    public void testSome() throws IOException, NoSuchFieldException, URISyntaxException, NoSuchAlgorithmException {


    }
    @Test
    public void testAnalyzePom(){

        try {
            XMLReader xmlReader = XMLReaderFactory.createXMLReader();
            PomDependencyHandler handler = new PomDependencyHandler();
            xmlReader.setContentHandler(handler);
            xmlReader.parse(new InputSource(this.getClass().getClassLoader().getResourceAsStream("testpom.xml")));
            assertEquals(4, handler.getDependencies().size());
            PomInfo pom = handler.getPomInfo();
            boolean hasVI=false;
            for (PomDependency d : handler.getDependencies()) {
                assertNotNull(d.artifactId);
                assertNotNull(d.groupId);
                assertNotNull(d.version);
                if("framework-validateinternals".equals(d.artifactId)){
                    hasVI=true;
                    assertEquals(d.version,"0.9");
                }
            }
            assertTrue(hasVI);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Test
    public  void testGetAllJarPom(){

        try {
            List<PomInfo> poms = Analyzer.getAllJarPomInfo();
            assertTrue(poms.size()>0);
            PomInfo pomInfo = poms.get(0);
            assertNotNull(pomInfo.artifactId);
            System.out.println(pomInfo.artifactId);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}

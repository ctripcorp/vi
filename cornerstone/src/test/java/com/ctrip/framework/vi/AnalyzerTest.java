package com.ctrip.framework.cornerstone;

import com.ctrip.framework.cornerstone.analyzer.Analyzer;
import com.ctrip.framework.cornerstone.analyzer.PomDependency;
import com.ctrip.framework.cornerstone.analyzer.PomInfo;
import com.ctrip.framework.cornerstone.analyzer.PomDependencyHandler;
import com.ctrip.framework.cornerstone.component.ComponentManager;
import com.ctrip.framework.cornerstone.component.defaultComponents.AllConfigFiles;
import com.ctrip.framework.cornerstone.component.defaultComponents.EnvInfo;
import com.ctrip.framework.cornerstone.jmx.VIDynamicMBean;
import com.ctrip.framework.cornerstone.util.DesUtil;
import com.ctrip.framework.cornerstone.util.IOUtils;
import com.ctrip.framework.cornerstone.util.SecurityUtil;
import com.google.gson.Gson;
import org.junit.Test;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.regex.Pattern;

import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by jiang.j on 2016/5/19.
 */
public class AnalyzerTest {

    @Test
    public void testSome() throws IOException, NoSuchFieldException {

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

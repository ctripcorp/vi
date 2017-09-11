package com.ctrip.framework.vi.configuration;

import com.ctrip.framework.vi.util.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.Assert.*;

/**
 * Created by jiang.j on 2016/4/7.
 */
public class ConfigurationManagerTest {

    public ConfigurationManagerTest(){
        ConfigurationManager.setEnableDefaultload(false);

    }

    @Test
    public void testNoConfig(){
        try {
            ConfigurationManager.setEnableDefaultload(true);
            ConfigurationManager.setConfigPath("config1/");
            Configuration config = ConfigurationManager.getConfigInstance();
        }catch (Exception e) {
            Assert.fail();
        }finally {
            ConfigurationManager.setEnableDefaultload(false);
        }
    }

    @Test
    public void testGetDefaultAppPro(){

        ConfigurationManager.setEnableDefaultload(true);

        System.out.println(ConfigurationManager.class.getProtectionDomain().getCodeSource().getLocation().toString());
        URL url = getClass().getProtectionDomain().getCodeSource().getLocation();
        try {
            InputStream in = ConfigurationManager.class.getClassLoader().getResourceAsStream("jar:"+url.toURI().toString()+"!/META-INF/MANIFEST.MF");
            System.out.println(url.toURI().toString());
            System.out.println(in);
            ZipInputStream zip = new ZipInputStream(url.openStream());
            ZipEntry ze;
            while ((ze = zip.getNextEntry())!=null){

                if(ze.getName().equals("META-INF/MANIFEST.MF")){
                    System.out.println(ze.getName());
                    break;
                }
            }
            System.out.println(IOUtils.readAll(zip));

        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //assertNotNull(ConfigurationManager.getConfigInstance().getString("app.sso.url"));
        ConfigurationManager.setEnableDefaultload(false);
    }

    @Test
    public void testLoadProperties() throws Exception {
        ConfigurationManager.loadPropertiesFromResources("test.properties");
        assertEquals("9", ConfigurationManager.getConfigInstance().getProperty("com.ctrip.config.samples.needCount"));
        assertEquals("9", ConfigurationManager.getConfigInstance().getString("com.ctrip.config.samples.needCount"));
        assertEquals("100", ConfigurationManager.getConfigInstance().getString("no.exist","100"));
    }

    @Test
    public void testNumberProperties() throws Exception {
        Configuration config = ConfigurationManager.getConfigInstance();
        double val = 12.3;
        config.setProperty("test-double",val);
        assertTrue(val == config.getDouble("test-double"));
        config.setProperty("test-int",10);
        assertTrue(10 == config.getDouble("test-int"));
        assertTrue(0 == config.getDouble("test-int-emp"));
        assertTrue(20 == config.getInt("test-int-emp",20));
        assertTrue(new Integer(23) == config.getInt("test-int-emp",new Integer(23)));
        assertEquals(false,config.getBoolean("test-boolean"));
    }

    @Test
    public void testLoadChineseProperties() throws Exception {
        ConfigurationManager.getConfigInstance().clear();
        ConfigurationManager.loadPropertiesFromResources("test.properties");
        assertEquals("测试", ConfigurationManager.getConfigInstance().getProperty("chinese"));
    }

    @Test
    public void testLoadCascadedProperties() throws Exception {
        ConfigurationManager.loadCascadedPropertiesFromResources("config/test");
        assertEquals("7", ConfigurationManager.getConfigInstance().getProperty("com.ctrip.config.samples.needCount"));
        assertEquals("1", ConfigurationManager.getConfigInstance().getProperty("cascaded.property"));
    }

    @Test
    public void testInstallReadOnly() throws InitConfigurationException {

        Properties pros = new Properties();
        pros.setProperty("hello","world");
        ConfigurationManager.installReadonlyProperties(pros);

        assertEquals("world",ConfigurationManager.getConfigInstance().getString("#.hello"));
        ConfigurationManager.setProperties(new HashMap<String, String>(){{
            put("#.hello", "abc");
        }},"root");
        assertEquals("world", ConfigurationManager.getConfigInstance().getString("#.hello"));
    }
}

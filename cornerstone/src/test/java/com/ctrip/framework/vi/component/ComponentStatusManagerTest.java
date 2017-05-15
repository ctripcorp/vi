package com.ctrip.framework.cornerstone.component;

import com.ctrip.framework.cornerstone.annotation.ComponentStatus;
import com.ctrip.framework.cornerstone.AppInfo;
import com.ctrip.framework.cornerstone.component.defaultComponents.HostInfo;
import com.ctrip.framework.cornerstone.component.defaultComponents.PerformanceStatus;
import com.ctrip.framework.cornerstone.util.Tools;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.*;

public class ComponentStatusManagerTest {

    @Test
    public void componentTest(){

        ComponentManager.add(AppInfo.class);
        ComponentManager.add(HostInfo.class);

        Map<String, Class<?>> components = ComponentManager.getAllComponents();
        ComponentStatus compAnnotation =  AppInfo.class.getAnnotation(ComponentStatus.class);
        assertTrue(components.containsKey(compAnnotation.id()));
    }

    @Test
    public void testCustomComponent() throws IOException {

        ComponentManager.add(PerformanceStatus.class);
        ComponentStatus compAnnotation =  PerformanceStatus.class.getAnnotation(ComponentStatus.class);
        assertNotNull(Tools.getInnerResources(PerformanceStatus.class,"componentstatus", compAnnotation.id(), "html"));
    }
    @Test
    public void fieldMetaTest(){
        ComponentManager.add(AppInfo.class);
        List<Map<String,String>> fieldMeta = ComponentManager.getFieldMeta();

        Map<String,String> meta = null;
        for(Map<String,String> item : fieldMeta){
            if("vi.appinfo.appid".equals(item.get("id"))){
                meta = item;
                break;
            }
        }
        assertNotEquals(null,meta);
        assertEquals("vi.appinfo.appid",meta.get("id"));
        assertEquals("Application ID",meta.get("name"));
        assertEquals("应用ID",meta.get("description"));

    }

    @Test
    public void testGetSingletonStatus() {
        AppInfo appInfo = ComponentManager.getStatus(AppInfo.class);
        assertNotNull(appInfo);
        AppInfo appInfo1 = ComponentManager.getStatus(AppInfo.class);
        assertEquals(appInfo,appInfo1);
    }

    @Test
    public void testGetSingletonStatus1() {
        AppInfo appInfo = ComponentManager.getStatus(AppInfo.class);
        assertNotNull(appInfo);
        String remark = "hello world";
        appInfo.setNote(remark);
        AppInfo appInfo1 = ComponentManager.getStatus(AppInfo.class);
        assertEquals(remark,appInfo1.getNotes());
    }
}

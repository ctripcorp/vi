package com.ctrip.framework.vi;

import com.ctrip.framework.vi.configuration.ConfigurationManager;
import com.ctrip.framework.vi.configuration.InitConfigurationException;
import com.ctrip.framework.vi.ignite.Status;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by jiang.j on 2017/7/17.
 */
public class AppStatusTest {

    public class MyStatusSource implements AppInfo.StatusSource,Reason{

        private boolean isnormal = true;
        private String reason ="";
        public void setValue(boolean val){
           this.isnormal = val;
        }

        public void setReason(String reason){
            this.reason = reason;
        }
        @Override
        public boolean normal() {
            return this.isnormal;
        }

        @Override
        public String reason() {
            return this.reason;
        }
    }
    public class MStatusSource implements AppInfo.StatusSource{

        private boolean isnormal = true;
        public void setValue(boolean val){
            this.isnormal = val;
        }

        @Override
        public boolean normal() {
            return this.isnormal;
        }
    }

    @Test
    public void testNormal() throws InterruptedException {

        IgniteManager.reset();
        IgniteManager.ignite();
        IgniteStatus status = IgniteManager.getStatus();

        while (status.getStatus()== Status.Running) {
            System.out.println(status.getStatus());
            Thread.sleep(300);
        }

        assertEquals(AppStatus.Initiated, AppInfo.getInstance().getStatus());
    }

    @Test
    public void testUninitiatedMarkdown() throws InterruptedException {

        IgniteManager.reset();
        MyStatusSource statusSource = new MyStatusSource();
        AppInfo.getInstance().addStatusSource(statusSource);
        statusSource.setValue(false);
        String reason = "some error";
        statusSource.setReason(reason);

        assertEquals(AppStatus.Uninitiated,AppInfo.getInstance().getStatus());

    }

    @Test
    public void testInitiatingMarkdown() throws InterruptedException {
        IgniteManager.reset();
        IgniteManager.ignite();
        IgniteStatus status = IgniteManager.getStatus();
        MyStatusSource statusSource = new MyStatusSource();
        AppInfo.getInstance().addStatusSource(statusSource);

        while (status.getStatus()== Status.Running) {
            System.out.println(status.getStatus());
            statusSource.setValue(false);
            String reason = "some error";
            statusSource.setReason(reason);
            assertEquals(AppStatus.Initiating, AppInfo.getInstance().getStatus());
            Thread.sleep(300);
        }


    }


    @Test
    public void testIgniteFailMarkdown() throws InterruptedException, InitConfigurationException {
        IgniteManager.reset();
        ConfigurationManager.getConfigKeys("ignite").add("com.ctrip.framework.vi.plugins.TestIginite5");
        IgniteManager.ignite();
        IgniteStatus status = IgniteManager.getStatus();
        MyStatusSource statusSource = new MyStatusSource();
        AppInfo.getInstance().addStatusSource(statusSource);

        while (status.getStatus()== Status.Running) {
            System.out.println(status.getStatus());
            Thread.sleep(300);
        }
        assertEquals(Status.Failure,status.getStatus());
        statusSource.setValue(false);
        assertEquals(AppStatus.InitiatedFailed, AppInfo.getInstance().getStatus());
        ConfigurationManager.getConfigKeys("ignite").remove("com.ctrip.framework.vi.plugins.TestIginite5");
    }

    @Test
    public void testMarkdown() throws InterruptedException {
        IgniteManager.reset();
        IgniteManager.ignite();
        IgniteStatus status = IgniteManager.getStatus();
        MyStatusSource statusSource = new MyStatusSource();
        AppInfo.getInstance().addStatusSource(statusSource);

        while (status.getStatus()== Status.Running) {
            System.out.println(status.getStatus());
            Thread.sleep(300);
        }
        assertEquals(AppStatus.Initiated,AppInfo.getInstance().getStatus());
        statusSource.setValue(false);
        String reason = "some error";
        statusSource.setReason(reason);
        assertEquals(AppStatus.MarkDown,AppInfo.getInstance().getStatus());
        assertTrue(AppInfo.getInstance().getMarkDownReason().endsWith(reason));

    }

    @Test
    public void testMarkup() throws InterruptedException {
        IgniteManager.reset();
        IgniteManager.ignite();
        IgniteStatus status = IgniteManager.getStatus();
        MyStatusSource statusSource = new MyStatusSource();
        AppInfo.getInstance().addStatusSource(statusSource);

        while (status.getStatus()== Status.Running) {
            System.out.println(status.getStatus());
            Thread.sleep(300);
        }
        assertEquals(AppStatus.Initiated,AppInfo.getInstance().getStatus());
        statusSource.setValue(false);
        String reason = "some error";
        statusSource.setReason(reason);
        assertEquals(AppStatus.MarkDown,AppInfo.getInstance().getStatus());
        assertTrue(AppInfo.getInstance().getMarkDownReason().endsWith(reason));
        statusSource.setValue(true);
        assertEquals(AppStatus.Initiated,AppInfo.getInstance().getStatus());
        assertEquals(null,AppInfo.getInstance().getMarkDownReason());

    }

    @Test
    public void testMultiMarkdown() throws InterruptedException {

        IgniteManager.reset();
        IgniteManager.ignite();
        IgniteStatus status = IgniteManager.getStatus();
        MyStatusSource statusSource = new MyStatusSource();
        MStatusSource statusSource1 = new MStatusSource();
        AppInfo.getInstance().addStatusSource(statusSource);
        AppInfo.getInstance().addStatusSource(statusSource1);

        while (status.getStatus()== Status.Running) {
            System.out.println(status.getStatus());
            Thread.sleep(300);
        }
        assertEquals(AppStatus.Initiated,AppInfo.getInstance().getStatus());
        statusSource.setValue(false);
        statusSource1.setValue(false);
        String reason = "some error";
        statusSource.setReason(reason);
        assertEquals(AppStatus.MarkDown, AppInfo.getInstance().getStatus());
        //assertTrue(AppInfo.getInstance().getMarkDownReason().contains("MStatusSource"));
        statusSource1.setValue(true);
        assertEquals(AppStatus.MarkDown, AppInfo.getInstance().getStatus());
        assertTrue(AppInfo.getInstance().getMarkDownReason().contains("MyStatusSource"));
        assertTrue(AppInfo.getInstance().getMarkDownReason().endsWith(reason));
        statusSource1.setValue(false);
        statusSource.setValue(true);
        assertEquals(AppStatus.MarkDown, AppInfo.getInstance().getStatus());
        assertTrue(!AppInfo.getInstance().getMarkDownReason().endsWith(reason));
        assertTrue(AppInfo.getInstance().getMarkDownReason().contains("MStatusSource"));

    }
}
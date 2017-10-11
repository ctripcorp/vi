package com.ctrip.framework.cs;

import com.ctrip.framework.cs.enterprise.DefaultEnUI;
import com.ctrip.framework.cs.enterprise.EnFactory;
import com.ctrip.framework.cs.ui.CustomPage;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


/**
 * Created by jiang.j on 2016/11/1.
 */
public class EnUITest {

    public class DemoPage implements CustomPage{

        @Override
        public String getId() {
            return "ctrip-clog";
        }

        @Override
        public String getName() {
            return "demo";
        }

        @Override
        public String getIcon() {
            return null;
        }
    }
    @Test
    public void testGetPage(){
        DefaultEnUI enUI = (DefaultEnUI) EnFactory.getEnUI();
        assertTrue(enUI.getMenus().size()>0);
        enUI.addMenu(new DemoPage());
        assertNotNull(enUI.getPageById("ctrip-clog"));
    }
}

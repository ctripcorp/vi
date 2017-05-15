package com.ctrip.framework.cornerstone;

import com.ctrip.framework.cornerstone.enterprise.DefaultEnUI;
import com.ctrip.framework.cornerstone.enterprise.EnFactory;
import com.ctrip.framework.cornerstone.ui.CustomPage;
import org.junit.Test;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.TestCase.assertTrue;


/**
 * Created by jiang.j on 2016/11/1.
 */
public class EnUITest {

    public class DemoPage implements CustomPage{

        @Override
        public String getId() {
            return "demo-id";
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
        assertNotNull(enUI.getPageById("demo-id"));
    }
}

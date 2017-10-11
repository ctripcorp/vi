package com.ctrip.framework.cs.enterprise;

import com.ctrip.framework.cs.ui.CustomPage;
import com.ctrip.framework.cs.ui.Menu;

import java.util.List;

/**
 * Created by jiang.j on 2016/11/1.
 */
public interface EnUI {
    public List<Menu> getMenus();
    public CustomPage getPageById(String id);
}

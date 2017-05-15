package com.ctrip.framework.cornerstone.enterprise;

import com.ctrip.framework.cornerstone.ui.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by jiang.j on 2016/11/1.
 */
public class DefaultEnUI implements EnUI {
    private List<Menu> menus = new ArrayList<>();
    private ConcurrentMap<String,CustomPage> pages = new ConcurrentHashMap<>();

    public DefaultEnUI(){
        addMenu(new BuildInComponent());
        addMenu(new BuildInMetrics());
        Menu analyzerMenu = addMenu("Analyzer","fa-coffee");
        addSubMenu(analyzerMenu, new BuildInJarDep());
        addSubMenu(analyzerMenu, new BuildInJvmSampler());
        addMenu(new BuildInConfiguration());
        addMenu(new BuildInFC());
        addMenu(new BuildInCache());
        addMenu(new BuildInThreadDump());
        addMenu(new BuildInFileLog());

    }


    public Menu addMenu(String name,String icon){
        Menu rtn = new Menu(name,icon);
        this.menus.add(rtn);
        return rtn;
    }

    public Menu addMenu(CustomPage customPage){
        Menu rtn = new Menu(customPage);
        if(!rtn.isBuildIn()){
            pages.putIfAbsent(customPage.getId(),customPage);
        }
        this.menus.add(rtn);
        return rtn;
    }

    public void addSubMenu(Menu menu, CustomPage customPage){
        Menu.SubMenu subMenu = new Menu.SubMenu(customPage);
        if(!subMenu.isBuildIn()){
            pages.putIfAbsent(customPage.getId(),customPage);
        }
        menu.addSubMenu(subMenu);
    }



    @Override
    public List<Menu> getMenus() {
        return menus;
    }

    @Override
    public CustomPage getPageById(String id) {
        return this.pages.get(id);
    }
}

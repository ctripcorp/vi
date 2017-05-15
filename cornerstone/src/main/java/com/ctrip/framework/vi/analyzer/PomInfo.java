package com.ctrip.framework.cornerstone.analyzer;

import java.io.Serializable;
import java.util.List;

/**
 * Created by jiang.j on 2016/5/19.
 */
public class PomInfo implements Serializable{
    public String  groupId;
    public String artifactId;
    public String version;
    public String location;
    public long size;
    public List<PomDependency> dependencies;

}


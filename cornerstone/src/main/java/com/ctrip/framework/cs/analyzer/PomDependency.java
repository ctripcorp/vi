package com.ctrip.framework.cs.analyzer;

import java.io.Serializable;

/**
 * Created by jiang.j on 2016/5/20.
 */
public class PomDependency implements Serializable{
    public String  groupId;
    public String artifactId;
    public String version;
    public String scope;
}

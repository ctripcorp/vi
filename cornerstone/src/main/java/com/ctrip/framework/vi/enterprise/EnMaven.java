package com.ctrip.framework.cornerstone.enterprise;

import java.io.InputStream;

/**
 * Created by jiang.j on 2016/10/20.
 */
public interface EnMaven {
    InputStream getPomInfoByFileName(String[] av,String fileName);
}

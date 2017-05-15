package com.ctrip.framework.cornerstone;

/**
 * Created by jiang.j on 2017/2/8.
 */
public class OwnerJudge implements AppInfo.StatusSource {
    private OwnerJudge(){

    }
    private static OwnerJudge ownerStatusSource = new OwnerJudge();
    public static OwnerJudge getInstance(){
        return ownerStatusSource;
    }
    boolean isNormal = true;

    public void toNormal(){
        isNormal = true;
    }

    public void toAbnormal(){
        isNormal = false;
    }
    @Override
    public boolean normal() {
        return isNormal;
    }
}

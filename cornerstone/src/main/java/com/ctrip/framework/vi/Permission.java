package com.ctrip.framework.vi;
/**
 * Created by jiang.j on 2016/5/4.
 */
public enum Permission{
    DENY(0),ALL(1),READ(2),EDIT(4);
    private int value;
    private Permission(int permission){
        this.value = permission;
    }

    public int getValue(){
        return value;
    }

    public static Permission valueOf(int value){
        switch (value){
            case 1:
                return Permission.ALL;
            case 2:
                return Permission.READ;
            case 4:
                return Permission.EDIT;
            case 0:
            default:
                return Permission.DENY;
        }
    }
}

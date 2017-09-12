package com.ctrip.framework.vi.security;

/**
 * Created by jiang.j on 2017/5/8.
 */
public enum BlockType {
    NONE((byte)0),BlackIP((byte)1),GrayIP((byte)2),
    BlackClientID((byte)3),GrayClientID((byte)4),
    BlackClientToken((byte)5),GrayClientToken((byte)6),
    BlackACID((byte)7),GrayACID((byte)8);
    private byte value;
    private BlockType(byte n){
       this.value = n;
    }

    public byte getValue(){
        return this.value;
    }

    public static BlockType fromInt(int value){
        switch (value){
            case 1:
                return BlackIP;
            case 2:
                return GrayIP;
            case 3:
                return BlackClientID;
            case 4:
                return GrayClientID;
            case 5:
                return BlackClientToken;
            case 6:
                return GrayClientToken;
            case 7:
                return BlackACID;
            case 8:
                return GrayACID;
        }
        return NONE;
    }

}

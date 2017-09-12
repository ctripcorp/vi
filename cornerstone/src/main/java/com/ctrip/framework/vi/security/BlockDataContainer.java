package com.ctrip.framework.vi.security;

import com.ctrip.framework.vi.util.ArrayUtils;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jiang.j on 2017/5/9.
 */
public class BlockDataContainer {

    Map<Byte,int[]> container = new HashMap<>();

    public byte[] array(){

        int totalCount = 0;

        int typeCount = container.size();
        for (int[] d:container.values()){
            totalCount += d.length;
        }
        ByteBuffer buffer = ByteBuffer.allocate(1 + 3 * typeCount + totalCount * 4);

        buffer.put((byte)typeCount);

        for(Map.Entry<Byte,int[]> entry:container.entrySet()){
            byte type = entry.getKey();
            buffer.put((byte) Math.abs(type));
            int[] values = entry.getValue();
            buffer.putShort((short) (values.length*(type<0?-1:1)));
            for(int i:values){
                buffer.putInt(i);
            }
        }

        return buffer.array();
    }

    public void addBlockData(BlockType type,int[] data){
        addBlockData(type,data,false);
    }

    public void addBlockData(BlockType type,int[] data,boolean isSubtract){


        Arrays.sort(data);
        Byte key = (byte)(type.getValue()*(isSubtract?-1:1));
        if(container.containsKey(key)) {
            container.put(key,
                    ArrayUtils.mergeSortedArray(data, container.get(key)));
        }else{
            container.put(key,data);
        }
    }
}

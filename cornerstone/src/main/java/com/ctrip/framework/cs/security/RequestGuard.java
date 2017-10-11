package com.ctrip.framework.cs.security;

import com.ctrip.framework.cs.util.ArrayUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jiang.j on 2017/5/8.
 */
public class RequestGuard {

    private static Map<BlockType,int[]> blockDataContainer = new HashMap<>();
    public static void getData(){
    }
    public static void refreshData(boolean isPart,InputStream is) throws IOException, IllegalDataException {
        int segNum = is.read();
        int i=0;
        Map<BlockType,int[]> tmpContainer = new HashMap<>();
        while (i++<segNum){
            BlockType blockType = BlockType.fromInt(is.read());
            byte[] tmp = new byte[2];
            if(is.read(tmp)!=2){
                throw new IllegalDataException();
            }
            short count = ByteBuffer.wrap(tmp).getShort();
            int ipCount = Math.abs(count);
            byte[] data = new byte[ipCount*4];
            int[] ips = new int[ipCount];
            if(is.read(data)!=data.length){
                throw new IllegalDataException();
            }

            for (int j = 0; j < ipCount; j++) {
                ips[j] = ByteBuffer.wrap(data, j * 4, 4).getInt();
            }

            if(isPart){
                int[] oldD = blockDataContainer.get(blockType);
                if(count>0){
                    tmpContainer.put(blockType, ArrayUtils.mergeSortedArray(oldD, ips));
                }else{
                    tmpContainer.put(blockType, ArrayUtils.subtractSortedArray(oldD, ips));
                }

            }else {
                if(tmpContainer.containsKey(blockType)){
                    throw new IllegalDataException();
                }
                tmpContainer.put(blockType, ips);
            }
        }

        if(is.read()!=-1){
            throw new IllegalDataException();
        }

        if(isPart){
            for(Map.Entry<BlockType,int[]> entry:tmpContainer.entrySet()){
                blockDataContainer.put(entry.getKey(),entry.getValue());
            }
        }else {
            blockDataContainer = tmpContainer;
        }

    }

    public static boolean isNeedBlock(BlockType type,int ip){
        return blockDataContainer.containsKey(type) && Arrays.binarySearch(blockDataContainer.get(type), ip) >= 0;
    }
}

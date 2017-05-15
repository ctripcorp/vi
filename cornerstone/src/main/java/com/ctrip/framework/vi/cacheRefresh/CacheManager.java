package com.ctrip.framework.cornerstone.cacheRefresh;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by jiang.j on 2016/5/12.
 */
public final class CacheManager {
    private static ConcurrentLinkedQueue<CacheCell> container = new ConcurrentLinkedQueue<>();
    private static Set<String> types = new HashSet<>();
    public final static boolean refreshById(String id,String typeName) throws CacheNotFoundException {

        return findCellById(id,typeName).refresh();

    }

    public static Set<String> types(){
        return types;
    }
    public  static Map<String,Map<String,Object>> status(String typeName){
        Map<String,Map<String,Object>> rtn = new HashMap<>();
        for(CacheCell cell:container){
            if(cell.getClass().getName().equalsIgnoreCase(typeName)) {
                rtn.put(cell.id(), cell.getStatus());
            }
        }
        return rtn;
    }
    public final static CacheCell findCellById(String id,String typeName) throws CacheNotFoundException{

        CacheCell cell = findByIdAndType(id,typeName);
        if(cell !=null){
            return cell;
        }
        throw new CacheNotFoundException();
    }

    private static  CacheCell findByIdAndType(String id,String typeName){

        for(CacheCell item : container){
            if(item.getClass().getName().equalsIgnoreCase(typeName) && item.id().equals(id)){
                return item;
            }
        }
        return null;
    }

    public final static Map<String,Object> getStatusById(String id,String typeName) throws CacheNotFoundException {
        return findCellById(id,typeName).getStatus();
    }

    public final static boolean add(CacheCell item){

        String typeName =item.getClass().getName();
        if(findByIdAndType(item.id(),typeName)!=null){
            return false;
        }
        synchronized (types){
            types.add(typeName);
        }

        return container.add(item);
    }



}

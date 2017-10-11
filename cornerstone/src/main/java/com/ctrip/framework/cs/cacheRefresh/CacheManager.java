package com.ctrip.framework.cs.cacheRefresh;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by jiang.j on 2016/5/12.
 */
public final class CacheManager {
    private static ConcurrentLinkedQueue<CacheCell> container = new ConcurrentLinkedQueue<>();
    private static Set<String> types = new HashSet<>();
    public static class KV{
        public KV(String key,Object value){
            this.key = key;
            this.value =value;
        }
        String key;
        Object value;
    }
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
                Map<String,Object> status = new LinkedHashMap<>();
                status.put("size", cell.size());
                status.putAll(cell.getStatus());
                rtn.put(cell.id(), status);
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

    public final static Object findByIndex(String id,String typeName,int index) throws CacheNotFoundException {
        CacheCell cell = findCellById(id,typeName);
        Iterable<String> keys = cell.keys();
        if(keys != null) {
            Iterator<String> iterator = keys.iterator();
            int current = 0;
            while (iterator.hasNext()) {
                String key = iterator.next();
                if (current == index) {
                    return new KV(key,cell.getByKey(key));
                }
                current++;
            }
        }else{
            throw new CacheNotFoundException();
        }

        return null;
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

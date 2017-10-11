package com.ctrip.framework.cs;

import com.ctrip.framework.cs.cacheRefresh.CacheManager;
import com.ctrip.framework.cs.cacheRefresh.CacheNotFoundException;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by jiang.j on 2016/5/12.
 */
public class CacheRefreshTest {
    @Test
    public void testCacheObj(){
        RedisCache cache = new RedisCache("onlytest");
        String rediscacheTypeName = RedisCache.class.getName();
        CacheManager.add(cache);
        String id = "more fun";
        CacheManager.add(new RedisCache(id));

        System.out.println(rediscacheTypeName);
        assertEquals(2, CacheManager.status(RedisCache.class.getName()).size());

        try {
            assertEquals(cache, CacheManager.findCellById("onlytest",rediscacheTypeName));
            assertNotNull(CacheManager.getStatusById("onlytest",rediscacheTypeName));
            assertNotNull(CacheManager.getStatusById(id,rediscacheTypeName));
            assertTrue(CacheManager.refreshById(id,rediscacheTypeName));
        }catch (Exception e){
            e.printStackTrace();
        }
        try{
            CacheManager.refreshById("moretest1",rediscacheTypeName);
        }catch (Exception e){

            assertEquals(CacheNotFoundException.class,e.getClass());
        }

    }
}

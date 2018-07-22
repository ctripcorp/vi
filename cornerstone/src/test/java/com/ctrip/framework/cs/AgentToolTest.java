package com.ctrip.framework.cs;

import com.ctrip.framework.cs.code.debug.LocalVariable;
import com.ctrip.framework.cs.instrument.AgentTool;
import com.google.gson.Gson;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by jiang.j on 2017/7/25.
 */
public class AgentToolTest {

    //@Test
    public void testLineNumMetadata(){

        try {
            List<LocalVariable> variableList = AgentTool.getLineAccessMetadata(Gson.class.getName(),498);
            assertEquals("this", variableList.get(0).getName());
            assertEquals("Lcom/google/gson/Gson;", variableList.get(0).getDesc());
            assertEquals("skipPast", variableList.get(1).getName());
            assertEquals("Lcom/google/gson/TypeAdapterFactory;", variableList.get(1).getDesc());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

package com.ctrip.framework.vi.codetest;

import com.ctrip.framework.vi.asm.Opcodes;
import com.ctrip.framework.vi.code.debug.*;
import com.ctrip.framework.vi.instrument.AgentTool;
import com.google.gson.Gson;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by jiang.j on 2017/7/12.
 */
public class DebugTest {
    @Test
    public void tesstNullStrLen() throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException, InstantiationException, NoSuchMethodException, InterruptedException, InvocationTargetException, IllegalConditionException {


        Condition conditionObj = new Condition("nulStr","Ljava/lang/String;", Condition.STRLEN,Opcodes.IF_ICMPLT,"10");
        List<Condition> conditions = new ArrayList<>();
        conditions.add(conditionObj);
        DebugClassLoader classLoader = new DebugClassLoader(this.getClass().getClassLoader(),new DebugInfo(44,"traceid", Condition.checkAndCorrect(conditions.toArray(new Condition[conditions.size()])),"dev"));

        Class testClass = classLoader.loadClass("com.ctrip.framework.vi.codetest.SampleClass");
        Object obj = testClass.newInstance();
        testClass.getMethod("doSome").invoke(obj);
        Thread.sleep(100);

        Object debugInfo = (testClass.getMethod("getDebugResult").invoke(obj));
        assertEquals(null,debugInfo);

    }

    @Test
    public void testNullFieldLen() throws IllegalConditionException, ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException, InterruptedException, InvocationTargetException {

        Condition conditionObj = new Condition("obj","Ljava/lang/Integer;", -1,Opcodes.IF_ICMPLE,"9",new ClassField[]{

                new ClassField(2,"value","I")
        });
        List<Condition> conditions = new ArrayList<>();
        conditions.add(conditionObj);
        DebugClassLoader classLoader = new DebugClassLoader(this.getClass().getClassLoader(),new DebugInfo(44,"traceid", conditions.toArray(new Condition[conditions.size()]),"dev"));

        Class testClass = classLoader.loadClass("com.ctrip.framework.vi.codetest.SampleClass");
        Object obj = testClass.newInstance();
        testClass.getMethod("doSome").invoke(obj);
        Thread.sleep(100);

        Object debugInfo = (testClass.getMethod("getDebugResult").invoke(obj));
        assertTrue(debugInfo instanceof Map);
        Map infos = (Map) debugInfo;
        assertTrue(infos.containsKey(DebugTool.STACKKEY));
        System.out.println(infos);


    }

    @Test
    public void testIntegerField() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException, InterruptedException, IOException, NoSuchFieldException {


        Condition conditionObj = new Condition("obj","Ljava/lang/Integer;", -1,Opcodes.IF_ICMPLE,"9",new ClassField[]{

                new ClassField(2,"value","I")
        });
        List<Condition> conditions = new ArrayList<>();
        conditions.add(conditionObj);
        DebugClassLoader classLoader = new DebugClassLoader(this.getClass().getClassLoader(),new DebugInfo(44,"traceid", conditions.toArray(new Condition[conditions.size()]),"dev"));

        Class testClass = classLoader.loadClass("com.ctrip.framework.vi.codetest.SampleClass");
        Object obj = testClass.newInstance();
        testClass.getMethod("doSome").invoke(obj);
        Thread.sleep(100);

        Object debugInfo = (testClass.getMethod("getDebugResult").invoke(obj));
        assertTrue(debugInfo instanceof Map);
        Map infos = (Map) debugInfo;
        assertTrue(infos.containsKey(DebugTool.STACKKEY));
        System.out.println(infos);
    }

    @Test
    public void testFieldString() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException, InterruptedException, IOException, NoSuchFieldException, IllegalConditionException {


        Condition conditionFSTREQ = new Condition("info","Lcom/ctrip/framework/vi/codetest/SampleClass$SampleInfo;",Condition.STREQUAL,Opcodes.IFEQ,"samle info",new ClassField[]{
                new ClassField(Opcodes.ACC_PRIVATE,"name","Ljava/lang/String;")
        });
        List<Condition> conditions = new ArrayList<>();
        conditions.add(conditionFSTREQ);
        DebugClassLoader classLoader = new DebugClassLoader(this.getClass().getClassLoader(),new DebugInfo(40,"traceid",Condition.checkAndCorrect(conditions.toArray(new Condition[conditions.size()])),"dev"));

        Class testClass = classLoader.loadClass("com.ctrip.framework.vi.codetest.SampleClass");
        Object obj = testClass.newInstance();
        testClass.getMethod("doSome").invoke(obj);
        Thread.sleep(100);

        Object debugInfo = (testClass.getMethod("getDebugResult").invoke(obj));
        assertEquals(null,debugInfo);
    }

    @Test
    public void testFieldString1() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException, InterruptedException, IOException, NoSuchFieldException, IllegalConditionException {


        Condition conditionFSTREQ = new Condition("info","Lcom/ctrip/framework/vi/codetest/SampleClass$SampleInfo;",Condition.STREQUAL,Opcodes.IFEQ,"samle info",new ClassField[]{
                new ClassField(Opcodes.ACC_PRIVATE,"name","Ljava/lang/String;")
        });
        List<Condition> conditions = new ArrayList<>();
        conditions.add(conditionFSTREQ);
        DebugClassLoader classLoader = new DebugClassLoader(this.getClass().getClassLoader(),new DebugInfo(39,"traceid",Condition.checkAndCorrect(conditions.toArray(new Condition[conditions.size()])),"dev"));

        Class testClass = classLoader.loadClass("com.ctrip.framework.vi.codetest.SampleClass");
        Object obj = testClass.newInstance();
        testClass.getMethod("doSome").invoke(obj);
        Thread.sleep(100);

        Object debugInfo = (testClass.getMethod("getDebugResult").invoke(obj));
        assertEquals(null,debugInfo);
    }

    @Test
    public void testSimpleDebugPass() throws Exception {

        Condition condition = new Condition("i","I", -1,Opcodes.IF_ICMPLE,"100");
        Condition conditionf = new Condition("f","F", Opcodes.FCMPG,Opcodes.IFGT,"15.3");
        Condition conditionS = new Condition("s","S", -1,Opcodes.IF_ICMPLE,"99");
        Condition conditionC = new Condition("c","C", -1,Opcodes.IF_ICMPNE,"z");
        Condition conditionB = new Condition("b","Z", -1,Opcodes.IF_ICMPNE,"0");
        Condition conditionObj = new Condition("obj","Ljava/lang/Integer;", -1,Opcodes.IFNULL);
        Condition conditionStr = new Condition("str","Ljava/lang/String;",-1,Opcodes.IFNULL);
        Condition conditionStrLen = new Condition("str","Ljava/lang/String;",Condition.STRLEN,Opcodes.IF_ICMPLE,"8");
        Condition conditionStrEqual = new Condition("str","Ljava/lang/String;",Condition.STREQUAL,Opcodes.IFEQ,"many ....");
        Condition conditionFB = new Condition("info","Lcom/ctrip/framework/vi/codetest/SampleClass$SampleInfo;", -1,Opcodes.IF_ICMPNE,"0",new ClassField[]{
                new ClassField(Opcodes.ACC_PRIVATE,"isTrue","Z")
        });
        Condition conditionFSTREQ = new Condition("info","Lcom/ctrip/framework/vi/codetest/SampleClass$SampleInfo;",Condition.STREQUAL,Opcodes.IFEQ,"samle info",new ClassField[]{
                new ClassField(Opcodes.ACC_PRIVATE,"name","Ljava/lang/String;")
        });
        List<Condition> conditions = new ArrayList<>();
        conditions.add(condition);
        conditions.add(conditionf);
        conditions.add(conditionStr);
        conditions.add(conditionStrLen);
        conditions.add(conditionStrEqual);
        conditions.add(conditionS);
        conditions.add(conditionC);
        conditions.add(conditionB);
        conditions.add(conditionObj);
        conditions.add(conditionFB);
        conditions.add(conditionFSTREQ);
        DebugClassLoader classLoader = new DebugClassLoader(this.getClass().getClassLoader(),new DebugInfo(44,"traceid", conditions.toArray(new Condition[conditions.size()]),"dev"));

        Class testClass = classLoader.loadClass("com.ctrip.framework.vi.codetest.SampleClass");
        Object obj = testClass.newInstance();
        testClass.getMethod("doSome").invoke(obj);
        Thread.sleep(100);

        Object debugInfo = (testClass.getMethod("getDebugResult").invoke(obj));
        assertTrue(debugInfo instanceof Map);
        Map infos = (Map) debugInfo;
        assertTrue(infos.containsKey(DebugTool.STACKKEY));
        System.out.println(infos);
    }

    @Test
    public void testFieldCondition(){

    }

    @Test
    public void testMetaData() throws IOException {
        List<LocalVariable> localVariables = AgentTool.getLineAccessMetadata(Gson.class.getName(), 562);
        System.out.println(localVariables);
    }
}

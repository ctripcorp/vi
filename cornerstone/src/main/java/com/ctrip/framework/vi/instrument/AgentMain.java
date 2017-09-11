package com.ctrip.framework.vi.instrument;


import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;

/**
 * Created by jiang.j on 2017/3/13.
 */
public class AgentMain {
    private static Instrumentation inst;
      public static void agentmain(String agentArgs, Instrumentation instrumentation)
            throws ClassNotFoundException, UnmodifiableClassException,
            InterruptedException {
          inst = instrumentation;
          inst.addTransformer(new CodeTransformer(), true);

    }

    public static Instrumentation instrumentation() {
        return inst;
    }


}

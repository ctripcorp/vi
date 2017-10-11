package com.ctrip.framework.cs.code;

import com.ctrip.framework.cs.NotFoundException;

/**
 * Created by jiang.j on 2017/3/13.
 */
public interface Debugger {
        /**
     * 初始化 Debugger 实例
     */
    boolean startup();

    /**
     * 添加一个断点
     *
     * @param source       源码文件，包括名称和路径
     * @param line         行号
     * @param breakpointId 断点ID
     * @return 断点是否添加成功
     */
    boolean registerBreakpoint(final String source, final int line, final String breakpointId);


    boolean registerBreakpoint(final String source, final int line, final String breakpointId,final String condition);

    /**
     * 激活一个断点，此处激活以后，后面执行中如果包含断点，就会进行 StackFrame 的记录
     *
     * @param breakpointId 需要激活的断点ID
     * @return 是否激活成功
     */
    boolean triggerBreakpoint(final String breakpointId);

    /**
     * 获得断点激活后记录的数据
     *
     * @param breakpointId 断点ID
     * @return 断点记录的数据
     */
    StackFrame getCapturedFrame(final String breakpointId) throws NotFoundException;

    void stopTrace(final String breakpointId) throws NotFoundException;
}

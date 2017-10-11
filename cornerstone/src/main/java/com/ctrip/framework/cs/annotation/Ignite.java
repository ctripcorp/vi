package com.ctrip.framework.cs.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by jiang.j on 2016/8/22.
 * 用来描述点火插件
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Ignite {
    /**
     * 点火插件类型
     */
    public enum PluginType{
        /**
         * 应用
         */
        App,
        /**
         * 组件
         */
        Component
    }

    /**
     * 点火插件ID，必填且唯一
     * 使用bu base package code（参照02-03 各BU Base Package命名规范）作为前缀（例如fx.soa）
     * @return 点火插件ID
     */
    String id();

    /**
     * 选填，如果有值，必须在数组内点火插件运行之前执行当前点火插件
     * 点火时会校验这些ID，如果不存在或排序时出现循环都会导致点火失败
     * @return 点火插件ID数组
     */
    String[] before() default {};

    /**
     * 选填，如果有值，必须在数组内点火插件运行之后执行当前点火插件
     * 点火时会校验这些ID，如果不存在或排序时出现循环都会导致点火失败
     * @return 点火插件ID数组
     */
    String[] after() default {};

    /**
     * 选填，默认值为PluginType.App
     * type为PluginType.Component的点火插件执行优先级更高
     * @return 点火插件类型
     */
    PluginType type() default PluginType.App;

    /**
     * 选填，默认值为false
     * 当为true时，该点火插件会自动注册
     * @return 是否自动注册
     */
    boolean auto() default false;

}

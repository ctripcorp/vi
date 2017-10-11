package com.ctrip.framework.cs.spring;

import com.ctrip.framework.cs.servlet.VIFilter;
import com.ctrip.framework.cs.SysKeys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;

import javax.servlet.DispatcherType;

/**
 * Created by jiang.j on 2017/4/11.
 */
@Configuration
public class AutoConfiguration {

    private static final String URLPATTERNS = "/@in/*";
    @Configuration
    @Conditional(NoneFilterRegistrationCondition.class)
    static class ConfigurationNew {

        @Autowired
        Environment environment;
        @Bean(name = "VIFilterRegistrationBeanNew")
        public org.springframework.boot.web.servlet.FilterRegistrationBean factory() {

            if(environment != null) {
                String port = environment.getProperty("server.port");
                if(port !=null) {
                    System.setProperty(SysKeys.SPRINGBOOTPORTKEY, port);
                }
            }
            org.springframework.boot.web.servlet.FilterRegistrationBean filter =
                    new org.springframework.boot.web.servlet.FilterRegistrationBean();
            filter.setFilter(new VIFilter());
            filter.setName("vi-filter");
            filter.addUrlPatterns(URLPATTERNS);
            filter.setDispatcherTypes(DispatcherType.REQUEST, DispatcherType.FORWARD);
            filter.setAsyncSupported(true);
            filter.setOrder(Ordered.HIGHEST_PRECEDENCE);
            return filter;
        }
    }

    @Configuration
    @Conditional(FilterRegistrationCondition.class)
    static class ConfigurationOld {

        @Autowired
        Environment environment;
        @Bean(name = "VIFilterRegistrationBeanOld")
        public org.springframework.boot.context.embedded.FilterRegistrationBean factory() {

            if(environment != null) {
                String port = environment.getProperty("server.port");
                if(port !=null) {
                    System.setProperty(SysKeys.SPRINGBOOTPORTKEY, port);
                }
            }
            org.springframework.boot.context.embedded.FilterRegistrationBean filter =
                    new org.springframework.boot.context.embedded.FilterRegistrationBean();
                filter.setFilter(new VIFilter());
                filter.setName("vi-filter");
                filter.addUrlPatterns(URLPATTERNS);
                filter.setDispatcherTypes(DispatcherType.REQUEST, DispatcherType.FORWARD);
                filter.setAsyncSupported(true);
                filter.setOrder(Ordered.HIGHEST_PRECEDENCE);
            return filter;
        }
    }
}
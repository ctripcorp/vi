package com.ctrip.framework.cs.spring.example;

import com.ctrip.framework.cs.enterprise.EnFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.support.SpringBootServletInitializer;

import java.awt.*;
import java.net.URI;

/**
 * Created by jiang.j on 2017/4/12.
 */
@SpringBootApplication
@ServletComponentScan
public class ServiceStarter extends SpringBootServletInitializer {
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(ServiceStarter.class);
	}

    public static void main(String[] args) throws Exception {
        new SpringApplicationBuilder(ServiceStarter.class).run(args);

        System.setProperty("java.awt.headless", "false");
        Desktop.getDesktop().browse(new URI("http://127.0.0.1:9090/@in"));
    }

}

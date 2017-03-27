package com.verne.assignment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.DispatcherServlet;

@SpringBootApplication
public class DemoFileUploadApplication extends SpringBootServletInitializer{

	public static void main(String[] args) {
		SpringApplication.run(DemoFileUploadApplication.class, args);
	}

}

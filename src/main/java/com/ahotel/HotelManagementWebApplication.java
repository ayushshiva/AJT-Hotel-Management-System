package com.ahotel;

import com.ahotel.servlet.SystemStatusServlet;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class HotelManagementWebApplication extends SpringBootServletInitializer {
    public static void main(String[] args) {
        SpringApplication.run(HotelManagementWebApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(HotelManagementWebApplication.class);
    }

    @Bean
    ServletRegistrationBean<SystemStatusServlet> systemStatusServlet() {
        return new ServletRegistrationBean<>(new SystemStatusServlet(), "/legacy-status");
    }
}
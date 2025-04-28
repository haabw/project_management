package com.coop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder; // 추가
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer; // 추가

@SpringBootApplication
// SpringBootServletInitializer 상속 추가
public class CoopApplication extends SpringBootServletInitializer {

    // configure 메소드 오버라이드 추가
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(CoopApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(CoopApplication.class, args);
    }
}
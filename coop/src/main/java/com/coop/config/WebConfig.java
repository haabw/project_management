package com.coop.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// 스프링 MVC 설정 클래스
// css, js 경로 매핑 
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
    	// "/css/**"로 요청시 /static/css/ 디렉토리에서 css 제공 
        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/");
        // "/js/**"로 요청시 /static/js/ 디렉토리에서 js 제공 
        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/js/");
    }
}


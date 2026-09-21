package com.mofei.erp.framework.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String imagesPath = new File("images").getAbsolutePath() + File.separator;
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:" + imagesPath, "classpath:/static/images/");
        registry.addResourceHandler("/logo/**")
                .addResourceLocations("file:" + imagesPath + "logo" + File.separator, "classpath:/static/logo/");
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
    }
}

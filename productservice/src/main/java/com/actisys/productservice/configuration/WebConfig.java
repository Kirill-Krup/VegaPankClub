package com.actisys.productservice.configuration;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  @Value("${app.upload-dir}")
  private String uploadDir;

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    String userDir = System.getProperty("user.dir");
    Path uploadPath = Paths.get(userDir, "productservice", uploadDir).toAbsolutePath();
    String fileLocation = "file:" + uploadPath.toString() + "/";
    registry.addResourceHandler("/static/images/**")
        .addResourceLocations(fileLocation)
        .setCacheControl(CacheControl.maxAge(1, TimeUnit.DAYS));
  }
}



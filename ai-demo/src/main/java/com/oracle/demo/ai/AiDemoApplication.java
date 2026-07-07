package com.oracle.demo.ai;

import com.oracle.demo.ai.config.OciGenAiProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(OciGenAiProperties.class)
public class AiDemoApplication {
  public static void main(String[] args) {
    SpringApplication.run(AiDemoApplication.class, args);
  }
}

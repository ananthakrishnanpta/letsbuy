package com.letsbuy;

import com.letsbuy.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class LetsbuyApplication {

    public static void main(String[] args) {
        SpringApplication.run(LetsbuyApplication.class, args);
    }
}

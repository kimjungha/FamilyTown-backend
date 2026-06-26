package io.ahnlab.familytown;

import io.ahnlab.familytown.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class FamilytownApplication {
    public static void main(String[] args) {
        SpringApplication.run(FamilytownApplication.class, args);
    }
}

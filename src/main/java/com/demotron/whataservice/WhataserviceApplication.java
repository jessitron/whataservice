package com.demotron.whataservice;

import com.demotron.whataservice.otel.OtelConfiguration;
import io.opentelemetry.api.OpenTelemetry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class WhataserviceApplication {

    public static void main(String[] args) {
        System.out.println("Hello world....");
        SpringApplication.run(WhataserviceApplication.class, args);
    }

    @Bean
    public OpenTelemetry openTelemetry() {
        return OtelConfiguration.initOpenTelemetry();
    }

}

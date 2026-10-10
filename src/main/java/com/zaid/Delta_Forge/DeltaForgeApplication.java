package com.zaid.Delta_Forge;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@ConfigurationPropertiesScan

public class DeltaForgeApplication {
	@Bean
	ApplicationRunner checkJsonLimits(JsonLimitProperties limits) {
		return args -> System.out.println(limits);
	}
	public static void main(String[] args) {
		SpringApplication.run(DeltaForgeApplication.class, args);
	}

}

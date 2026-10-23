package com.planit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class PlanitApplication {

	public static void main(String[] args) {
		SpringApplication.run(PlanitApplication.class, args);
	}

}

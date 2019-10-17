package com.legeyda.mts;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@SpringBootApplication
@EnableAutoConfiguration
@ComponentScan(
		basePackages = { "com.legeyda.mts" },
		excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.legeyda\\.mts\\.gen.*"))
public class Application {
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
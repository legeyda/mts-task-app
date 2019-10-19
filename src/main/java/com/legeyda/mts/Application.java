package com.legeyda.mts;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableAutoConfiguration
@ComponentScan(
		basePackages = { "com.legeyda.mts" },
		excludeFilters = {
				@ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.legeyda\\.mts\\.gen.*"),
				@ComponentScan.Filter(type = FilterType.REGEX, pattern = "org\\.openapitools\\.configuration.*")
		}
)
@EnableSwagger2
public class Application {
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
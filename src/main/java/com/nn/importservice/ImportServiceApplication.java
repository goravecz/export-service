package com.nn.importservice;

import com.nn.importservice.config.FileSystemProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(FileSystemProperties.class)
public class ImportServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ImportServiceApplication.class, args);
	}

}

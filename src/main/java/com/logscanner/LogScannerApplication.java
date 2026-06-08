package com.logscanner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LogScannerApplication {

	public static void main(String[] args) {
		SpringApplication.run(LogScannerApplication.class, args);
	}

}

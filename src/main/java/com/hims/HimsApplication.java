package com.hims;


import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
@EntityScan
@Slf4j
public class HimsApplication extends SpringBootServletInitializer {


	public static void main(String[] args) {
		SpringApplication.run(HimsApplication.class, args);
		log.info("========== HIMS STARTED SUCCESSFULLY ==========");
	}
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return builder.sources(HimsApplication.class);
	}




}

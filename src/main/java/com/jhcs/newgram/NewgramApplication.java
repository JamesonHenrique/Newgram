package com.jhcs.newgram;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class NewgramApplication {

	public static void main(String[] args) {
		SpringApplication.run(NewgramApplication.class, args);
	}

}

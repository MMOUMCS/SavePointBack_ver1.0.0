package com.couple.gallery.couple_gallery_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@ComponentScan(basePackages = "com.couple.gallery")
@EntityScan(basePackages = "com.couple.gallery.couple_gallery_backend.domain")
public class CoupleGalleryBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(CoupleGalleryBackendApplication.class, args);
	}

}

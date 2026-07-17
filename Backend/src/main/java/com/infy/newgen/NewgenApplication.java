package com.infy.newgen;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;


@OpenAPIDefinition(
 	info = @Info(title = "Newgen Insurance API", version = "1", description="REST Endpoints For Newgen Insurance API"),
 	security = @SecurityRequirement(name = "Bearer Authentication")
)
@SecurityScheme(
 	name = "Bearer Authentication",
 	type = SecuritySchemeType.HTTP,
 	scheme = "bearer",
 	bearerFormat = "JWT"
)
@SpringBootApplication
@EnableAsync // Multithreading To Make OTP Flow Faster
public class NewgenApplication {

 	public static void main(String[] args) {
 		SpringApplication.run(NewgenApplication.class, args);
 	}
}
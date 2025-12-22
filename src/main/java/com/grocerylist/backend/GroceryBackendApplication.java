package com.grocerylist.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class GroceryBackendApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(GroceryBackendApplication.class, args);
    }
    
    @GetMapping("/")
    public String home() {
        return "Grocery List Backend API - Service is running!";
    }
    
    @GetMapping("/health")
    public String health() {
        return "OK";
    }
}
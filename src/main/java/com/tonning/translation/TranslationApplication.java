// The main class for the API functions. SpringBoot automatically handles configuration and component scanning

package com.tonning.translation;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TranslationApplication {
    public static void main(String[] args) {
        SpringApplication.run(TranslationApplication.class, args);
    }
}
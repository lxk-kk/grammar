package com.compiler.grammar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.CrossOrigin;

/**
 * @author 10652
 */
@SpringBootApplication
@CrossOrigin(origins = "*")
public class GrammarApplication {
    public static void main(String[] args) {
        SpringApplication.run(GrammarApplication.class, args);
    }
}

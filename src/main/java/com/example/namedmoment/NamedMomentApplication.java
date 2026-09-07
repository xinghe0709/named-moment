package com.example.namedmoment;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.example.namedmoment.mapper")
@SpringBootApplication
public class NamedMomentApplication {

    public static void main(String[] args) {
        SpringApplication.run(NamedMomentApplication.class, args);
    }
}

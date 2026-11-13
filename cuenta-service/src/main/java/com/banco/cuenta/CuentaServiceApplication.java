package com.banco.cuenta;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CuentaServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(CuentaServiceApplication.class, args);
        System.out.println("=== [cuenta-service] iniciado en el puerto 8081 ===");
    }
}

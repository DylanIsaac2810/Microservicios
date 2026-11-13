package com.banco.transaccion;

/**
 * Cuerpo que se envia a cuenta-service.
 * Cada microservicio tiene su propia copia de estos DTO a proposito:
 * compartir una libreria de clases entre servicios los volveria a acoplar.
 */
public record Operacion(double cantidad) {
}

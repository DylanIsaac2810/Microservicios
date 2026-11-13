package com.banco.transaccion;

import java.time.Instant;

/**
 * Registro del historial (dato que ESTE microservicio posee).
 * estado: EXITOSA | RECHAZADA (regla de negocio) | FALLIDA (cuenta-service no respondio)
 * saldo:  saldo reportado por cuenta-service tras la operacion (null si no hubo respuesta)
 */
public record Transaccion(
        long id,
        String cuentaId,
        String tipo,
        double cantidad,
        String estado,
        String detalle,
        Double saldo,
        Instant fecha) {
}

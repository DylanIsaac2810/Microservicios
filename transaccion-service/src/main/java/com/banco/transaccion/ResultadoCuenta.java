package com.banco.transaccion;

/** Respuesta de cuenta-service (el contrato JSON es lo unico que comparten). */
public record ResultadoCuenta(boolean ok, String mensaje, double saldo) {
}

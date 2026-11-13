package com.banco.transaccion;

/** Lo que envia el cliente. tipo: DEPOSITO | RETIRO */
public record SolicitudTransaccion(String cuentaId, String tipo, double cantidad) {
}

package com.banco.transaccion;

import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class TransaccionServicio {

    private final CuentaClient cuentas;
    private final AtomicLong secuencia = new AtomicLong();
    private final List<Transaccion> historial = new CopyOnWriteArrayList<>();

    public TransaccionServicio(CuentaClient cuentas) {
        this.cuentas = cuentas;
    }

    public Transaccion procesar(SolicitudTransaccion s) {
        String tipo = s.tipo() == null ? "" : s.tipo().toUpperCase();

        // Validaciones locales: no hace falta molestar a cuenta-service
        if (s.cuentaId() == null || s.cuentaId().isBlank()) {
            return registrar(s, tipo, "RECHAZADA", "Falta la cuenta", null);
        }
        if (!tipo.equals("DEPOSITO") && !tipo.equals("RETIRO")) {
            return registrar(s, tipo, "RECHAZADA", "Tipo invalido (use DEPOSITO o RETIRO)", null);
        }
        if (s.cantidad() <= 0) {
            return registrar(s, tipo, "RECHAZADA", "La cantidad debe ser mayor que cero", null);
        }

        // Llamada al otro microservicio
        try {
            ResultadoCuenta r = cuentas.aplicar(s.cuentaId(), tipo, s.cantidad());
            return registrar(s, tipo, r.ok() ? "EXITOSA" : "RECHAZADA", r.mensaje(), r.saldo());
        } catch (HttpClientErrorException e) {
            // cuenta-service respondio 4xx (por ejemplo, la cuenta no existe)
            return registrar(s, tipo, "RECHAZADA", "La cuenta " + s.cuentaId() + " no existe", null);
        } catch (ResourceAccessException | HttpServerErrorException e) {
            // cuenta-service caido o no responde: este servicio sigue vivo
            return registrar(s, tipo, "FALLIDA", "cuenta-service no disponible", null);
        }
    }

    /** Del mas reciente al mas antiguo; cuentaId opcional para filtrar. */
    public List<Transaccion> historial(String cuentaId) {
        List<Transaccion> copia = new ArrayList<>(historial);
        if (cuentaId != null && !cuentaId.isBlank()) {
            copia.removeIf(t -> !cuentaId.equals(t.cuentaId()));
        }
        Collections.reverse(copia);
        return copia;
    }

    private Transaccion registrar(SolicitudTransaccion s, String tipo, String estado,
                                  String detalle, Double saldo) {
        Transaccion t = new Transaccion(
                secuencia.incrementAndGet(),
                s.cuentaId(),
                tipo,
                s.cantidad(),
                estado,
                detalle,
                saldo,
                Instant.now().truncatedTo(ChronoUnit.MILLIS));
        historial.add(t);
        System.out.println("[transaccion-service] (" + Thread.currentThread().getName() + ") #" + t.id()
                + " " + t.tipo() + " $" + t.cantidad() + " cuenta " + t.cuentaId()
                + " -> " + t.estado() + " (" + t.detalle() + ")");
        return t;
    }
}

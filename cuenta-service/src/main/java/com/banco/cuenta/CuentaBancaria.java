package com.banco.cuenta;

/**
 * Modelo de negocio (igual que en practicas anteriores).
 * El saldo es el dato que ESTE microservicio posee: nadie mas lo toca directamente.
 */
public class CuentaBancaria {
    private double saldo;

    public CuentaBancaria(double saldoInicial) {
        this.saldo = saldoInicial;
    }

    public synchronized boolean depositar(double cantidad) {
        if (cantidad <= 0) {
            return false;
        }
        this.saldo += cantidad;
        return true;
    }

    public synchronized boolean retirar(double cantidad) {
        if (cantidad <= 0 || this.saldo < cantidad) {
            return false;
        }
        this.saldo -= cantidad;
        return true;
    }

    public synchronized double consultarSaldo() {
        return saldo;
    }
}

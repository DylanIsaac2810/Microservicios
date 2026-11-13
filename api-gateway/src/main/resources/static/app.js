"use strict";

/*
 * Cliente de la Billetera (Practica 8).
 * Solo conoce UNA direccion: el api-gateway (el mismo origen que sirve esta pagina).
 *   /api/cuentas/{id}/saldo   -> cuenta-service
 *   /api/transacciones        -> transaccion-service
 * Cada pocos segundos pregunta a cada servicio si responde y lo muestra en el panel.
 */

const INTERVALO_MS = 3000;

const el = {
  servicios: document.getElementById("servicios"),
  cuenta: document.getElementById("cuenta"),
  saldo: document.getElementById("saldo"),
  form: document.getElementById("form"),
  cantidad: document.getElementById("cantidad"),
  lista: document.getElementById("lista"),
  toast: document.getElementById("toast"),
  btnInstalar: document.getElementById("btn-instalar")
};

const dinero = new Intl.NumberFormat("es-MX", { style: "currency", currency: "MXN" });
const hora = new Intl.DateTimeFormat("es-MX", { dateStyle: "short", timeStyle: "medium" });

/* ---------- Interfaz ---------- */

function mostrarToast(texto, esError) {
  el.toast.textContent = texto;
  el.toast.className = "toast" + (esError ? " toast--error" : "");
  el.toast.hidden = false;
  clearTimeout(mostrarToast.t);
  mostrarToast.t = setTimeout(() => { el.toast.hidden = true; }, 3500);
}

/* estado: "activo" | "caido" | "desconocido" */
function marcarServicio(nombre, estado) {
  const fila = el.servicios.querySelector('[data-servicio="' + nombre + '"]');
  if (!fila) return;
  fila.querySelector(".punto").className = "punto punto--" + estado;
  const texto = fila.querySelector(".serv__estado");
  texto.textContent = { activo: "activo", caido: "caído", desconocido: "—" }[estado];
  texto.className = "serv__estado estado--" + estado;
}

function pintarHistorial(lista) {
  el.lista.replaceChildren();
  if (lista.length === 0) {
    const li = document.createElement("li");
    li.className = "vacio";
    li.textContent = "Sin transacciones todavía.";
    el.lista.append(li);
    return;
  }
  lista.forEach((t) => {
    const li = document.createElement("li");

    const izq = document.createElement("div");
    const tipo = document.createElement("span");
    tipo.className = "mov__tipo";
    tipo.textContent = (t.tipo === "DEPOSITO" ? "Depósito" : t.tipo === "RETIRO" ? "Retiro" : t.tipo) +
      " · cuenta " + t.cuentaId;
    const etiqueta = document.createElement("span");
    etiqueta.className = "etiqueta etiqueta--" + t.estado;
    etiqueta.textContent = t.estado;
    tipo.append(etiqueta);
    const meta = document.createElement("span");
    meta.className = "mov__meta";
    meta.textContent = "#" + t.id + " · " + hora.format(new Date(t.fecha)) + " · " + t.detalle;
    izq.append(tipo, meta);

    const monto = document.createElement("span");
    monto.className = "mov__monto";
    monto.textContent = dinero.format(t.cantidad);

    li.append(izq, monto);
    el.lista.append(li);
  });
}

/* ---------- Consultas (cada una revela el estado de un servicio) ---------- */

/* Devuelve "activo" | "caido" a partir de la respuesta del gateway. */
function estadoDe(resp) {
  return resp.status === 502 || resp.status === 503 || resp.status === 504 ? "caido" : "activo";
}

async function consultarCuenta() {
  const resp = await fetch("/api/cuentas/" + encodeURIComponent(el.cuenta.value) + "/saldo");
  const estado = estadoDe(resp);
  marcarServicio("cuenta-service", estado);
  if (estado === "activo" && resp.ok) {
    const datos = await resp.json();
    el.saldo.textContent = dinero.format(datos.saldo);
  } else {
    el.saldo.textContent = "$ —";
  }
}

async function consultarTransacciones() {
  const resp = await fetch("/api/transacciones");
  const estado = estadoDe(resp);
  marcarServicio("transaccion-service", estado);
  if (estado === "activo" && resp.ok) {
    pintarHistorial(await resp.json());
  }
}

async function actualizar() {
  try {
    await Promise.all([consultarCuenta(), consultarTransacciones()]);
    marcarServicio("api-gateway", "activo");
  } catch (e) {
    // fetch falla solo si no se alcanza ni el gateway
    marcarServicio("api-gateway", "caido");
    marcarServicio("cuenta-service", "desconocido");
    marcarServicio("transaccion-service", "desconocido");
    el.saldo.textContent = "$ —";
  }
}

/* ---------- Transacciones ---------- */

async function enviar(tipo, cantidad) {
  try {
    const resp = await fetch("/api/transacciones", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ cuentaId: el.cuenta.value, tipo, cantidad })
    });
    let t = null;
    try { t = await resp.json(); } catch (e) { /* respuesta sin JSON */ }

    if (t && t.estado) {
      mostrarToast(t.estado + ": " + t.detalle, t.estado !== "EXITOSA");
    } else {
      mostrarToast("Servicio no disponible (" + resp.status + ")", true);
    }
  } catch (e) {
    mostrarToast("No se pudo contactar al api-gateway.", true);
  }
  actualizar();
}

el.form.addEventListener("submit", (ev) => {
  ev.preventDefault();
  const tipo = ev.submitter && ev.submitter.dataset.tipo;
  const cantidad = parseFloat(el.cantidad.value);
  if (!tipo) return;
  if (!(cantidad > 0)) {
    mostrarToast("Escribe una cantidad mayor que cero.", true);
    el.cantidad.focus();
    return;
  }
  enviar(tipo, cantidad);
  el.cantidad.value = "";
});

el.cuenta.addEventListener("change", actualizar);

/* ---------- Instalacion (PWA) ---------- */

let promptInstalar = null;
window.addEventListener("beforeinstallprompt", (ev) => {
  ev.preventDefault();
  promptInstalar = ev;
  el.btnInstalar.hidden = false;
});
el.btnInstalar.addEventListener("click", async () => {
  if (!promptInstalar) return;
  promptInstalar.prompt();
  await promptInstalar.userChoice;
  promptInstalar = null;
  el.btnInstalar.hidden = true;
});
window.addEventListener("appinstalled", () => { el.btnInstalar.hidden = true; });

/* ---------- Arranque ---------- */

if ("serviceWorker" in navigator) {
  window.addEventListener("load", () => {
    navigator.serviceWorker.register("sw.js").catch((e) => console.error("SW no registrado", e));
  });
}

actualizar();
setInterval(actualizar, INTERVALO_MS);

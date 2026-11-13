/*
 * Service Worker - Practica 8
 *  - Interfaz (HTML, CSS, JS, iconos): cache-first, para abrir la app sin red.
 *  - /api/**: NUNCA se cachea. El panel de estado debe reflejar la realidad:
 *    si un microservicio esta caido, la interfaz debe verlo, no una copia vieja.
 */
const VERSION = "v1";
const CACHE = "billetera-p8-" + VERSION;

const SHELL = [
  "./",
  "index.html",
  "styles.css",
  "app.js",
  "manifest.json",
  "icons/icon-192.png",
  "icons/icon-512.png"
];

self.addEventListener("install", (event) => {
  event.waitUntil(
    caches.open(CACHE).then((cache) => cache.addAll(SHELL)).then(() => self.skipWaiting())
  );
});

self.addEventListener("activate", (event) => {
  event.waitUntil(
    caches.keys()
      .then((claves) => Promise.all(claves.filter((k) => k !== CACHE).map((k) => caches.delete(k))))
      .then(() => self.clients.claim())
  );
});

self.addEventListener("fetch", (event) => {
  const req = event.request;
  if (req.method !== "GET") return;

  const url = new URL(req.url);
  if (url.origin !== self.location.origin) return;
  if (url.pathname.startsWith("/api/")) return;   // directo a la red

  event.respondWith(
    caches.match(req).then((enCache) => {
      if (enCache) return enCache;
      return fetch(req).catch(() => {
        if (req.mode === "navigate") return caches.match("index.html");
        throw new Error("sin red");
      });
    })
  );
});

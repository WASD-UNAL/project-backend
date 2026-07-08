# Comunicación con Mercado Pago vía ngrok

Mercado Pago necesita **URLs públicas** para dos cosas:

1. **Webhook (`notification_url`)** → así Mercado Pago avisa a tu backend cuando un
   pago se aprueba/rechaza y la membresía se activa **automáticamente**.
2. **`back_urls` + `auto_return`** → así el usuario vuelve a tu página desde el
   checkout de Mercado Pago.

En `localhost` Mercado Pago rechaza ambas cosas, por eso en desarrollo el pago
queda `PENDING` hasta que el `PaymentReconciliationScheduler` lo reconcilia por
polling o el admin lo aprueba a mano. Con ngrok, en cambio, la confirmación es
inmediata.

## Arquitectura: un solo túnel

El plan gratuito de ngrok da **un dominio**. Un dominio solo puede enrutar a un
puerto, así que **no** se pueden exponer backend (8080) y frontend (5173) a la vez
con el mismo dominio.

La solución es exponer **solo el frontend (Vite, 5173)** y que Vite reenvíe las
rutas `/api` al backend (8080) con su proxy (ya configurado en `vite.config.ts`).
Así todo comparte el mismo dominio público:

```
                                   ┌─────────────── Vite dev server (5173) ───────────────┐
navegador / Mercado Pago ─ ngrok ─▶│  /            → app React (SPA)                        │
   https://<dominio>.ngrok-free.dev│  /api/**      → proxy → backend Spring (8080) /api/**  │
                                   └───────────────────────────────────────────────────────┘
```

- `notification_url` = `https://<dominio>/api/payments/webhook` → ngrok → Vite → backend.
- `back_urls` = `https://<dominio>/dashboard?payment=…` → la SPA.

## 1. Levantar el túnel

```bash
# En project-backend/
cp ngrok.yml.example ngrok.yml     # pon tu authtoken y tu dominio reservado
ngrok start app --config ngrok.yml # apunta a 5173
```

> En el plan gratuito, reserva un dominio fijo en
> https://dashboard.ngrok.com/domains para que la URL no cambie en cada arranque.

## 2. Configurar los `.env`

**`project-backend/.env`** (mismo dominio en las dos; se usa para
`notification_url` y `back_urls`):

```env
BACKEND_URL=https://<dominio>.ngrok-free.dev
FRONTEND_URL=https://<dominio>.ngrok-free.dev
```

**`project-frontend/.env`** (ruta relativa; sale al mismo origen y Vite la
reenvía al backend, funciona igual en local y por ngrok):

```env
VITE_API_URL=/api
```

## 3. Levantar backend y frontend

```bash
# Backend  (project-backend/)  — recarga el .env en cada arranque
./gradlew bootRun

# Frontend (project-frontend/)
npm run dev
```

> Importante: si editas el `.env` con el backend ya corriendo, **reinícialo**;
> las variables se leen solo al arrancar. Si `BACKEND_URL` sigue en `localhost`,
> la preferencia se crea **sin** `notification_url`.

Abre la app usando la **URL pública de ngrok**, no `localhost`, para que el
retorno desde Mercado Pago caiga en tu página.

## 4. Configurar el webhook en el panel (opcional)

El backend ya envía `notification_url` en cada preferencia, así que funciona sin
tocar el panel. Si además quieres registrarlo de forma permanente:

- Panel de Mercado Pago → *Tus integraciones* → tu aplicación → **Webhooks**.
- URL: `https://<dominio>.ngrok-free.dev/api/payments/webhook`
- Evento: **Pagos** (`payment`).

## Cómo se procesa una notificación

- Mercado Pago hace `POST https://<dominio>/api/payments/webhook`.
- El endpoint es público (no requiere JWT) y acepta tanto el cuerpo JSON del
  formato Webhooks moderno como los query-params del formato IPN clásico
  (`?topic=payment&id=...`).
- `WebhookService` consulta el pago real en Mercado Pago y, si está `approved`,
  activa la membresía. El estado se refleja en la vista del usuario (polling tras
  el retorno) y en la del admin (auto-refresh cada 15 s).

## Cómo verificarlo (comprobado en esta rama)

Tras crear un checkout con tarjeta, puedes leer la preferencia real:

```bash
# El pref_id sale del checkoutUrl (…redirect?pref_id=XXXX)
curl -s -H "Authorization: Bearer $MERCADOPAGO_ACCESS_TOKEN" \
  https://api.mercadopago.com/checkout/preferences/<pref_id>
```

Debe incluir:

```json
{
  "notification_url": "https://<dominio>.ngrok-free.dev/api/payments/webhook",
  "auto_return": "all",
  "back_urls": { "success": "https://<dominio>.ngrok-free.dev/dashboard?payment=success", "...": "..." }
}
```

## Checklist

- [ ] `ngrok start app` muestra el túnel `online` apuntando a `5173`.
- [ ] `https://<dominio>/` sirve la app (200) y `https://<dominio>/api/plans` devuelve JSON.
- [ ] `BACKEND_URL`/`FRONTEND_URL` (backend) apuntan a ngrok; backend reiniciado.
- [ ] La preferencia creada incluye `notification_url` y `auto_return`.
- [ ] Pagas con una [tarjeta de prueba aprobada](https://www.mercadopago.com.co/developers/es/docs/checkout-api/additional-content/your-integrations/test/cards).
- [ ] En la consola de ngrok ves el `POST /api/payments/webhook` con `200 OK`.
- [ ] La membresía pasa a activa sin intervención del admin.

# Comunicación con Mercado Pago vía ngrok

Mercado Pago necesita **URLs públicas** para dos cosas:

1. **Webhook (`notification_url`)** → así Mercado Pago avisa a tu backend cuando un
   pago se aprueba/rechaza y la membresía se activa **automáticamente**.
2. **`back_urls` + `auto_return`** → así el usuario vuelve a tu página desde el
   checkout de Mercado Pago.

En `localhost` Mercado Pago rechaza ambas cosas, por eso en desarrollo el pago
queda `PENDING` hasta que el `PaymentReconciliationScheduler` lo reconcilia por
polling (cada 20 s) o el admin lo aprueba a mano. Con ngrok, en cambio, la
confirmación es inmediata.

## 1. Levantar los túneles

```bash
# En project-backend/
cp ngrok.yml.example ngrok.yml   # y pon tu authtoken dentro
ngrok start --all --config ngrok.yml
```

ngrok mostrará dos URLs, por ejemplo:

```
backend   https://abc123.ngrok-free.app  -> http://localhost:8080
frontend  https://xyz789.ngrok-free.app  -> http://localhost:5173
```

> En el plan gratuito las URLs cambian cada vez que reinicias ngrok: hay que
> actualizar los `.env` con las nuevas. Con un dominio reservado son fijas.

## 2. Configurar los `.env`

**`project-backend/.env`**

```env
BACKEND_URL=https://abc123.ngrok-free.app
FRONTEND_URL=https://xyz789.ngrok-free.app
```

**`project-frontend/.env`** (ojo: incluye el sufijo `/api`)

```env
VITE_API_URL=https://abc123.ngrok-free.app/api
```

## 3. Levantar backend y frontend

```bash
# Backend  (project-backend/)  — recarga el .env en cada arranque
./gradlew bootRun

# Frontend (project-frontend/) — Vite ya permite el host de ngrok (allowedHosts)
npm run dev
```

Abre la app usando la **URL de ngrok del frontend** (`https://xyz789.ngrok-free.app`),
no `localhost`. Así el retorno desde Mercado Pago y las cookies/headers funcionan
en el mismo origen público.

## 4. Configurar el webhook en el panel de Mercado Pago (opcional pero recomendado)

El backend ya envía `notification_url` en cada preferencia, así que funciona sin
tocar el panel. Si además quieres registrarlo de forma permanente:

- Panel de Mercado Pago → *Tus integraciones* → tu aplicación → **Webhooks**.
- URL: `https://abc123.ngrok-free.app/api/payments/webhook`
- Evento: **Pagos** (`payment`).

## Cómo se procesa una notificación

- Mercado Pago hace `POST https://<backend>/api/payments/webhook`.
- El endpoint es público (no requiere JWT) y acepta tanto el cuerpo JSON del
  formato Webhooks moderno como los query-params del formato IPN clásico
  (`?topic=payment&id=...`).
- `WebhookService` consulta el pago real en Mercado Pago y, si está `approved`,
  activa la membresía. El estado se refleja en la vista del usuario (polling tras
  el retorno) y en la del admin (auto-refresh cada 15 s).

## Checklist de verificación

- [ ] `ngrok start --all` muestra los dos túneles `online`.
- [ ] `BACKEND_URL`/`FRONTEND_URL` (backend) y `VITE_API_URL` (frontend) apuntan a ngrok.
- [ ] Abres la app por la URL de ngrok del frontend.
- [ ] Pagas con una [tarjeta de prueba aprobada](https://www.mercadopago.com.co/developers/es/docs/checkout-api/additional-content/your-integrations/test/cards).
- [ ] En la consola de ngrok ves el `POST /api/payments/webhook` con `200 OK`.
- [ ] La membresía pasa a activa sin intervención del admin.

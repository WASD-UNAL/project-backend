package app.gymly.service.payment

import com.mercadopago.client.payment.PaymentClient
import com.mercadopago.client.preference.PreferenceBackUrlsRequest
import com.mercadopago.client.preference.PreferenceClient
import com.mercadopago.client.preference.PreferenceItemRequest
import com.mercadopago.client.preference.PreferenceRequest
import com.mercadopago.exceptions.MPApiException
import com.mercadopago.exceptions.MPException
import com.mercadopago.net.MPSearchRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal
import com.mercadopago.resources.payment.Payment as MercadoPagoPayment

@Service
class MercadoPagoService(
    @Value("\${app.frontend-url}") private val frontendUrl: String,
    @Value("\${app.backend-url}") private val backendUrl: String,
) {
    private fun isPublicUrl(url: String): Boolean = !url.contains("localhost") && !url.contains("127.0.0.1")

    fun getPayment(mpPaymentId: Long): MercadoPagoPayment {
        try {
            return PaymentClient().get(mpPaymentId)
        } catch (e: MPApiException) {
            throw ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Error al consultar el pago en Mercado Pago: ${e.apiResponse.content}",
                e,
            )
        } catch (e: MPException) {
            throw ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Error interno de Mercado Pago SDK",
                e,
            )
        }
    }

    fun searchPaymentsByExternalReference(externalReference: String): List<MercadoPagoPayment> {
        try {
            val request =
                MPSearchRequest
                    .builder()
                    .offset(0)
                    .limit(30)
                    .filters(mapOf("external_reference" to externalReference))
                    .build()
            return PaymentClient().search(request).results ?: emptyList()
        } catch (e: MPApiException) {
            throw ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Error al buscar pagos en Mercado Pago: ${e.apiResponse.content}",
                e,
            )
        } catch (e: MPException) {
            throw ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Error interno de Mercado Pago SDK",
                e,
            )
        }
    }

    fun createPaymentLink(
        title: String,
        price: BigDecimal,
        externalReference: String,
    ): String {
        try {
            val client = PreferenceClient()

            val item =
                PreferenceItemRequest
                    .builder()
                    .title(title)
                    .quantity(1)
                    .unitPrice(price)
                    .currencyId("COP")
                    .build()

            val backUrls =
                PreferenceBackUrlsRequest
                    .builder()
                    .success("$frontendUrl/dashboard?payment=success")
                    .pending("$frontendUrl/dashboard?payment=pending")
                    .failure("$frontendUrl/dashboard?payment=failure")
                    .build()

            val builder =
                PreferenceRequest
                    .builder()
                    .items(listOf(item))
                    .externalReference(externalReference)
                    .backUrls(backUrls)

            if (isPublicUrl(backendUrl)) {
                builder.notificationUrl("$backendUrl/api/payments/webhook")
            }

            if (isPublicUrl(frontendUrl)) {
                builder.autoReturn("all")
            }

            val request = builder.build()

            val preference = client.create(request)
            return preference.initPoint
        } catch (e: MPApiException) {
            throw ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Error en la API de Mercado Pago: ${e.apiResponse.content}",
                e,
            )
        } catch (e: MPException) {
            throw ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Error interno de Mercado Pago SDK",
                e,
            )
        }
    }
}

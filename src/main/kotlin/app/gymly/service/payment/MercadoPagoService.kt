package app.gymly.service.payment

import com.mercadopago.client.preference.PreferenceBackUrlsRequest
import com.mercadopago.client.preference.PreferenceClient
import com.mercadopago.client.preference.PreferenceItemRequest
import com.mercadopago.client.preference.PreferenceRequest
import com.mercadopago.exceptions.MPApiException
import com.mercadopago.exceptions.MPException
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal

@Service
class MercadoPagoService(
    @Value("\${app.frontend-url}") private val frontendUrl: String,
) {
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

            val request =
                PreferenceRequest
                    .builder()
                    .items(listOf(item))
                    .externalReference(externalReference)
                    .backUrls(backUrls)
                    .autoReturn("all")
                    .build()

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

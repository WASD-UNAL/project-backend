package app.gymly.converter

import app.gymly.model.PaymentMethod
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter(autoApply = true)
class PaymentMethodConverter : AttributeConverter<PaymentMethod, String> {
    override fun convertToDatabaseColumn(attribute: PaymentMethod?): String? = attribute?.dbValue

    override fun convertToEntityAttribute(dbData: String?): PaymentMethod? = dbData?.let { PaymentMethod.fromDb(it) }
}

package app.gymly.converter

import app.gymly.model.PaymentStatus
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter(autoApply = true)
class PaymentStatusConverter : AttributeConverter<PaymentStatus, String> {
    override fun convertToDatabaseColumn(attribute: PaymentStatus?): String? = attribute?.dbValue

    override fun convertToEntityAttribute(dbData: String?): PaymentStatus? = dbData?.let { PaymentStatus.fromDb(it) }
}

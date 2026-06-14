package app.gymly.converter

import app.gymly.model.MembershipStatus
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter(autoApply = true)
class MembershipStatusConverter : AttributeConverter<MembershipStatus, String> {
    override fun convertToDatabaseColumn(attribute: MembershipStatus?): String? = attribute?.dbValue

    override fun convertToEntityAttribute(dbData: String?): MembershipStatus? = dbData?.let { MembershipStatus.fromDb(it) }
}

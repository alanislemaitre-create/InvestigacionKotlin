package org.example.tiendaapp.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.doubleOrNull

object DoubleOrStringSerializer : KSerializer<Double> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("DoubleOrString", PrimitiveKind.DOUBLE)

    override fun deserialize(decoder: Decoder): Double {
        return if (decoder is JsonDecoder) {
            val element = decoder.decodeJsonElement()
            if (element is JsonPrimitive) {
                element.doubleOrNull
                    ?: element.content.toDoubleOrNull()
                    ?: 0.0
            } else {
                0.0
            }
        } else {
            try {
                decoder.decodeDouble()
            } catch (e: Exception) {
                0.0
            }
        }
    }

    override fun serialize(encoder: Encoder, value: Double) {
        encoder.encodeDouble(value)
    }
}

@Serializable
data class Producto(
    val id: Int = 0,
    val nombre: String = "",
    val descripcion: String = "",
    @Serializable(with = DoubleOrStringSerializer::class)
    val precio: Double = 0.0,
    val stock: Int = 0,
    val activo: Boolean = true
)

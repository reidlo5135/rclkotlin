package net.wavem.rclkotlin.rosidl.domain

enum class RCLBufferType(val type : String) {
    STRING("kotlin.String"),
    BYTE("kotlin.Byte"),
    UBYTE("kotlin.UByte"),
    INT("kotlin.Int"),
    SHORT("kotlin.Short"),
    USHORT("kotlin.UShort"),
    LONG("kotlin.Long"),
    DOUBLE("kotlin.Double"),
    FLOAT("kotlin.Float"),
    DOUBLEARRAY("kotlin.DoubleArray"),
    FLOATARRAY("kotlin.FloatArray"),
    BOOLEAN("kotlin.Boolean"),
    BOOLEANARRAY("kotlin.BooleanArray");

    companion object {
        val TYPE_NAME_MAP : Map<String, RCLBufferType> = entries.associateBy { it.type }
    }
}
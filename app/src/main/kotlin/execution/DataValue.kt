package ru.tsaplev.app.execution

sealed class DataValue {
    data class IntValue(val value: Int): DataValue()
    data class StringValue(val value: String): DataValue()
    data class ArrayValue(val elems: MutableList<DataValue>): DataValue()
}

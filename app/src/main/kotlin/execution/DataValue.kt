package ru.tsaplev.app.execution

sealed class DataValue {
    class IntValue(val value: Int): DataValue()
    class StringValue(val value: String): DataValue()
    class ArrayValue(val elems: MutableList<DataValue>): DataValue()
}
package ru.tsaplev.app.execution

interface DataIO {
    fun write(output: DataValue?, end: String="\n")
    fun read(): DataValue
}
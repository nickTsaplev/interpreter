package ru.tsaplev.app.execution

interface IntIO {
    fun write(output: Int?)
    fun read(): Int
}
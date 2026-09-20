package ru.tsaplev.app.execution

class StandardIO: IntIO {
    override fun write(output: Int?) {
        println(output)
    }

    override fun read(): Int {
        return readln().toInt()
    }
}
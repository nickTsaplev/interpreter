import ru.tsaplev.app.execution.IntIO

class ListIOMock(val input: MutableList<Int>,
                 val output: MutableList<Int?> = mutableListOf<Int?>()) : IntIO {
    private var count = 0
    override fun write(output: Int?) {
        this.output.add(output)
    }

    override fun read(): Int {
        return input[count++]
    }
}
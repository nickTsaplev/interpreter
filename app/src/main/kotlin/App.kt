package ru.tsaplev.app

import ru.tsaplev.app.JSONToAST.JSONToBinop
import ru.tsaplev.utils.Printer
import java.io.File

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
fun main(args: Array<String>) {
    val text = File(args[0]).readText(Charsets.UTF_8)
    val jsonToASTer = JSONToBinop();

}

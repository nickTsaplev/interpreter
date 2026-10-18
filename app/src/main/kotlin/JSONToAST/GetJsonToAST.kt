package ru.tsaplev.app.JSONToAST

fun GetJsonToAST(): JsonToASTer {
    return JsonToBinop()
        .addNext(JsonToConst())
        .addNext(JsonToWrite())
        .addNext(JsonToSeq())
        .addNext(JsonToAssign())
        .addNext(JsonToVar())
        .addNext(JsonToRead())
        .addNext(JsonToSkip())
        .addNext(JsonToControl())
        .addNext(JsonToArray())
        .addNext(JsonToElem())
        .addNext(JsonToSet())
        .addNext(JsonToLength())
        .addNext(JsonToString())
}

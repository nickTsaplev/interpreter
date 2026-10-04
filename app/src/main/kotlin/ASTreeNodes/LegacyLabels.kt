package ru.tsaplev.app.ASTreeNodes

data class LegacyLoopLabels(val body: Int, val condition: Int)

data class LegacyIfLabels(val end: String, val elseLabel: String)

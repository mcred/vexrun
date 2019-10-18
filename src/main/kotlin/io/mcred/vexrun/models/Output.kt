package io.mcred.vexrun.models

data class Output(
        val type:  Type,
        val compare: Compare,
        var expected: String
) {
    enum class Type(val value: String){
        STDOUT("STDOUT"),
        STDERR("STDERR");
    }
    enum class Compare(val value: String){
        EQUALS("EQUALS"),
        CONTAINS("CONTAINS");
    }
}
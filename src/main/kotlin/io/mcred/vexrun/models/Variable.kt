package io.mcred.vexrun.models

data class Variable(
    val key: String,
    val value: String,
    val type: Type
) {
    enum class Type(val value: String){
        SYSTEM("SYSTEM"),
        PARAM("PARAM"),
        RESULT("RESULT");
    }
}
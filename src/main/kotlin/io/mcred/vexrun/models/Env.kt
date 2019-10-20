package io.mcred.vexrun.models

data class Env(
        val operation: Operation,
        val key: String,
        val type: Type,
        val pattern: Map<String, String>?
) {
    enum class Operation(val value: String) {
        GET("GET"),
        SET("SET")
    }
    enum class Type(val value: String) {
        STRING("STRING"),
        REPLACE("REPLACE")
    }
    companion object{
        fun Env.getVariableFromResult(result: Result, type: Output.Type): Variable {
            val actual = when (type) {
                Output.Type.STDERR -> result.stderr!!
                else -> result.stdout!!
            }
            val value = when(this.type) {
                Type.REPLACE -> actual.replace("${this.pattern!!["find"]}", "${this.pattern["replace"]}")
                else -> actual
            }
            return Variable(this.key, value)
        }
    }
}
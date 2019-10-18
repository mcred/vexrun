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
        fun Env.getVariableFromResult(result: Result): Variable {
            val value = when(this.type) {
                Type.REPLACE -> result.stdout!!.replace("${this.pattern!!["find"]}", "${this.pattern["replace"]}")
                else -> result.stdout!!
            }
            return Variable(this.key, value)
        }
    }
}
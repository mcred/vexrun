package io.mcred.vexrun.models

data class Env(
        val operation: Operation,
        val key: String,
        val type: Type,
        val pattern: Map<String, Any>?
) {
    enum class Operation(val value: String) {
        GET("GET"),
        SET("SET")
    }
    enum class Type(val value: String) {
        STRING("STRING"),
        REPLACE("REPLACE"),
        SPLIT("SPLIT")
    }

    companion object{
        private fun String.replaceFromPattern(pattern: Map<String, Any>): String {
            val find = pattern["find"] as String
            val replace = pattern["replace"] as String
            return this.replace(find, replace)
        }
        private fun String.splitFromPattern(pattern: Map<String, Any>): String {
            val delimiter = pattern["delimiter"] as String
            val position = pattern["position"] as Int
            return this.split(delimiter)[position]
        }
        fun Env.getVariableFromResult(result: Result, type: Output.Type): Variable {
            val actual = when (type) {
                Output.Type.STDERR -> result.stderr!!
                else -> result.stdout!!
            }
            val value = when(this.type) {
                Type.REPLACE -> actual.replaceFromPattern(this.pattern!!)
                Type.SPLIT -> actual.splitFromPattern(this.pattern!!)
                else -> actual
            }
            return Variable(this.key, value, Variable.Type.RESULT)
        }
    }
}
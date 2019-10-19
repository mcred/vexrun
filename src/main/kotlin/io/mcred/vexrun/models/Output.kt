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
        EXCLUDES("EXCLUDES"),
        CONTAINS("CONTAINS");
    }

    companion object{
        fun Output.compare(result: Result): Boolean {
            val actual = when(this.type) {
                Type.STDOUT -> result.stdout!!
                else -> result.stderr!!
            }
            return when (this.compare) {
                Compare.EXCLUDES -> !actual.contains(this.expected)
                Compare.CONTAINS -> actual.contains(this.expected)
                else -> actual == this.expected
            }
        }
    }
}
package io.mcred.vexrun.models

import io.mcred.vexrun.utils.CommandExecutor

data class Output(
        val type:  Type,
        val compare: Compare,
        val expected: String
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

data class Test(
        val name: String,
        val command: String,
        val exitValue: Int,
        val wait: Int = 0,
        val outputs: List<Output>? = null
){
    companion object {
        fun Test.run(){
            print("${this.name}: ")
            val result = CommandExecutor().exec(this.command)
            var compare = 0
            val exitCompare = result.exitValue - this.exitValue
            if (exitCompare != 0) compare++
            if (!this.outputs.isNullOrEmpty()) {
                for (output in this.outputs) {
                    val outputCompare = when (output.compare) {
                        Output.Compare.CONTAINS -> result.stdout!!.contains(output.expected)
                        else -> result.stdout.equals(output.expected)
                    }
                    if (!outputCompare) {
                        compare++
                    }
                }
            }

            if (compare == 0) {
                println("passed")
            } else {
                println("failed")
                println(result)
            }
            Thread.sleep(wait.toLong() * 1000)
        }

        @JvmStatic
        fun loadFromFile(raw: Map<String, Any>): Test {
            val name = raw.keys.first()
            val obj = raw[name] as Map<String, Any>

            val outputList = mutableListOf<Output>()
            if (obj.containsKey("stdout")) {
                val rawOutput = obj["stdout"]
                if (rawOutput is String) {
                    val output = Output(
                        Output.Type.STDOUT,
                        Output.Compare.EQUALS,
                        rawOutput
                    )
                    outputList.add(output)
                } else {
                    val outputMap = rawOutput as Map<String, Any>
                    if (outputMap["contains"] is String) {
                        val output = Output(
                            Output.Type.STDOUT,
                            Output.Compare.CONTAINS,
                            outputMap["contains"] as String
                        )
                        outputList.add(output)
                    } else {
                        val outputArray = outputMap["contains"] as List<String>
                        for (outputItem in outputArray) {
                            val output = Output(
                                Output.Type.STDOUT,
                                Output.Compare.CONTAINS,
                                outputItem
                            )
                            outputList.add(output)
                        }
                    }
                }
            }

            return Test(
                name,
                obj["command"] as String,
                obj["exitValue"] as Int,
                if(obj.containsKey("wait")) obj["wait"] as Int else 0,
                if(outputList.isNotEmpty()) outputList else null
            )
        }
    }
}
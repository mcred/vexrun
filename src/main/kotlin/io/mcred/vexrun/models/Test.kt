package io.mcred.vexrun.models

import io.mcred.vexrun.utils.CommandExecutor

data class Test(
        val name: String,
        val command: String,
        val exitValue: Int
){

    companion object {
        fun Test.run(){
            val result = CommandExecutor().exec(this.command)

            if (result.exitValue == this.exitValue) {
                println("$name: passed")
            } else {
                println("$name: failed")
            }
        }

        @JvmStatic
        fun loadFromFile(raw: Map<String, Any>): Test {
            val name = raw.keys.first()
            val obj = raw[name] as Map<String, Any>
            return Test(
                name,
                obj["command"] as String,
                obj["exitValue"] as Int
            )
        }
    }
}
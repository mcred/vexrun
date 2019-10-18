package io.mcred.vexrun.models

import io.mcred.vexrun.utils.CommandExecutor

data class Test(
        val name: String,
        val command: String,
        val exitValue: Int,
        val wait: Int = 0
){

    companion object {
        fun Test.run(){
            print("${this.name}: ")
            val result = CommandExecutor().exec(this.command)
            if (result.exitValue == this.exitValue) {
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
            return Test(
                name,
                obj["command"] as String,
                obj["exitValue"] as Int,
                if(obj.containsKey("wait")) obj["wait"] as Int else 0
            )
        }
    }
}
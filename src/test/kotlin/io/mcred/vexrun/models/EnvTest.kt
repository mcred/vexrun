package io.mcred.vexrun.models

import org.junit.Test
import kotlin.test.assertEquals
import io.mcred.vexrun.models.Env.Companion.getVariableFromResult

class EnvTest {
    private val result = Result(0, "Commit 6fbbb964f8e940adbbd80278ca971b91", null)

    @Test
    fun `can get replace variable from result`(){
        val envSet = Env( Env.Operation.SET, "COMMIT_GUID", Env.Type.REPLACE, mapOf("find" to "Commit ", "replace" to ""))
        val variable = envSet.getVariableFromResult(result, Output.Type.STDOUT)
        assertEquals("6fbbb964f8e940adbbd80278ca971b91", variable.value)
    }

    @Test
    fun `can get split variable from result`(){
        val envSet = Env( Env.Operation.SET, "COMMIT_GUID", Env.Type.SPLIT, mapOf("delimiter" to " ", "position" to 1))
        val variable = envSet.getVariableFromResult(result, Output.Type.STDOUT)
        assertEquals("6fbbb964f8e940adbbd80278ca971b91", variable.value)
    }

    @Test
    fun `can get after variable from result` () {
        val envSet = Env( Env.Operation.SET, "COMMIT_GUID", Env.Type.AFTER, mapOf("after" to "Commit "))
        val variable = envSet.getVariableFromResult(result, Output.Type.STDOUT)
        assertEquals("6fbbb964f8e940adbbd80278ca971b91", variable.value)
    }
}
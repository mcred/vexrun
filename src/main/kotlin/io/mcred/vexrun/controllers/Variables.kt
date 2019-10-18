package io.mcred.vexrun.controllers

import io.mcred.vexrun.models.Variable

data class Variables(
        val variables: MutableList<Variable> = mutableListOf<Variable>()
) {
    fun getVariablesByKey(key: String): List<Variable> {
        return variables.filter { it.key == key }
    }
}
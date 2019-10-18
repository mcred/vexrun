package io.mcred.vexrun.controllers

import io.mcred.vexrun.models.Test

data class Tests(
        val variables: Variables,
        val tests: MutableList<Test> = mutableListOf<Test>()
) {

}
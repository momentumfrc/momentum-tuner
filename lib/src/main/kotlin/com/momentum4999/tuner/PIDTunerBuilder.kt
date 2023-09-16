package com.momentum4999.tuner

import java.io.File
import java.util.function.Consumer

class PIDTunerBuilder(
        val controllerName: String,
        val saveValuesLocation: File = File("/home/lvuser/pid_constants.ini"),
        val properties: List<PIDProperty> = listOf()
) {
    fun withProperty(prop: PIDProperty) = PIDTunerBuilder(controllerName, saveValuesLocation, properties + prop)
    fun withProperty(name: String, consumer: Consumer<Double>) = withProperty(PIDProperty(name, consumer))

    fun withP(setP: Consumer<Double>) = withProperty("kP", setP)
    fun withI(setI: Consumer<Double>) = withProperty("kI", setI)
    fun withD(setD: Consumer<Double>) = withProperty("kD", setD)
    fun withFF(setFF: Consumer<Double>) = withProperty("kFF", setFF)
    fun withIZone(setIZone: Consumer<Double>) = withProperty("kIZone", setIZone)

    fun build() = PIDTuner(controllerName, saveValuesLocation, *properties.toTypedArray())

    fun safeBuild(): PIDTuner?
        = try {
            build()
        } catch(_: Exception) {
            null
        }
}

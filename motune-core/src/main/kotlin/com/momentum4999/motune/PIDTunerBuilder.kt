package com.momentum4999.motune

import edu.wpi.first.wpilibj.DriverStation
import java.nio.file.Path
import java.util.function.Consumer
import java.util.function.Supplier
import kotlin.io.path.Path

class PIDTunerBuilder(
        val controllerName: String,
        val dataStoreFile: Path = Path("/home/lvuser/pid_constants.ini"),
        val properties: List<PIDProperty> = listOf(),
        val stateValues: List<PIDStateValue> = listOf()
) {
    fun withDataStoreFile(file: Path)
        = PIDTunerBuilder(controllerName, file, properties, stateValues)

    fun withProperty(prop: PIDProperty)
        = PIDTunerBuilder(controllerName, dataStoreFile, properties + prop, stateValues)
    fun withProperty(name: String, consumer: Consumer<Double>)
        = withProperty(PIDProperty(name, consumer))

    fun withStateValue(value: PIDStateValue)
        = PIDTunerBuilder(controllerName, dataStoreFile, properties, stateValues + value)
    fun withStateValue(name: String, supplier: Supplier<Double>)
        = withStateValue(PIDStateValue(name, supplier))

    fun withP(setP: Consumer<Double>) = withProperty("kP", setP)
    fun withI(setI: Consumer<Double>) = withProperty("kI", setI)
    fun withD(setD: Consumer<Double>) = withProperty("kD", setD)
    fun withFF(setFF: Consumer<Double>) = withProperty("kFF", setFF)
    fun withIZone(setIZone: Consumer<Double>) = withProperty("kIZone", setIZone)

    fun withSetpoint(getSetpoint: Supplier<Double>) = withStateValue("setpoint", getSetpoint)
    fun withMeasurement(getMeasurement: Supplier<Double>) = withStateValue("measurement", getMeasurement)

    fun build() = PIDTuner(controllerName, dataStoreFile, properties, stateValues)

    fun safeBuild(): PIDTuner?
        = try {
            build()
        } catch(e: Exception) {
            DriverStation.reportError("Failed to build PIDTuner", e.stackTrace)
            null
        }
}

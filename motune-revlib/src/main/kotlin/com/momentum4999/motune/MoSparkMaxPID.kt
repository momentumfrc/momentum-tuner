package com.momentum4999.motune

import com.revrobotics.CANSparkMax
import com.revrobotics.REVLibError
import edu.wpi.first.wpilibj.DriverStation
import kotlin.properties.Delegates

private fun REVLibError?.check() {
    if(this != REVLibError.kOk) {
        DriverStation.reportError("REVLib operation failed with error: " + this.toString(), false)
    }
}

/**
 * A wrapper for a SparkMaxPIDController that will cache various values whenever they are updated. This is needed to
 * enable such functionality as graphing the setpoint; since there is no method on the SparkMaxPIDController to query
 * the current setpoint, we need to cache it ourselves whenever we set it.
 */
class MoSparkMaxPID(
        val type: Type,
        private val motorController: CANSparkMax,
        val pidSlot: Int
) {
    private val encoder = motorController.encoder
    val pidController = motorController.pidController!!

    var setpoint: Double by Delegates.observable(0.0) { _, _, value ->
        pidController.setReference(value, type.innerType)
    }

    val lastMeasurement: Double
        get() = when(type) {
            Type.POSITION, Type.SMARTMOTION -> encoder.position
            Type.VELOCITY -> encoder.velocity
        }

    var p: Double
        get() = pidController.getP(pidSlot)
        set(kP) = pidController.setP(kP, pidSlot).check()

    var i: Double
        get() = pidController.getI(pidSlot)
        set(kI) = pidController.setI(kI, pidSlot).check()

    var d: Double
        get() = pidController.getD(pidSlot)
        set(kD) = pidController.setD(kD, pidSlot).check()

    var FF: Double
        get() = pidController.getFF(pidSlot)
        set(kFF) = pidController.setFF(kFF, pidSlot).check()

    var iZone: Double
        get() = pidController.getIZone(pidSlot)
        set(iZone) = pidController.setIZone(iZone, pidSlot).check()

    enum class Type(val innerType: CANSparkMax.ControlType) {
        POSITION(CANSparkMax.ControlType.kPosition),
        SMARTMOTION(CANSparkMax.ControlType.kSmartMotion),
        VELOCITY(CANSparkMax.ControlType.kVelocity)
    }

    fun getTunerBuilder(controllerName: String): PIDTunerBuilder
        = PIDTuner.builder(controllerName)
                .withP(this::p::set)
                .withI(this::i::set)
                .withD(this::d::set)
                .withFF(this::FF::set)
                .withIZone(this::iZone::set)
                .withMeasurement(this::lastMeasurement::get)
                .withSetpoint(this::setpoint::get)
            .let{
                if(type == Type.SMARTMOTION) {
                    it.withProperty("maxVel") { v -> pidController.setSmartMotionMaxVelocity(v, pidSlot) }
                        .withProperty("maxAccel") { a -> pidController.setSmartMotionMaxAccel(a, pidSlot) }
                        .withProperty("allowedError") { e -> pidController.setSmartMotionAllowedClosedLoopError(e, pidSlot) }
                } else {
                    it
                }
            }

    fun buildTuner(controllerName: String) = getTunerBuilder(controllerName).safeBuild()
}
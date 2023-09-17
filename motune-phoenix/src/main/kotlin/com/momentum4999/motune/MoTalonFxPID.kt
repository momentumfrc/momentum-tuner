package com.momentum4999.motune

import com.ctre.phoenix.motorcontrol.TalonFXControlMode
import com.ctre.phoenix.motorcontrol.can.TalonFX
import kotlin.properties.Delegates

class MoTalonFxPID @JvmOverloads constructor(
        val type: Type,
        private val motorController: TalonFX,
        private val slotIdx: Int = 0
) {
    init {
        motorController.selectProfileSlot(slotIdx, 0)
    }

    var setpoint: Double by Delegates.observable(0.0) { _, _, value ->
        motorController.set(type.innerType, value)
    }

    val lastMeasurement: Double
        get() = when(type) {
            Type.POSITION, Type.SMARTMOTION -> motorController.getSelectedSensorPosition()
            Type.VELOCITY -> motorController.getSelectedSensorVelocity()
        }

    fun setP(kP: Double) = motorController.config_kP(slotIdx, kP)
    fun setI(kI: Double) = motorController.config_kI(slotIdx, kI)
    fun setD(kD: Double) = motorController.config_kD(slotIdx, kD)
    fun setFF(kFF: Double) = motorController.config_kF(slotIdx, kFF)
    fun setIZone(iZone: Double) = motorController.config_IntegralZone(slotIdx, iZone)

    enum class Type(val innerType: TalonFXControlMode) {
        POSITION(TalonFXControlMode.Position),
        SMARTMOTION(TalonFXControlMode.MotionMagic),
        VELOCITY(TalonFXControlMode.Velocity)
    }

    fun getTunerBuilder(controllerName: String): PIDTunerBuilder
        = PIDTuner.builder(controllerName)
                .withP(this::setP)
                .withI(this::setI)
                .withD(this::setD)
                .withFF(this::setFF)
                .withIZone(this::setIZone)
                .withMeasurement(this::lastMeasurement::get)
                .withSetpoint(this::setpoint::get)
            .let{
                if(type == Type.SMARTMOTION) {
                    it.withProperty("maxVel") { v -> motorController.configMotionCruiseVelocity(v) }
                        .withProperty("maxAccel") { a -> motorController.configMotionAcceleration(a) }
                        .withProperty("allowedError") { e -> motorController.configAllowableClosedloopError(slotIdx, e) }
                } else {
                    it
                }
            }

    fun buildTuner(controllerName: String) = getTunerBuilder(controllerName).safeBuild()
}
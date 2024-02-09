# Momentum Tuner

A set of wrappers and utility classes for storing PID values in a .ini file, while also listening for updates
to these values over the network tables.

The goal of this project is to improve the user experience of tuning PID controllers on the robot,
no matter if the controller runs on the RIO or on a motor controller. Using the PIDTuner class to configure
any given PID controller provides several benefits:
- PID constants are collected in one unified location, allowing them to be easily backed up
- PID constants are saved to disk as soon as they are updated, and they are automatically loaded back 
into the PID controller at startup
- PID constants can be adjusted via the NetworkTables using any NetworkTables client, including the
ShuffleBoard or Glass
- Process state variables from the PID controller are published to the NetworkTables, where they can be graphed
using Glass, much improving the process of tuning the constants

## Notes

There are two basic types of values handled by this tuner class: PID _properties_ and _state values_.

A _property_ is some value you set to configure the PID controller, also known as a PID constant or gain.
Examples include `kP`, `kI`, and `kD`.

A _state value_ is some value that describes the current state of the PID system. It is not a value you set, 
but a value you are interested in measuring to characterize the current behavior of the feedback loop. Examples
include the setpoint and the current measurement of the controller.

The `PIDTuner` class can handle any number of floating-point properties and state values. One simply needs to pass in a
key and a consumer or supplier (consumer for properties, supplier for state values) to the constructor. However,
it is often easier to use the `PIDTunerBuilder` class, which provides helper functions for common properties and
state values.

One function of note on the `PIDTunerBuilder` is the `safeBuild()` method. This will attempt to build the `PIDTuner`,
but if any exceptions occur it will catch them and report them to the driver station.


## Example

```java
PIDController drivePid = new PIDController(kP, kI, kD);
PIDTuner tuner = PIDTuner.builder("DrivePID")
        .withP(drivePid::setP)
        .withI(drivePid::setI)
        .withD(drivePid::setD)
        .withSetpoint(() -> lastSetpoint)
        .withMeasurement(() -> lastMeasurement)
        .safeBuild();
```
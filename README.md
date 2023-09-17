# Momentum Tuner

A set of wrappers and utility classes for storing PID values in a .ini file, while also listening for updates
to these values over the network tables.

The goal of this project is to improve the user experience of tuning PID controllers on the robot,
no matter if the controller runs on the RIO or on a motor controller. Using the PIDTuner class to configure
any given PID controller provides several benefits:
- PID constants are collected in one unified location, allowing them to be easily backed up
- PID constants are saved to disk as soon as they are updated, and they are automatically loaded back 
into the PID controler at startup
- PID constants can be adjusted via the NetworkTables using any NetworkTables client, including the
ShuffleBoard or Glass
- Process state variables from the PID controller are published to the NetworkTables, where they can be graphed
using Glass, much improving the process of tuning the constants
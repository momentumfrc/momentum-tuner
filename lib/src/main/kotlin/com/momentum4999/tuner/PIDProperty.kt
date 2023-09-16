package com.momentum4999.tuner

import java.util.function.Consumer

data class PIDProperty(
        val propertyName: String,
        val consumer: Consumer<Double>
)
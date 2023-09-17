package com.momentum4999.motune

import java.util.function.Consumer
import java.util.function.Supplier

data class PIDProperty(
        val propertyName: String,
        val consumer: Consumer<Double>
)

data class PIDStateValue(
        val valueName: String,
        val supplier: Supplier<Double>
)
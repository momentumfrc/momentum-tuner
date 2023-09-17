package com.momentum4999.tuner


import edu.wpi.first.networktables.NetworkTable
import edu.wpi.first.networktables.NetworkTableEvent
import edu.wpi.first.networktables.NetworkTableInstance
import java.io.File
import java.util.*

class PIDTuner(
        private val controllerName: String,
        dataStoreFile: File,
        properties: List<PIDProperty>,
        stateValues: List<PIDStateValue>
) : NetworkTable.TableEventListener {
    companion object{
        private const val TUNER_TABLE = "momentum-tuners"
        private const val DEFAULT_VALUE: Double = 0.0

        @JvmStatic fun build(controllerName: String) = PIDTunerBuilder(controllerName)
    }

    private val store = DataStore.getInstance(dataStoreFile)
    private val properties = properties.associateBy { it.propertyName }
    private val stateValues = stateValues.associateBy { it.valueName }

    private val table = NetworkTableInstance.getDefault().getTable(TUNER_TABLE).getSubTable(controllerName)

    private val listenerHandle = table.addListener(EnumSet.of(NetworkTableEvent.Kind.kValueAll, NetworkTableEvent.Kind.kImmediate), this)

    private val propertyPublishers = properties.associateWith { prop ->
        val value = store.getValue(controllerName, prop.propertyName).orElse(DEFAULT_VALUE)
        val publisher = table.getDoubleTopic(prop.propertyName).publish()

        publisher.set(value)

        return@associateWith publisher
    }

    private val stateValuePublishers by lazy {
        stateValues.associateWith { sv ->
            table.getDoubleTopic(sv.valueName).publish()
        }
    }

    override fun accept(table: NetworkTable, key: String, e: NetworkTableEvent) {
        val value = e.valueData.value.double
        val prop = properties[key] ?: return

        prop.consumer.accept(value)
        store.putValue(controllerName, prop.propertyName, value)
        store.save()
    }

    fun updateStateValues() {
        stateValuePublishers.forEach { (sv, publisher) -> publisher.set(sv.supplier.get()) }
    }

    fun cleanup() {
        table.instance.removeListener(listenerHandle)
    }
}
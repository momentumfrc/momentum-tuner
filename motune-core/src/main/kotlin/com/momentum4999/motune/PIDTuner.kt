package com.momentum4999.motune


import edu.wpi.first.networktables.NetworkTable
import edu.wpi.first.networktables.NetworkTableEvent
import edu.wpi.first.networktables.NetworkTableInstance
import java.nio.file.Path
import java.util.*

class PIDTuner(
        private val controllerName: String,
        dataStoreFile: Path,
        properties: List<PIDProperty>,
        stateValues: List<PIDStateValue>
) : NetworkTable.TableEventListener {
    companion object{
        private const val TUNER_TABLE = "momentum-tuners"
        private const val DEFAULT_VALUE: Double = 0.0

        private val instances: MutableList<PIDTuner> = mutableListOf()
        @JvmStatic fun builder(controllerName: String) = PIDTunerBuilder(controllerName)

        @JvmStatic fun pollAllStateValues() {
            instances.forEach{ it.pollStateValues() }
        }
    }

    init {
        instances.add(this)
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

        val oldValueOptional = store.getValue(controllerName, prop.propertyName)
        if(oldValueOptional.isPresent && oldValueOptional.orElseThrow() == value) {
            return
        }
        store.putValue(controllerName, prop.propertyName, value)
        store.save()
    }

    private fun pollStateValues() {
        stateValuePublishers.forEach { (sv, publisher) -> publisher.set(sv.supplier.get()) }
    }

    fun cleanup() {
        table.instance.removeListener(listenerHandle)
        instances.removeIf{it == this}
    }
}
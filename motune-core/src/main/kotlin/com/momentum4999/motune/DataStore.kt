package com.momentum4999.motune

import org.ini4j.Ini
import java.io.File
import java.util.*
import kotlin.collections.HashMap

class DataStore(private val file: File) {
    companion object {
        private val instances = HashMap<File, DataStore>()
        fun getInstance(file: File): DataStore = instances.getOrPut(file) { DataStore(file) }
    }

    private val ini = Ini()

    init {
        if(file.isFile) {
            ini.load(file)
        }
    }

    fun putValue(section: String, property: String, value: Double) = ini.put(section, property, value)
    fun getValue(section: String, property: String): Optional<Double> =
        if(ini.containsKey(section) && ini[section]!!.containsKey(property)) {
            Optional.of(ini.get(section, property, Double::class.java));
        } else {
            Optional.empty()
        }

    fun save() = ini.store(file)
}
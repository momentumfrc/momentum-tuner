package com.momentum4999.motune

import org.ini4j.Ini
import java.lang.IllegalStateException
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import kotlin.collections.HashMap
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource

class DataStore(private val file: Path) {
    companion object {
        private val instances = HashMap<Path, DataStore>()
        fun getInstance(file: Path): DataStore = instances.getOrPut(file) { DataStore(file) }

        private val MAX_NUM_BACKUP = 5
        private val BACKUP_INTERVAL = 60.seconds
    }

    private val ini = Ini()
    private var lastBackup: TimeMark? = null
    private val timeSource = TimeSource.Monotonic

    init {
        if(Files.isRegularFile(file)) {
            ini.load(file.toFile())
        }
    }

    fun putValue(section: String, property: String, value: Double) = ini.put(section, property, value)
    fun getValue(section: String, property: String): Optional<Double> =
        if(ini.containsKey(section) && ini[section]!!.containsKey(property)) {
            Optional.of(ini.get(section, property, Double::class.java));
        } else {
            Optional.empty()
        }

    private fun backup() {
        val name = file.fileName.toString()
        val toRemove = file.resolveSibling(name + MAX_NUM_BACKUP)
        Files.deleteIfExists(toRemove)
        for(i in MAX_NUM_BACKUP-1 downTo 1) {
            val oldName = file.resolveSibling(name + i)
            if(!Files.isRegularFile(oldName)) {
                continue
            }

            val newName = file.resolveSibling(name + (i+1))
            if(Files.exists(newName)) {
                throw IllegalStateException("File ${newName.fileName.toString()} already exists")
            }

            Files.move(oldName, newName)
        }

        val newName = file.resolveSibling(name + 1)
        Files.copy(file, newName)
        lastBackup = timeSource.markNow()
    }

    fun save() {
        val lastBackup = this.lastBackup
        if(lastBackup == null || lastBackup.elapsedNow() > BACKUP_INTERVAL) {
            backup()
        }
        ini.store(file.toFile())
    }
}
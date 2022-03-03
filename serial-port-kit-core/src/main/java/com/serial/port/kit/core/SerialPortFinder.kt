package com.serial.port.kit.core

import android.util.Log
import java.io.File
import java.io.FileReader
import java.io.LineNumberReader
import java.util.*

class SerialPortFinder {
    inner class Driver(val name: String, private val mDeviceRoot: String) {
        var mDevices: Vector<File>? = null
        val devices: Vector<File>
            get() {
                if (mDevices == null) {
                    mDevices = Vector()
                    val dev = File("/dev")
                    val files = dev.listFiles() ?: throw NullPointerException()
                    var i = 0
                    while (i < files.size) {
                        if (files[i].absolutePath.startsWith(mDeviceRoot)) {
                            Log.d(TAG, "Found new device: " + files[i])
                            mDevices!!.add(files[i])
                        }
                        i++
                    }
                }
                return mDevices!!
            }
    }

    private var mDrivers: Vector<Driver>? = null

    // Issue 3:
    // Since driver name may contain spaces, we do not extract driver name with split()
    @get:Throws(Exception::class)
    val drivers: Vector<Driver>
        get() {
            if (mDrivers == null) {
                mDrivers = Vector()
                val r = LineNumberReader(FileReader("/proc/tty/drivers"))
                var line: String?
                while (r.readLine().also { line = it } != null) {
                    // Issue 3:
                    // Since driver name may contain spaces, we do not extract driver name with split()
                    val driverName = line!!.substring(0, 0x15).trim { it <= ' ' }
                    val w = line!!.split(" +").toTypedArray()
                    if (w.size >= 5 && w[w.size - 1] == "serial") {
                        Log.e(TAG, "Found new driver " + driverName + " on " + w[w.size - 4])
                        mDrivers!!.add(Driver(driverName, w[w.size - 4]))
                    }
                }
                r.close()
            }
            return mDrivers!!
        }

    // Parse each driver
    @get:Throws(Exception::class)
    val allDevices: Array<String>
        get() {
            val devices = Vector<String>()
            // Parse each driver
            val itdriv: Iterator<Driver>
            itdriv = drivers.iterator()
            while (itdriv.hasNext()) {
                val driver = itdriv.next()
                val itdev: Iterator<File> = driver.devices.iterator()
                while (itdev.hasNext()) {
                    val device = itdev.next().name
                    val value = String.format("%s (%s)", device, driver.name)
                    devices.add(value)
                }
            }
            return devices.toTypedArray()
        }

    // Parse each driver
    @get:Throws(Exception::class)
    val allDevicesPath: Array<String>
        get() {
            val devices = Vector<String>()
            // Parse each driver
            val itdriv: Iterator<Driver>
            itdriv = drivers.iterator()
            while (itdriv.hasNext()) {
                val driver = itdriv.next()
                val itdev: Iterator<File> = driver.devices.iterator()
                while (itdev.hasNext()) {
                    val device = itdev.next().absolutePath
                    devices.add(device)
                }
            }
            return devices.toTypedArray()
        }

    companion object {
        private const val TAG = "SerialPort"
    }
}
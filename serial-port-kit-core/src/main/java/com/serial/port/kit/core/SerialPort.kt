package com.serial.port.kit.core

import android.util.Log
import java.io.*

class SerialPort
@JvmOverloads @Throws(IOException::class) constructor(
    device: File,
    baudRate: Int,
    flags: Int = 0
) {

    /**
     * 串口文件描述符，禁止删除或者重命名
     */
    private var mFd: FileDescriptor? = null

    /**
     * 输入流，用于接收串口数据
     */
    private val mFileInputStream: FileInputStream

    /**
     * 输出流，用于发送串口数据
     */
    private val mFileOutputStream: FileOutputStream

    val inputStream: FileInputStream
        get() = mFileInputStream

    val outputStream: FileOutputStream
        get() = mFileOutputStream

    init {
        checkPermission(device)
        // 开启串口，传入物理地址，波特率，flag值
        mFd = open(device.absolutePath, baudRate, flags)
        if (mFd == null) {
            Log.e(TAG, "native open returns null")
            throw IOException()
        }
        // 输入流，也就是获取从单片机或者传感器，通过串口传入到Android主板的IO数据（使用的时候，执行Read方法）,将外部存储的数据读取到内存里
        mFileInputStream = FileInputStream(mFd)
        // 输出流，Android将需要传输的数据发送到单片机或者传感器（使用的时候，执行Write方法）,将内存的数据写到外部存储
        mFileOutputStream = FileOutputStream(mFd)
    }

    @Throws(SecurityException::class)
    private fun checkPermission(device: File) {
        Log.i(TAG, "检测读写权限: 是否可读:${device.canRead()}，是否可写：${device.canWrite()}")
        // 检测设备管理权限，即文件的权限属性
        if (!device.canRead() || !device.canWrite()) {
            try {
                // Missing read/write permission, trying to chmod the file
                val su = Runtime.getRuntime().exec("/system/bin/su")
                val cmd = """
            chmod 777 ${device.absolutePath}
            exit
            """.trimIndent()
                Log.i(TAG, "提权命令:$cmd")
                su.outputStream.write(cmd.toByteArray())

                Log.i(TAG, "提权后重新检测读写权限: 是否可读:${device.canRead()}，是否可写：${device.canWrite()}")

                if (su.waitFor() != 0 || !device.canRead() || !device.canWrite()) {
                    throw SecurityException()
                }
            } catch (e: Exception) {
                Log.e(TAG, "checkPermission: trying to chmod the file is fail", e)
                throw SecurityException()
            }
        }
    }

    /**
     * 打开串口
     * @param path 串口虚拟文件
     * @param baudRate 波特率
     * @param flags 操作标识
     * @return 文件描述符
     */
    private external fun open(path: String, baudRate: Int, flags: Int): FileDescriptor

    /**
     * 关闭串口
     */
    external fun close()

    companion object {
        init {
            System.loadLibrary("SerialPortLib")
        }

        private const val TAG = "SerialPort"
    }
}
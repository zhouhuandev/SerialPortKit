package com.serial.port.kit.manage

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.serial.port.kit.core.common.TypeConversion.bytes2HexString
import com.serial.port.kit.manage.command.SerialCommandProtocol
import com.serial.port.kit.manage.listener.OnReadSystemStateListener
import com.serial.port.kit.manage.listener.OnReadVersionListener
import com.serial.port.kit.manage.model.DeviceVersionModel
import com.serial.port.kit.manage.model.SystemStateModel
import com.serial.port.kit.manage.proxy.SerialPortProxy
import com.serial.port.manage.SerialPortManager
import com.serial.port.manage.data.WrapReceiverData
import com.serial.port.manage.data.WrapSendData
import com.serial.port.manage.listener.OnDataReceiverListener
import kotlin.experimental.and

/**
 * 工具指令管理
 *
 * @author <a href="mailto: zhouhuandev@gmail.com" rel="nofollow">zhouhuan</a>
 * @since 2022/3/19 16:25
 */
object SerialPortHelper {
    private const val TAG = "SerialPortManager"

    private val mHandler = Handler(Looper.getMainLooper())
    private val mProxy = SerialPortProxy()

    /**
     * 暴露SDK
     */
    val portManager: SerialPortManager
        get() = mProxy.portManager

    /**
     * 内部使用，默认开启串口
     */
    private val serialPortManager: SerialPortManager
        get() {
            // 默认开启串口
            if (!portManager.isOpenDevice) {
                portManager.open()
            }
            return portManager
        }

    /**
     * 读取设备版本信息
     *
     * @param listener 监听回调
     */
    fun readVersion(listener: OnReadVersionListener?) {
        val sends: ByteArray = SenderManager.getSender().sendReadVersion()
        val isSuccess: Boolean =
            serialPortManager.send(
                WrapSendData(sends, 3000, 300, 1),
                object : OnDataReceiverListener {

                    override fun onSuccess(data: WrapReceiverData) {
                        val buffer: ByteArray = data.data
                        if (checkCallData(buffer)) {
                            val serializeId: Int =
                                ((buffer[7] and 0xFF.toByte()).toInt() shl 24) + ((buffer[8] and 0xFF.toByte()).toInt() shl 16) + ((buffer[9] and 0xFF.toByte()).toInt() shl 8) + (buffer[10] and 0xFF.toByte())
                            listener?.let {
                                runOnUiThread {
                                    listener.onResult(
                                        DeviceVersionModel(
                                            String.format("%s", serializeId),
                                            String.format("v %s.%s", buffer[3], buffer[4]),
                                            String.format("v %s.%s", buffer[5], buffer[6])
                                        )
                                    )
                                }
                            }
                        }
                    }

                    override fun onFailed(wrapSendData: WrapSendData, msg: String) {
                        Log.e(TAG, "onFailed: $msg")
                    }

                    override fun onTimeOut() {
                        Log.d(TAG, "onTimeOut: 发送数据或者接收数据超时")
                    }
                })
        printLog(isSuccess, sends)
    }


    /**
     * 读取设备信息
     *
     * @param listener 监听回调
     */
    fun readSystemState(listener: OnReadSystemStateListener?) {
        val sends: ByteArray = SenderManager.getSender().sendStartDetect()
        val isSuccess: Boolean =
            serialPortManager.send(WrapSendData(sends), object : OnDataReceiverListener {

                override fun onSuccess(data: WrapReceiverData) {
                    val buffer = data.data
                    if (checkCallData(buffer)) {
                        //输入电压
                        val inputVoltage = buffer[3] * 0.1
                        //电机电压
                        val motorVoltage = buffer[4] * 0.1
                        //VCC电压
                        val vccVoltage = buffer[5] * 0.1
                        //MCU电压
                        val mcuVoltage = buffer[6] * 0.1
                        //温度值
                        val bytes = ByteArray(1)
                        bytes[0] = buffer[7]
                        val temperature: Int =
                            bytes2HexString(bytes)?.substring(0, 2)?.toInt(16) ?: 0
                        //照度值
                        val illumination: Int =
                            ((buffer[8] and 0xFF.toByte()).toInt() shl 8) + (buffer[9] and 0xFF.toByte())
                        listener?.let {
                            runOnUiThread {
                                listener.onResult(
                                    SystemStateModel(
                                        inputVoltage,
                                        motorVoltage,
                                        vccVoltage,
                                        mcuVoltage,
                                        temperature,
                                        illumination
                                    )
                                )
                            }
                        }
                    }
                }

                override fun onFailed(wrapSendData: WrapSendData, msg: String) {
                    Log.e(TAG, "onFailed: $msg")
                }

                override fun onTimeOut() {
                    Log.d(TAG, "onTimeOut: 发送数据或者接收数据超时")
                }
            })
        printLog(isSuccess, sends)
    }

    /**
     * 检测回调数据是否符合要求
     *
     * @param buffer 回调数据
     * @return true 符合要求 false 数据命令未通过校验
     */
    private fun checkCallData(buffer: ByteArray): Boolean {
        val tempData = bytes2HexString(buffer)
        Log.i(TAG, "receive serialPort data ：$tempData")
        return buffer[0] == SerialCommandProtocol.baseStart[0] && SerialCommandProtocol.checkHex(
            buffer
        )
    }

    /**
     * 打印发送数据Log
     *
     * @param isSuccess 是否成功
     * @param bytes     数据
     */
    private fun printLog(isSuccess: Boolean, bytes: ByteArray) {
        val tempData = bytes2HexString(bytes)
        Log.d(
            TAG,
            "buildControllerProtocol:" + tempData + "，结果=" + if (isSuccess) "发送成功" else "发送失败"
        )
    }

    /**
     * 切换到主线程
     *
     * @param runnable Runnable
     */
    private fun runOnUiThread(runnable: Runnable) {
        mHandler.post(runnable)
    }
}
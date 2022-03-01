package com.serial.port.manage

import android.util.Log
import com.serial.port.command.SerialCommandProtocol
import com.serial.port.command.SerialCommandProtocol.checkHex
import com.serial.port.kit.core.common.TypeConversion.bytes2HexString
import com.serial.port.manage.config.SerialPortConfig
import com.serial.port.manage.listener.OnReadSystemStateListener
import com.serial.port.manage.listener.OnReadVersionListener
import com.serial.port.manage.listener.OnS0DataReceiverListener
import com.serial.port.manage.model.DeviceVersionModel
import com.serial.port.manage.model.SystemStateModel
import com.serial.port.manage.utils.ThreadUtils
import com.serial.port.manage.utils.ToastUtil.showToastCenter

/**
 * 串口包装管理
 *
 * @author zhouhuan
 * @time 2021/10/27
 */
object SerialPortManager {
    private const val TAG = "SerialPortManager"

    private var config: SerialPortConfig? = null

    fun init(config: SerialPortConfig) {
        this.config = config
        val isSuccess = SerialPortHelper.openDevice(config)
        if (isSuccess) {
            Log.d(
                TAG,
                String.format(
                    "init: open serial port success !，path=%s,baudRate=%s",
                    config.path,
                    config.baudRate
                )
            )
        } else {
            showToastCenter("串口打开失败，请尝试重新启动App")
        }
    }

    fun clear() {
        if (config != null) {
            config!!.getOnS0DataReceiverListeners().clear()
            config = null
        }
        SerialPortHelper.clear()
    }

    /**
     * 检测设备是否已经连接下位机
     */
    private fun checkConnectDevice() {
        requireNotNull(config) { "SerialPortManager must init" }
        val openDevice = SerialPortHelper.isOpenDevice
        val retry = SerialPortHelper.isRetry
        // 正常启动时，openDevice 为 false，retry 为 false；当发送指令出现异常被捕获时，触发重试机制，此时 openDevice 为 true，retry 为 true。满足重新启动条件
        if (!openDevice || retry) {
            // 若是重试机制打开，则关闭重试机制。注：重试机制仅为一次，若是重试打开错误时，会给出弹窗提示。
            if (retry) {
                SerialPortHelper.isRetry = false
            }
            val isSuccess = SerialPortHelper.reOpenDevice(config)
            if (isSuccess) {
                Log.d(
                    TAG,
                    String.format(
                        "checkConnectDevice: open serial port success !，path=%s,baudRate=%s",
                        config!!.path,
                        config!!.baudRate
                    )
                )
            } else {
                showToastCenter("串口打开失败，请尝试重新启动App")
            }
        }
    }

    /**
     * 读取版本号
     *
     * @param listener 监听回调
     */
    fun readVersion(listener: OnReadVersionListener?) {
        ThreadUtils.submit {
            checkConnectDevice()
            if (listener != null) {
                SerialPortHelper.addS0DataReceiverListener(object : OnS0DataReceiverListener {
                    override fun onDataReceive(buffer: ByteArray, size: Int) {
                        if (checkCallData(buffer) && checkCommand(
                                SerialCommandProtocol.readVersion,
                                buffer
                            )
                        ) {
                            val serializeId: Int =
                                (buffer[7].toInt() and 0xFF shl 24) + (buffer[8].toInt() and 0xFF shl 16) + (buffer[9].toInt() and 0xFF shl 8) + (buffer[10].toInt() and 0xFF)
                            listener.onResult(
                                DeviceVersionModel(
                                    String.format("%s", serializeId),
                                    String.format("v %s.%s", buffer[3], buffer[4]),
                                    String.format("v %s.%s", buffer[5], buffer[6])
                                )
                            )
                            SerialPortHelper.removeS0DataReceiverListener(this)
                        }
                    }
                })
            }
            // 发送指令
            SenderManager.getSender().sendReadVersion()
        }
    }

    /**
     * 读取设备信息
     */
    fun readSystemState(listener: OnReadSystemStateListener?) {
        ThreadUtils.submit {
            checkConnectDevice()
            if (listener != null) {
                SerialPortHelper.addS0DataReceiverListener(object : OnS0DataReceiverListener {
                    override fun onDataReceive(buffer: ByteArray, size: Int) {
                        if (checkCallData(buffer) && checkCommand(
                                SerialCommandProtocol.systemState,
                                buffer
                            )
                        ) {
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
                                bytes2HexString(bytes)!!.substring(0, 2).toInt(16)
                            //照度值
                            val illumination: Int =
                                (buffer[8].toInt() and 0xFF shl 8) + (buffer[9].toInt() and 0xFF)
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
                            SerialPortHelper.removeS0DataReceiverListener(this)
                        }
                    }
                })
            }
            // 发送指令
            SenderManager.getSender().sendStartDetect()
        }
    }

    /**
     * 检测回调数据是否符合要求
     *
     * @param buffer 回调数据
     * @return true 符合要求 false 数据命令未通过校验
     */
    private fun checkCallData(buffer: ByteArray): Boolean {
        val tempData = bytes2HexString(buffer)
        Log.i(TAG, "Receive SerialPort Data ：$tempData")
        val b = buffer[0] == SerialCommandProtocol.baseStart[0] && checkHex(buffer)
        if (!b) {
            Log.e(TAG, String.format("数据命令未通过校验:%s", tempData))
        }
        return b
    }

    /**
     * 检测命令所属指令
     *
     * @param command 命令字段
     * @param buffer  回调数据(取第二个参数命令)
     * @return true 符合 false 不符合
     */
    private fun checkCommand(command: ByteArray, buffer: ByteArray): Boolean {
        return command[0] == buffer[1]
    }


}

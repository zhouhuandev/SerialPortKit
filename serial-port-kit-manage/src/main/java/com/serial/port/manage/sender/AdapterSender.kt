package com.serial.port.manage.sender

import com.serial.port.command.SerialCommandProtocol.onCmdCheckDeviceStatusInfo
import com.serial.port.command.SerialCommandProtocol.onCmdReadVersionStatus
import com.serial.port.manage.SerialPortHelper

/**
 * 发送指令实现
 *
 * @author zhouhuan
 * @time 2019/12/18 18:35
 */
class AdapterSender : Sender {
    /**
     * 发送组装好的命令组
     *
     * @param bytes 数据
     * @return true 发送成功
     */
    override fun sendControllerProtocolBuffer(bytes: ByteArray?): Boolean {
        return SerialPortHelper.sendBuffer(bytes)
    }

    override fun sendStartDetect(): Boolean {
        val bytes = onCmdCheckDeviceStatusInfo()
        return sendControllerProtocolBuffer(bytes)
    }

    override fun sendReadVersion(): Boolean {
        val bytes = onCmdReadVersionStatus()
        return sendControllerProtocolBuffer(bytes)
    }
}
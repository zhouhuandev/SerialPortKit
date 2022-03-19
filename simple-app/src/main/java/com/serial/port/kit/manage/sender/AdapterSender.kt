package com.serial.port.kit.manage.sender

import com.serial.port.kit.manage.command.SerialCommandProtocol.onCmdCheckDeviceStatusInfo
import com.serial.port.kit.manage.command.SerialCommandProtocol.onCmdReadVersionStatus

/**
 * 发送指令实现
 *
 * @author zhouhuan
 * @time 2019/12/18 18:35
 */
class AdapterSender : Sender {

    override fun sendStartDetect(): ByteArray {
        return onCmdCheckDeviceStatusInfo()
    }

    override fun sendReadVersion(): ByteArray {
        return onCmdReadVersionStatus()
    }
}
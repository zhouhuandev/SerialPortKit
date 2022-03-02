package com.serial.port.kit.command

import com.serial.port.manage.command.protocol.BaseProtocol

/**
 * 命令池
 *
 * @author zhouhuan
 * @time 2020/1/10 15:51
 */
object SerialCommandProtocol : BaseProtocol() {
    var baseStart = byteArrayOf(0xAA.toByte())
    var baseEnd = byteArrayOf(0x00.toByte())

    /**
     * 系统状态参数读取,检测机器状态信息
     */
    var systemState = byteArrayOf(0xA1.toByte())
    var deviceInfo = byteArrayOf(0x00.toByte(), 0xB5.toByte())

    /**
     * 读取主板版本号
     */
    var readVersion = byteArrayOf(0xA9.toByte())
    var readVersionStatus = byteArrayOf(0x00.toByte(), 0xAD.toByte())

    /**
     * 升级指令
     */
    var upgrade = byteArrayOf(0xAA.toByte())
    var readyForUpgrade = byteArrayOf(0x01.toByte(), 0x00.toByte(), 0xAB.toByte())

    /**
     * 检查机器运行状态信息
     *
     * @return 0xAA 0xA1 0x00 0xB5
     */
    fun onCmdCheckDeviceStatusInfo(): ByteArray {
        return buildControllerProtocol(
            baseStart,
            systemState,
            deviceInfo
        )
    }

    /**
     * 获取主板版本号
     *
     * @return 0xAA 0xA9 0x00 0xAD
     */
    fun onCmdReadVersionStatus(): ByteArray {
        return buildControllerProtocol(
            baseStart,
            readVersion,
            readVersionStatus
        )
    }

    /**
     * 准备进入升级模式
     *
     * @return 0xAA 0xAA 0x01 0x00 0xAB
     */
    fun onCmdReadyForUpgrade(): ByteArray {
        return buildControllerProtocol(
            baseStart,
            upgrade,
            readyForUpgrade
        )
    }

    /**
     * 校验板子发回来的结果集
     *
     * @return
     */
    fun checkHex(ret: ByteArray): Boolean {
        var tempRet = 0
        for (i in 0 until ret.size - 1) {
            tempRet += ret[i]
        }
        return (tempRet.inv() + 1).toByte() == ret[ret.size - 1]
    }
}
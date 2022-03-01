package com.serial.port.command.protocol

import java.util.*

/**
 * 基础命令池
 *
 * @author zhouhuan
 * @time 2020/1/10 13:36
 */
abstract class BaseProtocol {
    /**
     * 指令组装
     *
     * @param bytes
     * @return
     */
    fun buildControllerProtocol(vararg bytes: ByteArray): ByteArray {
        val byteArray: MutableList<Byte> = LinkedList()
        for (bytes1 in bytes) {
            for (b in bytes1) {
                byteArray.add(b)
            }
        }
        val ret = ByteArray(byteArray.size)
        for (i in byteArray.indices) {
            ret[i] = byteArray[i]
        }
        return ret
    }
}
package com.serial.port.manage

import com.serial.port.manage.sender.Sender
import com.serial.port.manage.sender.AdapterSender

/**
 * 串口数据发送工具管理初始化
 *
 * @author zhouhuan
 * @time 2019/12/18 18:33
 */
object SenderManager {
    private var senderMap = hashMapOf<Int, Sender>()

    /**
     * 获取发送者
     */
    @JvmOverloads
    fun getSender(type: Int = 0): Sender {
        if (senderMap[type] == null) {
            senderMap[type] = createSender(type)
        }
        return senderMap[type]!!
    }

    /**
     * 创建发送者
     *
     * @param type
     * @return
     */
    private fun createSender(type: Int): Sender {
        return when (type) {
            0, 1 -> AdapterSender()
            else -> AdapterSender()
        }
    }
}
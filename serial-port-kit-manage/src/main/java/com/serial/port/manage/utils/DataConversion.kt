package com.serial.port.manage.utils

import com.serial.port.manage.data.WrapSendData

/**
 * 数据类型转换工具
 *
 * @author zhouhuan
 * @time 2022/3/2
 */
object DataConversion {
    /**
     * 指令转换发送数据类型
     *
     * @param cmd 指令
     * @return true 发送成功
     */
    @JvmStatic
    fun sendCmds(cmd: String): WrapSendData {
        val mBuffer = cmd.toByteArray()
        return WrapSendData(mBuffer)
    }
}
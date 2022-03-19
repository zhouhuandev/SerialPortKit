package com.serial.port.kit.manage.utils

import com.serial.port.manage.data.WrapReceiverData
import com.serial.port.manage.data.WrapSendData
import com.serial.port.manage.listener.OnAddressCheckCall
import com.serial.port.manage.listener.OnDataCheckCall
import java.io.InputStream

/**
 * 数据工具类
 *
 * @author <a href="mailto: zhouhuandev@gmail.com" rel="nofollow">zhouhuan</a>
 * @since 2022/3/19 16:36
 */
object DataConvertUtil {

    /**
     * 检测命令所属指令
     *
     * @param command 命令字段
     * @param buffer  回调数据(取第二个参数命令)
     * @return true 符合 false 不符合
     */
    private fun checkCommand(command: ByteArray, buffer: ByteArray): Boolean {
        return command[1] == buffer[1]
    }

    /**
     * 自定义通讯协议
     *
     * @return 协议回调
     */
    fun customProtocol(): OnDataCheckCall {
        return object : OnDataCheckCall {
            override fun customCheck(
                inputStream: InputStream, onDataPickCall: (WrapReceiverData) -> Unit
            ): Boolean {
                val tempBuffer = ByteArray(64)
                val bodySize = inputStream.read(tempBuffer)
                return if (bodySize > 0) {
                    onDataPickCall.invoke(WrapReceiverData(tempBuffer, bodySize))
                    true
                } else {
                    false
                }
            }
        }
    }

    /**
     * 校验地址
     *
     * @return 地址回调
     */
    fun addressCheckCall(): OnAddressCheckCall {
        return object : OnAddressCheckCall {
            override fun checkAddress(
                wrapSendData: WrapSendData,
                wrapReceiverData: WrapReceiverData
            ): Boolean {
                return checkCommand(wrapSendData.sendData, wrapReceiverData.data)
            }
        }
    }
}
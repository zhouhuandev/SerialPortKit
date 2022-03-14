package com.serial.port.manage.listener

import com.serial.port.manage.data.WrapReceiverData
import java.io.InputStream

/**
 * 自定义校验数据回调
 *
 * @author zhouhuan
 * @time 2022/3/2
 */
interface OnDataCheckCall {
    fun customCheck(
        inputStream: InputStream,
        onDataPickCall: (WrapReceiverData) -> Unit
    ): Boolean
}
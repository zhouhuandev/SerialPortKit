package com.serial.port.manage.listener

import com.serial.port.manage.data.WrapReceiverData
import com.serial.port.manage.data.WrapSendData

/**
 * 串口数据回调
 *
 * @author zhouhuan
 * @time 2021/10/28
 */
interface OnDataReceiverListener {
    /**
     * 回调数据
     *
     * @param data 数据
     */
    fun onSuccess(data: WrapReceiverData)

    fun onFailed(wrapSendData: WrapSendData, msg: String)

    fun onTimeOut()
}
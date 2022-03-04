package com.serial.port.manage.listener

import com.serial.port.manage.data.WrapReceiverData
import com.serial.port.manage.data.WrapSendData

/**
 * 校验命令地址位
 *
 * @author zhouhuan
 * @time 2022/3/4
 */
interface OnAddressCheckCall {
    fun checkAddress(wrapSendData: WrapSendData, wrapReceiverData: WrapReceiverData): Boolean
}
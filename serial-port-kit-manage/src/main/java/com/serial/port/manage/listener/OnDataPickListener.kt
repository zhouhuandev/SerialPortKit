package com.serial.port.manage.listener

import com.serial.port.manage.data.WrapSendData

/**
 * 增加额外监听回调
 *
 * @author zhouhuan
 * @time 2022/3/10
 */
interface OnDataPickListener : OnDataReceiverListener {
    override fun onFailed(wrapSendData: WrapSendData, msg: String) {

    }

    override fun onTimeOut() {

    }
}
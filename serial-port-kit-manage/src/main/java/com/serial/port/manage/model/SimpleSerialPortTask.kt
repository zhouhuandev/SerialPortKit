package com.serial.port.manage.model

import com.serial.port.manage.data.BaseSerialPortTask
import com.serial.port.manage.data.WrapSendData
import com.serial.port.manage.listener.OnDataReceiverListener

/**
 * 普通任务
 *
 * @author zhouhuan
 * @time 2022/3/2
 */
class SimpleSerialPortTask(
    private val wrapSendData: WrapSendData,
    private val onDataReceiverListener: OnDataReceiverListener
) : BaseSerialPortTask() {
    override fun sendWrapData(): WrapSendData = wrapSendData

    override fun onDataReceiverListener(): OnDataReceiverListener = onDataReceiverListener

    override fun onTaskStart() {

    }

    override fun onTaskCompleted() {

    }
}
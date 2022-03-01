package com.serial.port.manage.config

import com.serial.port.manage.listener.OnS0DataReceiverListener
import java.lang.ref.WeakReference
import java.util.ArrayList

/**
 * 串口配置信息
 *
 * @author zhouhuan
 * @time 2021/10/26
 */
class SerialPortConfig(
    /**
     * 串口地址
     */
    var path: String,

    /**
     * 波特率
     */
    var baudRate: Int = 115200,

    /**
     * 最大读取长度
     */
    var maxSize: Int = 64,


    /**
     * 是否按最大接收长度进行返回（注：当前需求不需要按照数据的最大长度进行返回）
     */
    var isReceiveMaxSize: Boolean = false,

    /**
     * 是否按照自定义协议读取数据信息。
     * 如果 isCustom = true 自定义协议的话，需要在 [SerialReadThread.run] 自行实现读取串口数据信息
     * 注：如果自定义协议，[SerialPortConfig.isReceiveMaxSize] 则必须为 false
     */
    var isCustom: Boolean = false
) {

    /**
     * 数据接听回调
     */
    private val onS0DataReceiverListeners: MutableList<WeakReference<OnS0DataReceiverListener>> =
        ArrayList()

    fun getOnS0DataReceiverListeners(): MutableList<WeakReference<OnS0DataReceiverListener>> {
        return onS0DataReceiverListeners
    }
}
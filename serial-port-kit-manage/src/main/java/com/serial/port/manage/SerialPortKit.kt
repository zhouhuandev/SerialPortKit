package com.serial.port.manage

import android.app.Application
import com.serial.port.kit.core.SerialPort
import com.serial.port.kit.core.annotation.CmdSuShell
import com.serial.port.manage.config.SerialPortConfig
import com.serial.port.manage.config.SerialPortEnv
import com.serial.port.manage.listener.OnAddressCheckCall
import com.serial.port.manage.listener.OnDataCheckCall
import com.serial.port.manage.utils.ToastUtil
import java.util.concurrent.ExecutorService

/**
 * SerialPortKit
 *
 * @author zhouhuan
 * @time 2022/3/2
 */
class SerialPortKit private constructor(builder: Builder) {

    companion object {
        @JvmStatic
        fun newBuilder(app: Application): Builder {
            return Builder(app)
        }
    }

    class Builder(app: Application) {

        init {
            SerialPortEnv.app = app
        }

        internal var mExecutor: ExecutorService? = null

        internal var path = ""
        internal var baudRate: Int = 115200
        internal var maxSize: Int = 64
        internal var retryCount = 0
        internal var receiveMaxCount = 1
        internal var isReceiveMaxSize: Boolean = false
        internal var isCustom: Boolean = false
        internal var isBlockingReadData: Boolean = false
        internal var cmdSuShell: Int = SerialPort.CMD_BIN_SU_SHELL
        internal var dataCheckCall: OnDataCheckCall? = null
        internal var addressCheckCall: OnAddressCheckCall? = null
        internal var debug = false
        internal var isShowToast = false

        fun path(path: String): Builder = apply { this.path = path }

        fun baudRate(baudRate: Int): Builder = apply { this.baudRate = baudRate }

        fun maxSize(maxSize: Int): Builder = apply { this.maxSize = maxSize }

        fun retryCount(retryCount: Int): Builder = apply { this.retryCount = retryCount }

        fun receiveMaxCount(receiveMaxCount: Int): Builder =
            apply { this.receiveMaxCount = receiveMaxCount }

        fun isReceiveMaxSize(isReceiveMaxSize: Boolean): Builder =
            apply { this.isReceiveMaxSize = isReceiveMaxSize }

        fun isCustom(isCustom: Boolean, dataCheckCall: OnDataCheckCall? = null): Builder = apply {
            this.isCustom = isCustom
            this.dataCheckCall = dataCheckCall
        }

        fun isBlockingReadData(isBlockingReadData: Boolean): Builder = apply {
            this.isBlockingReadData = isBlockingReadData
        }

        fun setCmdSuShell(@CmdSuShell cmdSuShell: Int): Builder =
            apply { this.cmdSuShell = cmdSuShell }

        fun addressCheckCall(addressCheckCall: OnAddressCheckCall): Builder =
            apply { this.addressCheckCall = addressCheckCall }

        fun setExecutor(executor: ExecutorService): Builder = apply { this.mExecutor = executor }

        fun debug(debug: Boolean): Builder = apply { this.debug = debug }

        fun isShowToast(showToast: Boolean): Builder = apply { this.isShowToast = showToast }

        fun build(): SerialPortKit {
            checkParams()
            return SerialPortKit(this)
        }

        private fun checkParams() {
            check(path != "") { "Path is must important parameters，and it cannot be null!" }
            check(baudRate >= 0) { "The baudRate is $baudRate, BaudRate is must important parameters，and it cannot be less than 0!" }
            check(retryCount in 1..SerialPortManager.MAX_RETRY_COUNT) { "The retryCount is $retryCount, The number of retries should be between 1 and 3!" }
            check(receiveMaxCount in 1..SerialPortManager.MAX_RECEIVE_COUNT) { "The receiveMaxCount is $receiveMaxCount, The number of retries should be between 1 and 3!" }
            if (isCustom) {
                checkNotNull(dataCheckCall) { "isCustom is ture, dataCheckCall is not null !" }
            }
        }
    }

    private val manager = SerialPortManager(
        SerialPortConfig(
            path = builder.path,
            baudRate = builder.baudRate,
            maxSize = builder.maxSize,
            retryCount = builder.retryCount,
            receiveMaxCount = builder.receiveMaxCount,
            isReceiveMaxSize = builder.isReceiveMaxSize,
            isCustom = builder.isCustom,
            isBlockingReadData = builder.isBlockingReadData,
            cmdSuShell = builder.cmdSuShell,
            dataCheckCall = builder.dataCheckCall,
            addressCheckCall = builder.addressCheckCall,
            debug = builder.debug,
            isShowToast = builder.isShowToast,
            executor = builder.mExecutor,
        )
    )

    init {
        ToastUtil.init(SerialPortEnv.requireApp())
    }

    fun get() = manager
}
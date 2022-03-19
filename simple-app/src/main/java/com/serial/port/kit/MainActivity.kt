package com.serial.port.kit

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.serial.port.kit.core.common.TypeConversion
import com.serial.port.kit.manage.SenderManager
import com.serial.port.kit.manage.SerialPortManager
import com.serial.port.kit.manage.listener.OnReadSystemStateListener
import com.serial.port.kit.manage.listener.OnReadVersionListener
import com.serial.port.kit.manage.model.DeviceVersionModel
import com.serial.port.kit.manage.model.SystemStateModel
import com.serial.port.manage.data.WrapReceiverData
import com.serial.port.manage.data.WrapSendData
import com.serial.port.manage.listener.OnDataPickListener
import com.serial.port.manage.listener.OnDataReceiverListener

class MainActivity : AppCompatActivity() {

    private val button: Button by lazy { findViewById(R.id.button) }
    private val button2: Button by lazy { findViewById(R.id.button2) }
    private val button3: Button by lazy { findViewById(R.id.button3) }
    private val button4: Button by lazy { findViewById(R.id.button4) }
    private val button5: Button by lazy { findViewById(R.id.button5) }
    private val button6: Button by lazy { findViewById(R.id.button6) }
    private val button7: Button by lazy { findViewById(R.id.button7) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
    }

    private fun initView() {
        // 打开串口
        button.setOnClickListener {
            if (!MyApp.portManager.isOpenDevice) {
                val open = MyApp.portManager.open()
                Log.d(TAG, "串口打开${if (open) "成功" else "失败"}")
            }
        }
        // 关闭串口
        button2.setOnClickListener {
            val close = MyApp.portManager.close()
            Log.d(TAG, "串口关闭${if (close) "成功" else "失败"}")
        }
        // 发送数据
        button3.setOnClickListener {
            MyApp.portManager.send(WrapSendData(SenderManager.getSender().sendStartDetect()),
                object : OnDataReceiverListener {
                    override fun onSuccess(data: WrapReceiverData) {
                        Log.d(TAG, "响应数据：${TypeConversion.bytes2HexString(data.data)}")
                    }

                    override fun onFailed(wrapSendData: WrapSendData, msg: String) {
                        Log.e(
                            TAG,
                            "发送数据: ${TypeConversion.bytes2HexString(wrapSendData.sendData)}, $msg"
                        )
                    }

                    override fun onTimeOut() {
                        Log.e(TAG, "发送或者接收超时")
                    }
                })
        }
        // 切换串口
        button4.setOnClickListener {
            val switchDevice = MyApp.portManager.switchDevice(path = "/dev/ttyS1")
            Log.d(TAG, "串口切换${if (switchDevice) "成功" else "失败"}")
        }
        // 切换波特率
        button5.setOnClickListener {
            val switchDevice = MyApp.portManager.switchDevice(baudRate = 9600)
            Log.d(TAG, "波特率切换${if (switchDevice) "成功" else "失败"}")

        }
        // 读取版本信息
        button6.setOnClickListener {
            SerialPortManager.readVersion(object : OnReadVersionListener {
                override fun onResult(deviceVersionModel: DeviceVersionModel) {
                    Log.d(TAG, "onResult: $deviceVersionModel")
                }
            })
        }
        // 读取系统信息
        button7.setOnClickListener {
            SerialPortManager.readSystemState(object : OnReadSystemStateListener {
                override fun onResult(systemStateModel: SystemStateModel) {
                    Log.d(TAG, "onResult: $systemStateModel")
                }
            })
        }

    }

    override fun onResume() {
        super.onResume()
        // 增加统一监听回调
        MyApp.portManager.addDataPickListener(onDataPickListener)
    }

    override fun onPause() {
        super.onPause()
        // 移除统一监听回调
        MyApp.portManager.removeDataPickListener(onDataPickListener)
    }

    private val onDataPickListener: OnDataPickListener = object : OnDataPickListener {
        override fun onSuccess(data: WrapReceiverData) {
            Log.d(TAG, "统一响应数据：${TypeConversion.bytes2HexString(data.data)}")
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
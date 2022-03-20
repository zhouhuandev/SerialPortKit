package com.serial.port.kit.provider

import android.app.Application
import android.content.ContentProvider
import com.serial.port.kit.provider.ContextProvider
import android.content.pm.ProviderInfo
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri

/**
 * Use to Context
 *
 * @author [zhouhuan](mailto: zhouhuandev@gmail.com)
 * @since 2022/3/20
 */
class ContextProvider : ContentProvider() {
    override fun onCreate(): Boolean {
        INSTANCE = this
        //这里只取上下文，所以，返回false告诉系统这个provider并没有初始化成功！
        return false
    }

    override fun attachInfo(context: Context, info: ProviderInfo) {
        INSTANCE = this
        super.attachInfo(context, info)
    }

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor? {
        return null
    }

    override fun getType(uri: Uri): String? {
        return null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        return null
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        return 0
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        return 0
    }

    companion object {
        private var INSTANCE: ContextProvider? = null

        /**
         * 获取实例
         * @return 不为空，因为common库无法在启动之前执行代码所以，
         * 这边默认认为返回值不可能为空，但是，请不要在attachBaseContext里面使用！！！
         */
        val instance: ContextProvider
            get() = INSTANCE!!

        /**
         * 请不要在Application 的 attachBaseContext 或者 onCreate里面使用！！！
         */
        val holdContext: Context
            get() = INSTANCE!!.context!!

        /**
         * 请不要在Application 的 attachBaseContext 或者 onCreate里面使用！！！
         */
        val appContext: Application
            get() = INSTANCE!!.context!!.applicationContext as Application
    }
}
package com.serial.port.manage.utils

import java.lang.ref.WeakReference

/**
 * 拓展类
 *
 * @author zhouhuan
 * @time 2022/3/10
 */

/**
 * 是否包含某个弱引用对象
 * @param element 目标对象
 * @param remove 是否移除
 * @param removeResult 是否移除成功回调
 */
@JvmOverloads
fun <T> Iterable<WeakReference<T>>.contains(
    element: T,
    remove: Boolean = false,
    removeResult: ((Boolean) -> Unit)? = null
): Boolean {
    val iterator = iterator()
    while (iterator.hasNext()) {
        val reference = iterator.next()
        if (reference.get() == null) {
            if (iterator is MutableIterator) {
                iterator.remove()
            }
            continue
        }
        if (reference.get() == element) {
            if (remove) {
                if (iterator is MutableIterator) {
                    iterator.remove()
                    removeResult?.invoke(remove)
                }
            }
            return true
        }
    }
    removeResult?.invoke(remove)
    return false
}
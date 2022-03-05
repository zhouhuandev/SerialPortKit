package com.serial.port.kit.core.common

import java.util.*

/**
 * Byte数组工具
 *
 * @author zhouhuan
 * @time 2022/3/5
 */
object TypeConversion {

    /**
     * 判断奇数或偶数，位运算，最后一位是1则为奇数，为0是偶数
     *
     * @param num
     * @return
     */
    fun isOdd(num: Int): Int {
        return num and 0x1
    }

    /**
     * 字节转10进制
     * @param b
     */
    fun byte2Int(b: Byte): Int {
        return b.toInt()
    }

    /**
     * 10进制转字节
     * @param i
     */
    fun int2Byte(i: Int): Byte {
        return i.toByte()
    }

    /**
     * byte转16进制字符串
     *
     * @param b
     * @return
     */
    fun byte2hex(b: ByteArray): String {
        val sb = StringBuffer()
        var tmp: String?
        for (i in b.indices) {
            tmp = Integer.toHexString(b[i].toInt() and 0XFF)
            if (tmp.length == 1) {
                sb.append("0x0$tmp ")
            } else {
                sb.append("0x$tmp ")
            }
        }
        return sb.toString()
    }

    /**
     * @param bytes
     * @return
     */
    fun bytes2hex(bytes: ByteArray): String {
        val hex = "0123456789abcdef"
        val sb = StringBuilder(bytes.size * 2)
        for (b in bytes) {
            // 取出这个字节的高4位，然后与0x0f与运算，得到一个0-15之间的数据，通过HEX.charAt(0-15)即为16进制数
            sb.append(hex[b.toInt() shr 4 and 0x0f])
            // 取出这个字节的低位，与0x0f与运算，得到一个0-15之间的数据，通过HEX.charAt(0-15)即为16进制数
            sb.append(hex[b.toInt() and 0x0f].toString() + " ")
        }
        return sb.toString()
    }

    /**
     * 字节数组转16进制字符串
     *
     * @param src 字节数组
     * @return 16进制字符串 01 30 31 32
     */
    fun bytes2HexString(src: ByteArray): String? {
        if (src.isEmpty()) {
            return null
        }
        val result = StringBuffer()
        var hex: String
        for (i in src.indices) {
            hex = Integer.toHexString(src[i].toInt() and 0xFF)
            if (hex.length == 1) {
                hex = "0$hex"
            }
            result.append(hex.uppercase(Locale.getDefault()) + " ")
        }
        return result.toString()
    }

    /**
     * 字符串转16进制字符串
     *
     * @param strPart 字符串
     * @return 16进制字符串
     */
    fun string2HexString(strPart: String): String {
        val hexString = StringBuffer()
        for (element in strPart) {
            val ch = element.code
            val strHex = Integer.toHexString(ch)
            hexString.append(strHex)
        }
        return hexString.toString()
    }

    /**
     * 16进制字符串转字符串
     *
     * @param src 16进制字符串
     * @return 字节数组
     */
    fun hexString2String(src: String): String {
        var temp = ""
        for (i in 0 until src.length / 2) {
            temp = (temp
                    + Integer.valueOf(
                src.substring(i * 2, i * 2 + 2),
                16
            ).toByte().toInt().toChar())
        }
        return temp
    }

    /**
     * 字符转成字节数据char-->integer-->byte
     *
     * @param src
     * @return Byte
     */
    fun char2Byte(src: Char): Byte {
        return Integer.valueOf(src.code).toByte()
    }

    /**
     * 10进制数字转成16进制
     *
     * @param a   转化数据
     * @param len 占用字节数
     * @return
     */
    private fun intToHexString(a: Int, len: Int): String {
        var len = len
        len = len shl 1
        var hexString = Integer.toHexString(a)
        val b = len - hexString.length
        if (b > 0) {
            for (i in 0 until b) {
                hexString = "0$hexString"
            }
        }
        return hexString
    }

    fun hexStringToByte(hex: String): ByteArray {
        val len = hex.length / 2
        val result = ByteArray(len)
        val achar = hex.toCharArray()
        for (i in 0 until len) {
            val pos = i * 2
            result[i] = (toByte(achar[pos]) shl 4 or toByte(
                achar[pos + 1]
            )).toByte()
        }
        return result
    }

    private fun toByte(c: Char): Int {
        val b = "0123456789ABCDEF".indexOf(c).toByte()
        return b.toInt()
    }


    /**
     * 将data字节型数据转换为0~255 (0xFF 即BYTE)。
     *
     * @param data
     * @return
     */
    fun getUnsignedByte(data: Byte): Int {
        return data.toInt() and 0x0FF
    }

    /**
     * 将data字节型数据转换为0~65535 (0xFFFF 即 WORD)。
     *
     * @param data
     * @return
     */
    fun getUnsignedByte(data: Short): Int {
        return data.toInt() and 0x0FFFF
    }

    /**
     * 将int数据转换为0~4294967295 (0xFFFFFFFF即DWORD)。
     *
     * @param data
     * @return
     */
    fun getUnsignedInt(data: Int): Long {
        return (data.toInt() and 0x0FFFFFFFFL.toInt()).toLong()
    }

    /**
     * 获取高四位
     *
     * @param data
     * @return
     */
    fun getHeight4(data: Byte): Int {
        return data.toInt() and 0xf0 shr 4
    }

    /**
     * 获取低四位
     *
     * @param data
     * @return
     */
    fun getLow4(data: Byte): Int {
        return data.toInt() and 0x0f
    }
}
/**
 * 日志
 *
 * @author zhouhuan
 * @time 2022/3/1
 */

#ifndef SERIALPORTHELPER_MYLOG_H
#define SERIALPORTHELPER_MYLOG_H

#include <android/log.h>
// 各个优先级的宏定义
static const char *TAG = "SerialPortLog";
#define LOGI(fmt, args...) __android_log_print(ANDROID_LOG_INFO,  TAG, fmt, ##args)
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, TAG, fmt, ##args)
#define LOGE(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, TAG, fmt, ##args)
#define LOGW(fmt, args...) __android_log_write(ANDROID_LOG_WARN, TAG, fmt, ##args)
#define LOGV(fmt, args...) __android_log_print(ANDROID_LOG_VERBOSE, TAG, fmt, ##args)

#endif
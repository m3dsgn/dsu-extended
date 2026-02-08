package com.dsu.extended.util

import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AppLogger {
    private val timestampFormatter =
        ThreadLocal.withInitial { SimpleDateFormat("HH:mm:ss.SSS", Locale.US) }
    private const val MAX_BUFFERED_LINES = 4000
    private val bufferedLogs = ArrayDeque<String>(MAX_BUFFERED_LINES)
    private val lock = Any()

    private fun buildMessage(message: String, details: Array<out Pair<String, Any?>>): String {
        val formatter = requireNotNull(timestampFormatter.get())
        val timestamp = formatter.format(Date())
        val prefix = "[$timestamp][${Thread.currentThread().name}]"
        if (details.isEmpty()) {
            return "$prefix $message"
        }
        val formattedDetails =
            details.joinToString(separator = ", ", prefix = "{", postfix = "}") { (key, value) ->
                "$key=$value"
        }
        return "$prefix $message $formattedDetails"
    }

    private fun appendBufferedLog(priority: String, tag: String, message: String) {
        synchronized(lock) {
            if (bufferedLogs.size >= MAX_BUFFERED_LINES) {
                bufferedLogs.removeFirst()
            }
            bufferedLogs.addLast("[$priority][$tag] $message")
        }
    }

    fun dumpBufferedLogs(): String {
        synchronized(lock) {
            return bufferedLogs.joinToString(separator = "\n")
        }
    }

    fun d(tag: String, message: String, vararg details: Pair<String, Any?>) {
        val formatted = buildMessage(message, details)
        appendBufferedLog("D", tag, formatted)
        Log.d(tag, formatted)
    }

    fun i(tag: String, message: String, vararg details: Pair<String, Any?>) {
        val formatted = buildMessage(message, details)
        appendBufferedLog("I", tag, formatted)
        Log.i(tag, formatted)
    }

    fun w(tag: String, message: String, vararg details: Pair<String, Any?>) {
        val formatted = buildMessage(message, details)
        appendBufferedLog("W", tag, formatted)
        Log.w(tag, formatted)
    }

    fun e(tag: String, message: String, throwable: Throwable? = null, vararg details: Pair<String, Any?>) {
        val formatted = buildMessage(message, details)
        appendBufferedLog("E", tag, formatted + (throwable?.let { "\n${Log.getStackTraceString(it)}" } ?: ""))
        Log.e(tag, formatted, throwable)
    }
}

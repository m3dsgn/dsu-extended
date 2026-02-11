package com.dsu.extended.installer

import android.os.Build
import com.dsu.extended.model.DeviceInfo
import com.dsu.extended.model.DiagnosticReport
import com.dsu.extended.model.ErrorAnalysis
import com.dsu.extended.model.ErrorPatterns
import com.dsu.extended.model.ErrorSeverity
import com.dsu.extended.model.GsiInfo
import com.dsu.extended.model.InstallationInfo
import com.dsu.extended.model.LogEntry
import com.dsu.extended.model.LogSeverity
import com.dsu.extended.preparation.InstallationStep

/**
 * Boot failure analysis engine for diagnosing DSU installation issues.
 * Provides detailed analysis and suggestions based on error patterns.
 */
object BootDiagnostics {

    private const val TAG = "BootDiagnostics"

    /**
     * Analyzes logcat output and returns a diagnostic report
     */
    fun analyzeBootFailure(
        logs: String,
        gsiInfo: GsiInfo? = null,
        installationInfo: InstallationInfo? = null,
    ): DiagnosticReport {
        val parsedLogs = parseLogs(logs)
        val errorAnalysis = analyzeErrors(logs, parsedLogs)
        val suggestions = generateSuggestions(errorAnalysis, gsiInfo)

        return DiagnosticReport(
            timestamp = System.currentTimeMillis(),
            deviceInfo = DeviceInfo(),
            gsiInfo = gsiInfo,
            installationInfo = installationInfo,
            errorAnalysis = errorAnalysis,
            logs = parsedLogs,
            suggestions = suggestions,
        )
    }

    /**
     * Determines the installation step based on error analysis
     */
    fun getInstallationStepFromError(errorAnalysis: ErrorAnalysis): InstallationStep {
        return when {
            errorAnalysis.errorType.contains("avb", ignoreCase = true) ||
                errorAnalysis.errorType.contains("verity", ignoreCase = true) ->
                InstallationStep.ERROR_AVB_VERIFICATION

            errorAnalysis.errorType.contains("partition", ignoreCase = true) ->
                InstallationStep.ERROR_PARTITION_TABLE

            errorAnalysis.errorType.contains("kernel", ignoreCase = true) ->
                InstallationStep.ERROR_KERNEL_MISMATCH

            errorAnalysis.errorType.contains("bootloop", ignoreCase = true) ->
                InstallationStep.ERROR_BOOTLOOP_DETECTED

            errorAnalysis.errorType.contains("vendor", ignoreCase = true) ->
                InstallationStep.ERROR_VENDOR_MISMATCH

            errorAnalysis.errorType.contains("vndk", ignoreCase = true) ->
                InstallationStep.ERROR_VNDK_MISMATCH

            errorAnalysis.errorType.contains("treble", ignoreCase = true) ->
                InstallationStep.ERROR_TREBLE_INCOMPATIBLE

            errorAnalysis.errorType.contains("dm-verity", ignoreCase = true) ->
                InstallationStep.ERROR_DM_VERITY

            errorAnalysis.errorType.contains("system_server", ignoreCase = true) ->
                InstallationStep.ERROR_SYSTEM_SERVER_CRASH

            errorAnalysis.errorType.contains("timeout", ignoreCase = true) ->
                InstallationStep.ERROR_BOOT_TIMEOUT

            errorAnalysis.errorType.contains("super", ignoreCase = true) ->
                InstallationStep.ERROR_SUPER_PARTITION

            errorAnalysis.errorType.contains("slot", ignoreCase = true) ->
                InstallationStep.ERROR_SLOT_SWITCH

            errorAnalysis.errorType.contains("arch", ignoreCase = true) ||
                errorAnalysis.errorType.contains("cpu", ignoreCase = true) ->
                InstallationStep.ERROR_CPU_ARCH_MISMATCH

            errorAnalysis.errorType.contains("selinux", ignoreCase = true) ->
                InstallationStep.ERROR_SELINUX

            else -> InstallationStep.ERROR_UNKNOWN_BOOT_FAILURE
        }
    }

    /**
     * Parses raw log string into structured LogEntry list
     */
    private fun parseLogs(rawLogs: String): List<LogEntry> {
        val entries = mutableListOf<LogEntry>()
        val lines = rawLogs.split("\n")

        for (line in lines) {
            if (line.isBlank()) continue

            val entry = parseLogLine(line)
            entries.add(entry)
        }

        return entries
    }

    /**
     * Parses a single log line into a LogEntry
     */
    private fun parseLogLine(line: String): LogEntry {
        // Try to parse standard logcat format: "D/Tag: message"
        val logcatPattern = Regex("^([VDIWEF])/([^:]+):\\s*(.+)$")
        val match = logcatPattern.find(line)

        return if (match != null) {
            val (severityStr, tag, message) = match.destructured
            val severity = when (severityStr) {
                "V" -> LogSeverity.VERBOSE
                "D" -> LogSeverity.DEBUG
                "I" -> LogSeverity.INFO
                "W" -> LogSeverity.WARNING
                "E" -> LogSeverity.ERROR
                "F" -> LogSeverity.FATAL
                else -> LogSeverity.INFO
            }
            LogEntry(
                timestamp = System.currentTimeMillis(),
                severity = severity,
                tag = tag.trim(),
                message = message,
                source = "logcat",
            )
        } else {
            LogEntry(
                timestamp = System.currentTimeMillis(),
                severity = LogSeverity.INFO,
                tag = "raw",
                message = line,
                source = "raw",
            )
        }
    }

    /**
     * Analyzes logs for error patterns
     */
    private fun analyzeErrors(rawLogs: String, parsedLogs: List<LogEntry>): ErrorAnalysis {
        val lowerLogs = rawLogs.lowercase()
        val relatedLines = mutableListOf<String>()
        val possibleCauses = mutableListOf<String>()
        var errorType = "Unknown"
        var severity = ErrorSeverity.ERROR
        var isRecoverable = false

        // Check AVB patterns
        for ((pattern, description) in ErrorPatterns.AVB_PATTERNS) {
            if (lowerLogs.contains(pattern.lowercase())) {
                errorType = "AVB Verification Failure"
                possibleCauses.add(description)
                severity = ErrorSeverity.CRITICAL
                relatedLines.addAll(findLinesContaining(parsedLogs, pattern))
            }
        }

        // Check boot patterns
        for ((pattern, description) in ErrorPatterns.BOOT_PATTERNS) {
            if (lowerLogs.contains(pattern.lowercase())) {
                if (errorType == "Unknown") errorType = "Boot Failure"
                possibleCauses.add(description)
                if (pattern.lowercase() == "kernel panic") {
                    severity = ErrorSeverity.CRITICAL
                }
                relatedLines.addAll(findLinesContaining(parsedLogs, pattern))
            }
        }

        // Check partition patterns
        for ((pattern, description) in ErrorPatterns.PARTITION_PATTERNS) {
            if (lowerLogs.contains(pattern.lowercase())) {
                if (errorType == "Unknown") errorType = "Partition Error"
                possibleCauses.add(description)
                relatedLines.addAll(findLinesContaining(parsedLogs, pattern))
            }
        }

        // Check compatibility patterns
        for ((pattern, description) in ErrorPatterns.COMPATIBILITY_PATTERNS) {
            if (lowerLogs.contains(pattern.lowercase())) {
                if (errorType == "Unknown") errorType = "Compatibility Issue"
                possibleCauses.add(description)
                isRecoverable = true
                relatedLines.addAll(findLinesContaining(parsedLogs, pattern))
            }
        }

        // Check system patterns
        for ((pattern, description) in ErrorPatterns.SYSTEM_PATTERNS) {
            if (lowerLogs.contains(pattern.lowercase())) {
                if (errorType == "Unknown") errorType = "System Error"
                possibleCauses.add(description)
                relatedLines.addAll(findLinesContaining(parsedLogs, pattern))
            }
        }

        // Detect bootloop pattern
        val rebootCount = Regex("Rebooting in", RegexOption.IGNORE_CASE).findAll(rawLogs).count()
        if (rebootCount >= 2) {
            errorType = "Bootloop Detected"
            possibleCauses.add("System is rebooting repeatedly ($rebootCount times detected)")
            severity = ErrorSeverity.CRITICAL
        }

        return ErrorAnalysis(
            errorType = errorType,
            errorCode = getErrorCode(errorType),
            errorMessage = possibleCauses.firstOrNull() ?: "Unknown error occurred",
            errorSource = "BootDiagnostics",
            relatedLogLines = relatedLines.distinct().takeLast(10),
            severity = severity,
            isRecoverable = isRecoverable,
            possibleCauses = possibleCauses.distinct(),
        )
    }

    private fun findLinesContaining(logs: List<LogEntry>, pattern: String): List<String> {
        return logs
            .filter { it.message.contains(pattern, ignoreCase = true) }
            .map { it.toFormattedString() }
    }

    private fun getErrorCode(errorType: String): Int {
        return when (errorType) {
            "AVB Verification Failure" -> 1001
            "Boot Failure" -> 1002
            "Partition Error" -> 1003
            "Compatibility Issue" -> 1004
            "System Error" -> 1005
            "Bootloop Detected" -> 1006
            else -> 9999
        }
    }

    /**
     * Generates suggestions based on the error analysis
     */
    private fun generateSuggestions(
        errorAnalysis: ErrorAnalysis,
        gsiInfo: GsiInfo?,
    ): List<String> {
        val suggestions = mutableListOf<String>()

        when {
            errorAnalysis.errorType.contains("AVB", ignoreCase = true) -> {
                suggestions.add("🔓 Flash disabled vbmeta: fastboot flash vbmeta vbmeta_disabled.img --disable-verity --disable-verification")
                suggestions.add("🔓 Or boot with: fastboot boot --disable-verity --disable-verification boot.img")
                suggestions.add("📖 See: https://developer.android.com/topic/generic-system-image#flash-gsi")
            }

            errorAnalysis.errorType.contains("Bootloop", ignoreCase = true) -> {
                suggestions.add("🔄 Try a different GSI (the current one may be incompatible with your device)")
                suggestions.add("🔧 Ensure your VNDK version matches the GSI requirements")
                suggestions.add("📱 Check if your device's vendor image is compatible")
                suggestions.add("🔓 If bootloader is locked, unlock it first")
            }

            errorAnalysis.errorType.contains("Partition", ignoreCase = true) -> {
                suggestions.add("📦 Ensure your device has Dynamic Partitions support")
                suggestions.add("💾 Free up more storage space (DSU requires significant free space)")
                suggestions.add("🔧 Use a custom gsid build that matches your Android branch")
            }

            errorAnalysis.errorType.contains("Compatibility", ignoreCase = true) -> {
                suggestions.add("✅ Verify GSI matches your device architecture (${Build.SUPPORTED_ABIS.joinToString()})")
                suggestions.add("📋 Check VNDK version compatibility")
                suggestions.add("🔍 Ensure GSI supports your Android version")
            }

            errorAnalysis.errorType.contains("SELinux", ignoreCase = true) -> {
                suggestions.add("🛡️ Try running: setenforce 0 (requires root)")
                suggestions.add("🔧 Install a Magisk module with sepolicy fixes")
                suggestions.add("📦 Use a GSI with relaxed SELinux policies")
            }

            errorAnalysis.errorType.contains("System", ignoreCase = true) -> {
                suggestions.add("🔄 Try wiping dynamic system userdata")
                suggestions.add("📦 Use a more stable GSI build")
                suggestions.add("📱 Check for firmware updates for your device")
            }

            else -> {
                suggestions.add("📋 Review the full logs for more details")
                suggestions.add("🌐 Search for your device + GSI on XDA forums")
                suggestions.add("📝 Try a different GSI or version")
                suggestions.add("🔧 Ensure your device meets all DSU requirements")
            }
        }

        // Add device-specific suggestions
        if (Build.VERSION.SDK_INT < 30) {
            suggestions.add("⚠️ Android 10 has limited DSU support - consider upgrading")
        }

        gsiInfo?.let { gsi ->
            if (gsi.expectedArch.isNotEmpty() &&
                !Build.SUPPORTED_ABIS.any { it.contains(gsi.expectedArch, ignoreCase = true) }
            ) {
                suggestions.add(0, "❌ CRITICAL: GSI architecture (${gsi.expectedArch}) doesn't match device (${Build.SUPPORTED_ABIS.first()})")
            }
        }

        return suggestions
    }

    /**
     * Quick check if device is likely compatible with DSU
     */
    fun checkDeviceCompatibility(): DeviceCompatibilityResult {
        val issues = mutableListOf<String>()
        val warnings = mutableListOf<String>()

        // Check SDK version
        if (Build.VERSION.SDK_INT < 29) {
            issues.add("Device running Android ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT}). DSU requires Android 10 or higher.")
        } else if (Build.VERSION.SDK_INT == 29) {
            warnings.add("Android 10 has limited DSU support. Some features may not work correctly.")
        }

        // Check if emulator
        if (DeviceInfo().isEmulator) {
            warnings.add("Running on emulator - DSU may not work correctly.")
        }

        // Check system properties for dynamic partitions
        val hasDynamicPartitions = try {
            Runtime.getRuntime().exec("getprop ro.boot.dynamic_partitions")
                .inputStream.bufferedReader().readText().trim() == "true"
        } catch (e: Exception) {
            false
        }

        if (!hasDynamicPartitions) {
            issues.add("Device may not support Dynamic Partitions (required for DSU).")
        }

        return DeviceCompatibilityResult(
            isCompatible = issues.isEmpty(),
            issues = issues,
            warnings = warnings,
            cpuArch = Build.SUPPORTED_ABIS.first(),
            androidVersion = "${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})",
            hasDynamicPartitions = hasDynamicPartitions,
        )
    }
}

data class DeviceCompatibilityResult(
    val isCompatible: Boolean,
    val issues: List<String>,
    val warnings: List<String>,
    val cpuArch: String,
    val androidVersion: String,
    val hasDynamicPartitions: Boolean,
)

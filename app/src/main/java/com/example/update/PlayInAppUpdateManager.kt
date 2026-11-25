package com.example.update

import android.app.Activity
import android.content.Context
import android.content.IntentSender
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.common.IntentSenderForResultStarter
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface InAppUpdateUiState {
    data object Idle : InAppUpdateUiState
    data object Checking : InAppUpdateUiState
    data object UpToDate : InAppUpdateUiState
    data class Available(
        val appUpdateInfo: AppUpdateInfo,
        val updateType: Int,
        val availableVersionCode: Int,
        val stalenessDays: Int?,
        val priority: Int,
        val shouldLaunchImmediately: Boolean
    ) : InAppUpdateUiState
    data class Downloading(
        val bytesDownloaded: Long,
        val totalBytesToDownload: Long
    ) : InAppUpdateUiState {
        val progress: Float
            get() = if (totalBytesToDownload > 0L) {
                (bytesDownloaded.toFloat() / totalBytesToDownload.toFloat()).coerceIn(0f, 1f)
            } else {
                0f
            }
    }
    data object Downloaded : InAppUpdateUiState
    data class InfoMessage(val message: String) : InAppUpdateUiState
}

/**
 * Coordinates Google Play In-App Updates (Flexible & Immediate) following official best practices:
 * - Priority >= 4 or staleness >= 7 days triggers IMMEDIATE update flow.
 * - Standard updates trigger non-intrusive FLEXIBLE notification banner & background download.
 * - Checks stalled IMMEDIATE updates and DOWNLOADED flexible updates on resume.
 * - Uses ActivityResultLauncher<IntentSenderRequest> instead of deprecated onActivityResult.
 */
class PlayInAppUpdateManager(
    private val appUpdateManager: AppUpdateManager
) {
    constructor(context: Context) : this(AppUpdateManagerFactory.create(context.applicationContext))

    companion object {
        const val HIGH_PRIORITY_THRESHOLD = 4
        const val IMMEDIATE_STALENESS_DAYS = 7
    }

    private val _uiState = MutableStateFlow<InAppUpdateUiState>(InAppUpdateUiState.Idle)
    val uiState: StateFlow<InAppUpdateUiState> = _uiState.asStateFlow()

    private var dismissedVersionCode: Int? = null
    private var isListenerRegistered = false

    private val installStateListener = InstallStateUpdatedListener { state ->
        when (state.installStatus()) {
            InstallStatus.PENDING -> {
                _uiState.value = InAppUpdateUiState.Downloading(0L, 0L)
            }
            InstallStatus.DOWNLOADING -> {
                _uiState.value = InAppUpdateUiState.Downloading(
                    bytesDownloaded = state.bytesDownloaded(),
                    totalBytesToDownload = state.totalBytesToDownload()
                )
            }
            InstallStatus.DOWNLOADED -> {
                unregisterListener()
                _uiState.value = InAppUpdateUiState.Downloaded
            }
            InstallStatus.INSTALLED -> {
                unregisterListener()
                _uiState.value = InAppUpdateUiState.Idle
            }
            InstallStatus.CANCELED -> {
                unregisterListener()
                _uiState.value = InAppUpdateUiState.Idle
            }
            InstallStatus.FAILED -> {
                unregisterListener()
                _uiState.value = InAppUpdateUiState.InfoMessage(
                    "No se pudo completar la descarga de la actualización."
                )
            }
            else -> Unit
        }
    }

    fun checkForUpdates(userInitiated: Boolean = false) {
        if (userInitiated) {
            dismissedVersionCode = null
            _uiState.value = InAppUpdateUiState.Checking
        }

        appUpdateManager.appUpdateInfo
            .addOnSuccessListener { appUpdateInfo ->
                handleAppUpdateInfo(appUpdateInfo, userInitiated)
            }
            .addOnFailureListener {
                if (userInitiated) {
                    _uiState.value = InAppUpdateUiState.InfoMessage(
                        "Disponible solo cuando la app está instalada desde Google Play."
                    )
                } else {
                    _uiState.value = InAppUpdateUiState.Idle
                }
            }
    }

    fun checkOnResume() {
        appUpdateManager.appUpdateInfo
            .addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                    _uiState.value = InAppUpdateUiState.Downloaded
                    return@addOnSuccessListener
                }

                if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS &&
                    appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
                ) {
                    _uiState.value = InAppUpdateUiState.Available(
                        appUpdateInfo = appUpdateInfo,
                        updateType = AppUpdateType.IMMEDIATE,
                        availableVersionCode = appUpdateInfo.availableVersionCode(),
                        stalenessDays = appUpdateInfo.clientVersionStalenessDays(),
                        priority = appUpdateInfo.updatePriority(),
                        shouldLaunchImmediately = true
                    )
                }
            }
    }

    private fun handleAppUpdateInfo(appUpdateInfo: AppUpdateInfo, userInitiated: Boolean) {
        if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
            _uiState.value = InAppUpdateUiState.Downloaded
            return
        }

        if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS &&
            appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
        ) {
            _uiState.value = InAppUpdateUiState.Available(
                appUpdateInfo = appUpdateInfo,
                updateType = AppUpdateType.IMMEDIATE,
                availableVersionCode = appUpdateInfo.availableVersionCode(),
                stalenessDays = appUpdateInfo.clientVersionStalenessDays(),
                priority = appUpdateInfo.updatePriority(),
                shouldLaunchImmediately = true
            )
            return
        }

        if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
            val availableVersion = appUpdateInfo.availableVersionCode()
            if (!userInitiated && dismissedVersionCode == availableVersion) {
                return
            }

            val priority = appUpdateInfo.updatePriority()
            val staleness = appUpdateInfo.clientVersionStalenessDays()
            val prefersImmediate = priority >= HIGH_PRIORITY_THRESHOLD ||
                (staleness != null && staleness >= IMMEDIATE_STALENESS_DAYS)

            when {
                prefersImmediate && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE) -> {
                    _uiState.value = InAppUpdateUiState.Available(
                        appUpdateInfo = appUpdateInfo,
                        updateType = AppUpdateType.IMMEDIATE,
                        availableVersionCode = availableVersion,
                        stalenessDays = staleness,
                        priority = priority,
                        shouldLaunchImmediately = true
                    )
                }
                appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE) -> {
                    _uiState.value = InAppUpdateUiState.Available(
                        appUpdateInfo = appUpdateInfo,
                        updateType = AppUpdateType.FLEXIBLE,
                        availableVersionCode = availableVersion,
                        stalenessDays = staleness,
                        priority = priority,
                        shouldLaunchImmediately = false
                    )
                }
                appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE) -> {
                    _uiState.value = InAppUpdateUiState.Available(
                        appUpdateInfo = appUpdateInfo,
                        updateType = AppUpdateType.IMMEDIATE,
                        availableVersionCode = availableVersion,
                        stalenessDays = staleness,
                        priority = priority,
                        shouldLaunchImmediately = userInitiated
                    )
                }
                else -> {
                    if (userInitiated) {
                        _uiState.value = InAppUpdateUiState.UpToDate
                    }
                }
            }
        } else {
            if (userInitiated) {
                _uiState.value = InAppUpdateUiState.UpToDate
            }
        }
    }

    fun startUpdateFlow(
        availableState: InAppUpdateUiState.Available,
        launcher: ActivityResultLauncher<IntentSenderRequest>
    ) {
        if (availableState.updateType == AppUpdateType.FLEXIBLE) {
            registerListener()
        }

        val starter = IntentSenderForResultStarter { intent, _, fillInIntent, flagsMask, flagsValues, _, _ ->
            val request = IntentSenderRequest.Builder(intent)
                .setFillInIntent(fillInIntent)
                .setFlags(flagsMask, flagsValues)
                .build()
            launcher.launch(request)
        }

        try {
            appUpdateManager.startUpdateFlowForResult(
                availableState.appUpdateInfo,
                starter,
                AppUpdateOptions.newBuilder(availableState.updateType).build(),
                1001
            )
        } catch (_: IntentSender.SendIntentException) {
            unregisterListener()
            _uiState.value = InAppUpdateUiState.InfoMessage(
                "No se pudo iniciar la actualización desde Google Play."
            )
        } catch (_: Exception) {
            unregisterListener()
            _uiState.value = InAppUpdateUiState.InfoMessage(
                "Actualización no disponible en este momento."
            )
        }
    }

    fun onActivityResult(resultCode: Int) {
        if (resultCode != Activity.RESULT_OK) {
            val current = _uiState.value
            if (current is InAppUpdateUiState.Available) {
                dismissedVersionCode = current.availableVersionCode
            }
            unregisterListener()
            _uiState.value = InAppUpdateUiState.Idle
        }
    }

    fun dismissBanner(versionCode: Int) {
        dismissedVersionCode = versionCode
        _uiState.value = InAppUpdateUiState.Idle
    }

    fun clearMessage() {
        if (_uiState.value is InAppUpdateUiState.InfoMessage ||
            _uiState.value is InAppUpdateUiState.UpToDate
        ) {
            _uiState.value = InAppUpdateUiState.Idle
        }
    }

    fun completeFlexibleUpdate() {
        appUpdateManager.completeUpdate()
    }

    private fun registerListener() {
        if (!isListenerRegistered) {
            appUpdateManager.registerListener(installStateListener)
            isListenerRegistered = true
        }
    }

    private fun unregisterListener() {
        if (isListenerRegistered) {
            appUpdateManager.unregisterListener(installStateListener)
            isListenerRegistered = false
        }
    }

    fun dispose() {
        unregisterListener()
    }
}

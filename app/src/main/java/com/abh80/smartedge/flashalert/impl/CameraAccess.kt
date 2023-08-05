package com.abh80.smartedge.flashalert.impl

import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Handler
import android.util.Log


class CameraAccess(private val mContext: Context) {
    companion object {
        private var isFlashlightOn = false
        private var shouldStroboscopeStop = false
        private var isStroboscopeRunning = false
        private var shouldEnableFlashlight = false
        private var template = default
        private var index = 0
    }

    private val camManager = mContext.getSystemService(Context.CAMERA_SERVICE) as CameraManager

    fun stopStroboscope(){
        Log.d("CameraAccess", "stopStroboscope: ")
        shouldStroboscopeStop = true
    }

    fun toggleStroboscope(templates: Array<Long> = default): Boolean {
        template = templates
        if (!isStroboscopeRunning) {
            disableFlashlight()
        }

        if (isStroboscopeRunning) {
            stopStroboscope()
        } else {
            Thread(stroboscope).start()
        }

        return true
    }

    private val stroboscope = Runnable {
        if (isStroboscopeRunning) {
            return@Runnable
        }

        shouldStroboscopeStop = false
        isStroboscopeRunning = true

        Log.d("CameraAccess", "switchFlash: ")
        try {
            val cameraId = camManager.cameraIdList[0]

            val camChars = camManager.getCameraCharacteristics(cameraId)

            val isTorchAvailable = camChars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE).toString().toBoolean()
            Log.d("CameraAccess", "isTorchAvailable: $isTorchAvailable")

            if(isTorchAvailable) {
                index = 0
                while (!shouldStroboscopeStop) {
                    try {
                        camManager.setTorchMode(cameraId, true)
                        Thread.sleep(template.getTemplateInterval())
                        camManager.setTorchMode(cameraId, false)
                        Thread.sleep(template.getTemplateInterval())
                        Log.d("CameraAccess", "Pass ")
                    } catch (e: Exception) {
                        e.printStackTrace()
                        shouldStroboscopeStop = true
                    }
                }
                isStroboscopeRunning = false
            }
        } catch (e:CameraAccessException) {
            e.printStackTrace()
        }

        isStroboscopeRunning = false
        shouldStroboscopeStop = false
        if (shouldEnableFlashlight) {
            enableFlashlight()
            shouldEnableFlashlight = false
        }
    }

    private fun Array<Long>.getTemplateInterval():Long{
        if(size <= index){
            index = 0
        }
        val value = this[index]
        Log.d("CameraAccess", "getTemplateInterval: ${this[index]}")
        index++
        return value
    }

    private fun stateChanged(isEnabled: Boolean) {
        isFlashlightOn = isEnabled
    }

    private fun disableFlashlight() {
        if (isStroboscopeRunning) {
            return
        }
        camManager.cameraIdList.forEach {
            val camChars = camManager.getCameraCharacteristics(it)
            val isTorchAvailable = camChars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE).toString().toBoolean()
            if(isTorchAvailable)
                camManager.setTorchMode(it,false)
        }
        stateChanged(false)
    }

    private fun enableFlashlight() {
        shouldStroboscopeStop = true
        if (isStroboscopeRunning) {
            shouldEnableFlashlight = true
            return
        }
        camManager.cameraIdList.forEach {
            val camChars = camManager.getCameraCharacteristics(it)
            val isTorchAvailable = camChars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE).toString().toBoolean()
            if(isTorchAvailable)
                camManager.setTorchMode(it,true)
        }

        val mainRunnable = Runnable { stateChanged(true) }
        Handler(mContext.mainLooper).post(mainRunnable)
    }
}
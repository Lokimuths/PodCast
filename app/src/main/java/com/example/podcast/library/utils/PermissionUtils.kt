package com.example.podcast.library.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager

fun Activity.checkAudioAndFilePermission(requestCode: Int): Boolean {
    if (!isGranted(Manifest.permission.RECORD_AUDIO)) {
        requestPermissions(
            arrayOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ),
            requestCode
        )
        return false
    }
    return true
}

fun Context.isGranted(permissionCode: String) =
    checkSelfPermission(permissionCode) == PackageManager.PERMISSION_GRANTED

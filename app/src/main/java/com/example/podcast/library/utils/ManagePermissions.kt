package com.example.podcast.library.utils

import android.app.Activity
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.podcast.R

class ManagePermissions(private val activity: Activity, private val list: List<String>, private val code:Int) {

    fun checkPermissions(): Boolean {
        return if (isPermissionsGranted() != PackageManager.PERMISSION_GRANTED) {
            showAlert()
            false
        } else {
            true
        }
    }


    private fun isPermissionsGranted(): Int {
        var counter = 0
        for (permission in list) {
            counter += ContextCompat.checkSelfPermission(activity, permission)
        }
        return counter
    }


    private fun deniedPermission(): String {
        for (permission in list) {
            if (ContextCompat.checkSelfPermission(activity, permission)
                == PackageManager.PERMISSION_DENIED) return permission
        }
        return ""
    }


    private fun showAlert() {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(R.string.need_permission)
        builder.setMessage(R.string.permission_require)
        builder.setPositiveButton(R.string.com_ok) { _, _ -> requestPermissions() }
        builder.setNeutralButton(R.string.com_cancel, null)
        val dialog = builder.create()
        dialog.show()
    }


    private fun requestPermissions() {
        val permission = deniedPermission()
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
            Toast.makeText(activity, R.string.permission_continue, Toast.LENGTH_SHORT).show()
        } else {
            ActivityCompat.requestPermissions(activity, list.toTypedArray(), code)
        }
    }
}

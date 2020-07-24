package com.francis.pahoservice

import android.util.Log
import android.widget.Toast
import com.francis.pahoservice.base.AppController

object UiUtils {

    fun appLog(TAG: String?, msg: String?) {
        Log.v("$TAG", "$msg")
    }

    fun appErrorLog(TAG: String?, msg: String?) {
        Log.e("$TAG", "$msg")
    }

    fun showToast(msg: String?) {
        Toast.makeText(AppController.instance, "$msg", Toast.LENGTH_SHORT).show()
    }
}
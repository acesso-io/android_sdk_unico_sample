package com.unico.unico_check_exemplo.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.unico.unico_check_exemplo.R


fun showToast(msg: String, context: Context){

    val toast = Toast(context)
    val tvInfo: TextView?
    val llToast: View = LayoutInflater.from(context).inflate(
        R.layout.toast_custom, null
    )

    tvInfo = llToast.findViewById(R.id.toastMessage)

    toast.duration = Toast.LENGTH_SHORT
    toast.view = llToast
    tvInfo.text = msg
    toast.show()
}
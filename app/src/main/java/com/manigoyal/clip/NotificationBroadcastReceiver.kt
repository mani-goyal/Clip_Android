package com.manigoyal.clip

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.getSystemService


class NotificationBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val copiedData = intent.getStringExtra("copiedData")
        val notificationID = intent.getIntExtra("notificationId", 0)
        // if you want cancel notification
        Log.e("NOTIFICATION", copiedData.toString())
        Log.e("NOTIFICATIONID", notificationID.toString())


        val clipBoard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip : ClipData = ClipData.newPlainText("text", copiedData)
        val ret = clipBoard.setPrimaryClip(clip)
        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
        Log.e("RETURNCODE", ret.toString())

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(notificationID)
    }
}
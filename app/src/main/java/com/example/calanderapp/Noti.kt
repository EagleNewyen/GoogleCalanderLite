package com.example.calanderapp

import android.app.Notification
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

const val notificationID = 1
const val channelID = "channel1"
const val eventExtra = "eventExtra"
const val TAG = "FIRESTORE"



// logic to show notification tab at specific time
class Noti: BroadcastReceiver() {

    override fun onReceive(context: Context,intent: Intent) {


        // Notification variable
        val noti : Notification = NotificationCompat.Builder(context, channelID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(intent.getStringExtra(eventExtra))
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationID, noti)

    }



}


package com.example.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class StopAdhanReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        AdhanPlaybackService.stopAdhan(context)
    }
}

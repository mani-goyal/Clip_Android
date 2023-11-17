package com.manigoyal.clip

import android.app.PendingIntent
import android.app.Service
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.createChannel
import io.github.jan.supabase.realtime.decodeRecord
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.websocket.WebSockets
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch


class DataPullService : Service() {


    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        Toast.makeText(this, "Service started", Toast.LENGTH_SHORT).show()

            val client = createSupabaseClient(
                supabaseUrl = SUPABASE_URL,
                supabaseKey = SUPABASE_KEY
            ) {
                install(Postgrest)
                install(Realtime)

                httpEngine = HttpClient(CIO) {
                    install(WebSockets)
                }.engine
            }

            client.realtime.createChannel("listen-insert") {
                //optional config
            }

            val channel = client.realtime.createChannel("listen-insert") {
                //optional config
            }

            GlobalScope.launch {
                client.realtime.connect()
                channel.postgresChangeFlow<PostgresAction.Insert>("public") {
                    table = SUPABASE_TABLE
                }.onEach {
                    when (it) {
                        is PostgresAction.Insert ->
                        {
                            val data: ClipBoard_Data = it.decodeRecord<ClipBoard_Data>()
                            Log.e("DataPullService", data.text)
                            //Toast.makeText(applicationContext, data.text, Toast.LENGTH_SHORT).show()
                            val clipBoard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip : ClipData = ClipData.newPlainText("simple text", data.text)
                            clipBoard.setPrimaryClip(clip)
//
//                            val NOTIFICATION_ID = System.currentTimeMillis().toInt()
//                            val pendingIntent = PendingIntent.getActivity(this@DataPullService, 0, intent, PendingIntent.FLAG_MUTABLE)
//                            val buttonIntent =  Intent(this@DataPullService, NotificationBroadcastReceiver::class.java)
//                            buttonIntent.putExtra("copiedData", data.text)
//                            buttonIntent.putExtra( "notificationId" , NOTIFICATION_ID) ;
//                            val btPendingIntent =
//                                PendingIntent.getBroadcast(this@DataPullService, 0, buttonIntent,
//                                    PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE )
//
//
//                            var builder = NotificationCompat.Builder(this@DataPullService, "default")
//                                .setSmallIcon(R.drawable.ic_launcher_foreground)
//                                .setContentTitle("CLIP")
//                                .setContentText("New text copied")
//                                .setPriority(NotificationCompat.PRIORITY_HIGH)
//                                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
//                                .addAction(R.drawable.ic_launcher_foreground, "Copy", btPendingIntent)
//                                .setContentIntent(pendingIntent)
//                                .setVibrate(longArrayOf(1L, 2L, 3L))
//                            with(NotificationManagerCompat.from(this@DataPullService)) {
//                                this.cancelAll()
//                                this.notify(NOTIFICATION_ID, builder.build())
//                            }


                        }
                    }
                }.launchIn(GlobalScope)

                channel.join()
            }

        return super.onStartCommand(intent, flags, startId);
    }

    protected fun getDeleteIntent(): PendingIntent? {
        val intent = Intent(
            this@DataPullService,
            NotificationBroadcastReceiver::class.java
        )
        intent.action = "notification_cancelled"
        return PendingIntent.getBroadcast(
            this@DataPullService,
            0,
            intent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.e("DatPullServce", "Not yet implemented")
        return TODO("Provide the return value")
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        val restartServiceIntent = Intent(applicationContext, this.javaClass)
        restartServiceIntent.setPackage(packageName)
        startService(restartServiceIntent)
        super.onTaskRemoved(rootIntent)
    }
}
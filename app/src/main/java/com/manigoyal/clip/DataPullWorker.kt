package com.manigoyal.clip

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.createChannel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class DataPullWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("WorkerClass","It's Working")

            val client = createSupabaseClient(
                supabaseUrl = SUPABASE_URL,
                supabaseKey = SUPABASE_KEY
            ) {
                install(Postgrest)
                install(Realtime)
            }

            val channel = client.realtime.createChannel("random") {
                //optional config
            }

            Log.e("DataPullService", "HEREEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE")


            client.realtime.connect()
            channel.postgresChangeFlow<PostgresAction.Insert>("public") {
                table = SUPABASE_TABLE
            }.onEach {
                Log.e("DataPullService", "HERE")
                when (it) {
                    is PostgresAction.Insert -> Log.e("DataPullService", "Insert recieved")
                }
            }

            channel.join()


        //Thread.sleep(100000)
        Log.d("WorkerClass","Going back")
        // Task result
        return Result.success()
    }
}
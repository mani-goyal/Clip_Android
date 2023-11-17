package com.manigoyal.clip

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.manigoyal.clip.databinding.ActivitySecondBinding
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Returning
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
data class ClipBoard_Data(val text:String)
{
    val id: Int = 0
}

class SecondActivity : AppCompatActivity() {
    private  lateinit var binding : ActivitySecondBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySecondBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val action = intent.action

        var text:CharSequence? = ""
        if(Intent.ACTION_SEND == action)
            text = intent.getCharSequenceExtra(Intent.EXTRA_TEXT);
        else
            text = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT);
        Log.e("second", text.toString())
        binding.txtSecondScreen.text = text.toString()
        pushData(text.toString())
    }

    private fun pushData(text:String){
        val client = createSupabaseClient(
            supabaseUrl = SUPABASE_URL_HTTP,
            supabaseKey = SUPABASE_KEY
        ) {
            install(Postgrest)
            //install other modules
        }
        val clippedData = ClipBoard_Data(text = text)

        val clipBoard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip : ClipData = ClipData.newPlainText("simple text", text)
        clipBoard.setPrimaryClip(clip)

        GlobalScope.launch {
            val answer = client.postgrest[SUPABASE_TABLE].insert(clippedData, returning = Returning.MINIMAL)
            Log.d("Second-Activity", answer.toString())
        }
        Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show()
        finish()

    }
}
package xyz.chenzyadb.wanna_recite

import android.content.Context
import android.speech.tts.TextToSpeech
import android.widget.Toast
import java.util.*

class TextToSpeech(private val context: Context, private val text: String) :
    TextToSpeech.OnInitListener {
    private val TTS: TextToSpeech = TextToSpeech(context, this)

    override fun onInit(i: Int) {
        if (i == TextToSpeech.SUCCESS) {
            try {
                val result: Int = TTS.setLanguage(Locale.UK)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(context, "系统TTS不支持英语播报", Toast.LENGTH_LONG).show()
                } else {
                    TTS.setSpeechRate(1.0f)
                    TTS.setPitch(1.0f)
                    TTS.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
                }
            } catch (e: Exception) {
                Toast.makeText(context, "无法调用系统TTS", Toast.LENGTH_LONG).show()

                e.printStackTrace()
            }
        } else {
            Toast.makeText(context, "无法初始化系统TTS", Toast.LENGTH_LONG).show()
        }
    }
}
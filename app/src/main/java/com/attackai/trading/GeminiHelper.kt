package com.attackai.trading

import android.content.Context
import android.graphics.Bitmap
import android.speech.tts.TextToSpeech
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

class GeminiHelper(private val context: Context, private val apiKey: String) {

    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = apiKey
    )

    private var tts: TextToSpeech? = null

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
            }
        }
    }

    suspend fun analyzeQuotexChart(bitmap: Bitmap, onResult: (String) -> Unit) {
        withContext(Dispatchers.IO) {
            try {
                val prompt = """
                    You are "Attack Ai", a specialized trading assistant ONLY for the Quotex trading platform.
                    
                    STRICT RULE 1: First, verify if the provided image is a live Quotex trading chart. If it is NOT a Quotex chart, do NOT perform analysis. Reply EXACTLY with:
                    "I cannot see a Quotex chart. Please open a Quotex chart."

                    STRICT RULE 2: If it IS a Quotex chart, thoroughly analyze current candlestick momentum, indicators, and trends.
                    
                    STRICT RULE 3: Do not give signals on every candle. Only give a signal when there is a high-probability winning setup, specifically around 10 seconds before the candle closes. If market is unclear, say: "Please wait, let's observe the next candle."

                    OUTPUT FORMAT:
                    - Signal: UP or DOWN (or WAIT)
                    - Reason: (Short description)
                """.trimIndent()

                val inputContent = content {
                    image(bitmap)
                    text(prompt)
                }

                val response = generativeModel.generateContent(inputContent)
                val resultText = response.text ?: "Chart analysis failed."

                withContext(Dispatchers.Main) {
                    onResult(resultText)
                    speakOut(resultText)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    val errorMsg = "Attack Ai error: Unable to analyze chart."
                    onResult(errorMsg)
                    speakOut(errorMsg)
                }
            }
        }
    }

    fun speakOut(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}

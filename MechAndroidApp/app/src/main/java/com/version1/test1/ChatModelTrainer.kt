package com.version1.test1

import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class ChatModelTrainer(private val context: Context) {
    private var diagnosticDatasetContent: String = ""
    private val conversationHistory = JSONArray()
    private var datasetAlreadySentOnce = false
    private val secureApiKeyToken: String = "AQ.Ab8RN6KHw-_IKrVekKpGvH90QuKfdmOPQx8aqHfK8yZUzcj9Mg"

    init {
        loadRawDatasetFromAssets()
    }

    private fun loadRawDatasetFromAssets() {
        try {
            val assetStream = context.assets.open("automotive_faults_aktc_obike_et_al.json")
            val streamReader = InputStreamReader(assetStream)
            val bufferedReader = BufferedReader(streamReader)
            val stringBuilder = StringBuilder()

            var lineString: String? = bufferedReader.readLine()
            while (lineString != null) {
                stringBuilder.append(lineString)
                lineString = bufferedReader.readLine()
            }

            diagnosticDatasetContent = stringBuilder.toString()
            bufferedReader.close()
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    private fun buildDatasetPrefixIfNeeded(): String {
        if (datasetAlreadySentOnce) return ""
        datasetAlreadySentOnce = true
        return "Use the following structured automotive database rules as your reference knowledge for this " +
                "entire conversation, including any future messages:\\n\\n" +
                diagnosticDatasetContent.replace("\"", "\\\"").replace("\n", "\\n") + "\\n\\n"
    }

    fun processSymptomAnalysisWithAi(userMessageText: String): String {
        if (diagnosticDatasetContent.isEmpty()) {
            return "Local asset database failed to load."
        }

        val contextualPromptInstructions = "You are the MachFind AI vehicle diagnostic assistant. " +
                buildDatasetPrefixIfNeeded() +
                "New message from user: " + userMessageText.replace("\"", "\\\"") + "\\n\\n" +
                "Keep answers SHORT and conversational. " +
                "CRITICAL FORMATTING RULE: Do not use any markdown formatting symbols under any circumstances. " +
                "At the very end of your response, you MUST append a specialization tag formatted exactly like this: |SPECIALIZATION: [Insert Name]|. " +
                "Choose the single most relevant specialization for the issue from this exact list: ABS & Brake Mechanic, Air Conditioning Mechanic, Cooling System Mechanic, Drivetrain Mechanic, Auto Electrical Mechanic, Emissions System Mechanic, Engine Mechanic, Fuel System Mechanic, Transmission Mechanic, Wheel & Tire Mechanic. " +
                "Output only plain English text followed by the tag."

        return sendTextOnlyRequestToGemini(contextualPromptInstructions, userMessageText)
    }

    fun processImageSymptomAnalysisWithAi(vehicleImageBitmap: Bitmap, userCaptionText: String): String {
        val base64ImageString = encodeBitmapToBase64Jpeg(vehicleImageBitmap)

        val captionPortion = if (userCaptionText.isBlank()) {
            "Rely only on what is visible in the photo."
        } else {
            "Additional description: " + userCaptionText.replace("\"", "\\\"")
        }

        val contextualPromptInstructions = "You are the MachFind AI vehicle diagnostic assistant. " +
                buildDatasetPrefixIfNeeded() +
                captionPortion.replace("\n", "\\n") + "\\n\\n" +
                "Identify what component or issue is visibly shown. Respond briefly and specifically. " +
                "CRITICAL FORMATTING RULE: Do not use any markdown formatting symbols under any circumstances. " +
                "At the very end of your response, you MUST append a specialization tag formatted exactly like this: |SPECIALIZATION: [Insert Name]|. " +
                "Choose the single most relevant specialization for the issue from this exact list: ABS & Brake Mechanic, Air Conditioning Mechanic, Cooling System Mechanic, Drivetrain Mechanic, Auto Electrical Mechanic, Emissions System Mechanic, Engine Mechanic, Fuel System Mechanic, Transmission Mechanic, Wheel & Tire Mechanic. " +
                "Output only plain English text followed by the tag."

        return sendImageRequestToGemini(contextualPromptInstructions, base64ImageString, userCaptionText)
    }

    private fun resizeBitmapForUpload(bitmap: Bitmap, maxDimension: Int = 1024): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        if (width <= maxDimension && height <= maxDimension) return bitmap

        val scaleFactor = maxDimension.toFloat() / maxOf(width, height)
        val newWidth = (width * scaleFactor).toInt().coerceAtLeast(1)
        val newHeight = (height * scaleFactor).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    private fun encodeBitmapToBase64Jpeg(bitmap: Bitmap): String {
        val resizedBitmap = resizeBitmapForUpload(bitmap)
        val byteArrayOutputStream = ByteArrayOutputStream()
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    private fun appendToHistory(role: String, text: String) {
        val turnObject = JSONObject()
        turnObject.put("role", role)
        val partsArray = JSONArray()
        val partObject = JSONObject()
        partObject.put("text", text)
        partsArray.put(partObject)
        turnObject.put("parts", partsArray)
        conversationHistory.put(turnObject)
    }

    private fun sendTextOnlyRequestToGemini(promptText: String, rawUserMessageForHistory: String): String {
        if (diagnosticDatasetContent.isEmpty()) {
            return "Local asset database failed to load."
        }

        try {
            val mainJsonPayload = JSONObject()
            val contentsArray = JSONArray()

            for (i in 0 until conversationHistory.length()) {
                contentsArray.put(conversationHistory.getJSONObject(i))
            }

            val contentObject = JSONObject()
            contentObject.put("role", "user")
            val partsArray = JSONArray()
            val partObject = JSONObject()
            partObject.put("text", promptText)
            partsArray.put(partObject)
            contentObject.put("parts", partsArray)
            contentsArray.put(contentObject)

            mainJsonPayload.put("contents", contentsArray)

            val responseText = executeGeminiRequest(
                "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent?key=" + secureApiKeyToken,
                mainJsonPayload
            )

            appendToHistory("user", rawUserMessageForHistory)
            appendToHistory("model", responseText)

            return responseText
        } catch (exception: Exception) {
            return "Network transmission operation fault: " + exception.message
        }
    }

    private fun sendImageRequestToGemini(promptText: String, base64ImageString: String, rawCaptionForHistory: String): String {
        try {
            val mainJsonPayload = JSONObject()
            val contentsArray = JSONArray()

            for (i in 0 until conversationHistory.length()) {
                contentsArray.put(conversationHistory.getJSONObject(i))
            }

            val contentObject = JSONObject()
            contentObject.put("role", "user")
            val partsArray = JSONArray()

            val textPartObject = JSONObject()
            textPartObject.put("text", promptText)
            partsArray.put(textPartObject)

            val imagePartObject = JSONObject()
            val inlineDataObject = JSONObject()
            inlineDataObject.put("mime_type", "image/jpeg")
            inlineDataObject.put("data", base64ImageString)
            imagePartObject.put("inline_data", inlineDataObject)
            partsArray.put(imagePartObject)

            contentObject.put("parts", partsArray)
            contentsArray.put(contentObject)
            mainJsonPayload.put("contents", contentsArray)

            val responseText = executeGeminiRequest(
                "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent?key=" + secureApiKeyToken,
                mainJsonPayload
            )

            val historyLabel = if (rawCaptionForHistory.isBlank()) {
                "[User sent a photo of their vehicle issue]"
            } else {
                "[User sent a photo of their vehicle issue] " + rawCaptionForHistory
            }
            appendToHistory("user", historyLabel)
            appendToHistory("model", responseText)

            return responseText
        } catch (exception: Exception) {
            return "Network transmission operation fault: " + exception.message
        }
    }

    private fun executeGeminiRequest(urlTargetString: String, mainJsonPayload: JSONObject): String {
        try {
            val destinationUrl = URL(urlTargetString)
            val urlConnection = destinationUrl.openConnection() as HttpURLConnection

            urlConnection.requestMethod = "POST"
            urlConnection.setRequestProperty("Content-Type", "application/json")
            urlConnection.doOutput = true
            urlConnection.doInput = true
            urlConnection.connectTimeout = 30000
            urlConnection.readTimeout = 60000
            urlConnection.setChunkedStreamingMode(0)

            val outputStreamWriter = OutputStreamWriter(urlConnection.outputStream)
            outputStreamWriter.write(mainJsonPayload.toString())
            outputStreamWriter.flush()
            outputStreamWriter.close()

            val connectionResponseCode = urlConnection.responseCode
            if (connectionResponseCode == HttpURLConnection.HTTP_OK) {
                val streamReader = InputStreamReader(urlConnection.inputStream)
                val bufferedReader = BufferedReader(streamReader)
                val responseStringBuilder = StringBuilder()

                var rowLine: String? = bufferedReader.readLine()
                while (rowLine != null) {
                    responseStringBuilder.append(rowLine)
                    rowLine = bufferedReader.readLine()
                }
                bufferedReader.close()

                val masterJsonObject = JSONObject(responseStringBuilder.toString())
                val candidatesArray = masterJsonObject.getJSONArray("candidates")
                val firstCandidate = candidatesArray.getJSONObject(0)
                val contentNode = firstCandidate.getJSONObject("content")
                val partsNodeArray = contentNode.getJSONArray("parts")
                val textPayloadString = partsNodeArray.getJSONObject(0).getString("text")

                return textPayloadString
            } else {
                return "Server validation query response error code: " + connectionResponseCode
            }
        } catch (networkException: Exception) {
            return "Network transmission operation fault: " + networkException.message
        }
    }
}
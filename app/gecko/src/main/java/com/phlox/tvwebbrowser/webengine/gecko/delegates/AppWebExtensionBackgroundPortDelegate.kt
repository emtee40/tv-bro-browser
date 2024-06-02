package com.phlox.tvwebbrowser.webengine.gecko.delegates

import android.net.Uri
import android.util.Log
import com.phlox.tvwebbrowser.webengine.gecko.GeckoWebEngine
import org.json.JSONObject
import org.mozilla.geckoview.WebExtension

class AppWebExtensionBackgroundPortDelegate(val port: WebExtension.Port, val webEngine: GeckoWebEngine): WebExtension.PortDelegate {
    override fun onPortMessage(message: Any, port: WebExtension.Port) {
        //Log.d(TAG, "onPortMessage: $message")
        try {
            val msgJson = message as JSONObject
            when (msgJson.getString("action")) {
                "onBeforeRequest" -> {
                    Log.i(TAG, "onBeforeRequest: " + msgJson.toString())
                    val data = msgJson.getJSONObject("details")
                    val requestId = data.getInt("requestId")
                    val url = data.getString("url")
                    val originUrl = data.getString("originUrl") ?: ""
                    val type = data.getString("type")
                    val callback = webEngine.callback ?: return
                    val msg = JSONObject()
                    msg.put("action", "onResolveRequest")
                    val block = if (callback.isAdBlockingEnabled()) {
                        callback.isAd(Uri.parse(url), type, Uri.parse(originUrl)) ?: false
                    } else {
                        false
                    }
                    if (block) {
                        callback.onBlockedAd(url)
                    }
                    msg.put("data", JSONObject().put("requestId", requestId).put("block", block))
                    port.postMessage(msg)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDisconnect(port: WebExtension.Port) {
        Log.d(TAG, "onDisconnect")
        webEngine.appHomeContentScriptPortDelegate = null
    }

    companion object {
        val TAG: String = AppWebExtensionBackgroundPortDelegate::class.java.simpleName
    }
}
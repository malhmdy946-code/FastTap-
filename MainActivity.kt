package com.fasttap.app

import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardItem

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    private var rewardedAd: RewardedAd? = null
    private var interstitialAd: InterstitialAd? = null

    companion object {

        // وحدات AdMob الحقيقية الخاصة بك
        private const val REWARDED_AD_ID =
            "ca-app-pub-6216579901312801/8450380783"

        private const val INTERSTITIAL_AD_ID =
            "ca-app-pub-6216579901312801/3706792747"

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MobileAds.initialize(this) {

            runOnUiThread {

                loadRewardedAd()
                loadInterstitialAd()

            }

        }

        webView = WebView(this)

        webView.settings.apply {

            javaScriptEnabled = true
            domStorageEnabled = true

            allowFileAccess = true
            allowContentAccess = true

        }

        webView.webViewClient = WebViewClient()
        webView.webChromeClient = WebChromeClient()

        webView.addJavascriptInterface(
            FastTapBridge(),
            "FastTapAndroid"
        )

        setContentView(webView)

        webView.loadUrl("file:///android_asset/index.html")
    }

    // ==========================================
    // تحميل Rewarded Ad
    // ==========================================

    private fun loadRewardedAd() {

        val request = AdRequest.Builder().build()

        RewardedAd.load(
            this,
            REWARDED_AD_ID,
            request,
            object : RewardedAdLoadCallback() {

                override fun onAdLoaded(ad: RewardedAd) {

                    rewardedAd = ad

                }

                override fun onAdFailedToLoad(error: LoadAdError) {

                    rewardedAd = null

                }

            }
        )
    }

    // ==========================================
    // تحميل Interstitial
    // ==========================================

    private fun loadInterstitialAd() {

        val request = AdRequest.Builder().build()

        InterstitialAd.load(
            this,
            INTERSTITIAL_AD_ID,
            request,
            object : InterstitialAdLoadCallback() {

                override fun onAdLoaded(ad: InterstitialAd) {

                    interstitialAd = ad

                }

                override fun onAdFailedToLoad(error: LoadAdError) {

                    interstitialAd = null

                }

            }
        )
    }

    // ==========================================
    // JavaScript Bridge
    // ==========================================

    inner class FastTapBridge {

        @JavascriptInterface
        fun showRewardedAd(type: String) {

            runOnUiThread {

                val ad = rewardedAd

                if (ad == null) {

                    loadRewardedAd()

                    return@runOnUiThread
                }

                ad.show(
                    this@MainActivity
                ) { _: RewardItem ->

                    webView.evaluateJavascript(
                        "FastTapAdReward('$type')",
                        null
                    )

                    rewardedAd = null

                    loadRewardedAd()
                }
            }
        }

        @JavascriptInterface
        fun showInterstitial() {

            runOnUiThread {

                val ad = interstitialAd

                if (ad == null) {

                    loadInterstitialAd()

                    return@runOnUiThread
                }

                ad.show(this@MainActivity)

                interstitialAd = null

                loadInterstitialAd()
            }
        }
    }

    // ==========================================
    // زر الرجوع
    // ==========================================

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {

        if (webView.canGoBack()) {

            webView.goBack()

        } else {

            super.onBackPressed()

        }
    }

}

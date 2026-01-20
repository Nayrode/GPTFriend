package com.dtetu.gptfriend.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

/**
 * Manages interstitial ad loading and display
 */
class AdManager(private val context: Context) {
    
    companion object {
        private const val TAG = "AdManager"
        // Test ad unit ID for interstitial ads
        // Replace with your actual ad unit ID in production
        private const val AD_UNIT_ID = "ca-app-pub-6999926769777443/7403807634"
    }
    
    private var interstitialAd: InterstitialAd? = null
    private var isAdLoading = false
    
    /**
     * Initialize the Mobile Ads SDK
     */
    fun initialize() {
        MobileAds.initialize(context) { initializationStatus ->
            Log.d(TAG, "AdMob SDK initialized: ${initializationStatus.adapterStatusMap}")
        }
    }
    
    /**
     * Load an interstitial ad
     */
    fun loadAd() {
        // Prevent loading multiple ads at once
        if (isAdLoading || interstitialAd != null) {
            Log.d(TAG, "Ad is already loaded or loading")
            return
        }
        
        isAdLoading = true
        val adRequest = AdRequest.Builder().build()
        
        InterstitialAd.load(
            context,
            AD_UNIT_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d(TAG, "Interstitial ad loaded successfully")
                    interstitialAd = ad
                    isAdLoading = false
                }
                
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.e(TAG, "Failed to load ad: ${loadAdError.message}")
                    interstitialAd = null
                    isAdLoading = false
                }
            }
        )
    }
    
    /**
     * Show the interstitial ad if it's loaded
     * @param activity The activity context for showing the ad
     * @param onAdClosed Callback for when the ad is closed
     */
    fun showAd(activity: Activity, onAdClosed: () -> Unit = {}) {
        val ad = interstitialAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Ad was dismissed")
                    interstitialAd = null
                    onAdClosed()
                    // Preload the next ad
                    loadAd()
                }
                
                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.e(TAG, "Ad failed to show: ${adError.message}")
                    interstitialAd = null
                    onAdClosed()
                }
                
                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Ad showed fullscreen content")
                }
            }
            
            ad.show(activity)
        } else {
            Log.d(TAG, "Interstitial ad is not loaded yet")
            onAdClosed()
            // Try to load for next time
            loadAd()
        }
    }
    
    /**
     * Check if an ad is ready to be shown
     */
    fun isAdReady(): Boolean {
        return interstitialAd != null
    }
}

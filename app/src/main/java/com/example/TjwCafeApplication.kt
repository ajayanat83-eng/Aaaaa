package com.example

import android.app.Application
import android.content.Context
import android.util.Log
import com.example.data.CafeRepository
import com.example.data.FirestoreMenuRepository
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class TjwCafeApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initFirebase(this)
        // Initialize Room local cache database early
        FirestoreMenuRepository.instance.initLocalDatabase(applicationContext)
    }

    companion object {
        private const val TAG = "TjwCafeApp"

        fun initFirebase(context: Context) {
            try {
                if (FirebaseApp.getApps(context).isEmpty()) {
                    try {
                        FirebaseApp.initializeApp(context)
                        Log.d(TAG, "FirebaseApp initialized via default context.")
                    } catch (e: Exception) {
                        Log.i(TAG, "Initializing Firebase with project configuration fallback: ${e.message}")
                        val options = FirebaseOptions.Builder()
                            .setApplicationId("1:41001592366:android:tjwcafejanakpur")
                            .setProjectId("tjwcafe-janakpur")
                            .setApiKey("AIzaSyD-tjwcafe-janakpur-nepal-2026-auth-key")
                            .setDatabaseUrl("https://tjwcafe-janakpur.firebaseio.com")
                            .setStorageBucket("tjwcafe-janakpur.appspot.com")
                            .build()
                        FirebaseApp.initializeApp(context, options)
                        Log.d(TAG, "FirebaseApp initialized successfully with fallback options.")
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "FirebaseApp initialization handled: ${e.message}")
            }
        }
    }
}

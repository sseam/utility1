package com.sseam.android.utility1

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.sseam.android.utility1.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var _binding : ActivityMainBinding
    private var BackKeyBeforeTime = 0L
    private lateinit var adView : AdView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(_binding.root)

        val toolbar = _binding.toolbar
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.baseline_edgesensor_high_24)

        MobileAds.initialize(this) {}
        adView = _binding.adView
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        _binding.button1.setOnClickListener {
            val intent = Intent(this, MagneticSensorActivity::class.java)
            startActivity(intent)
        }

        _binding.button2.setOnClickListener {
            val intent = Intent(this, CompassActivity::class.java)
            startActivity(intent)
        }

        _binding.button3.setOnClickListener {
            val intent = Intent(this, LevelSensorActivity::class.java)
            startActivity(intent)
        }

        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val now = System.currentTimeMillis()
                val result = now - BackKeyBeforeTime

                if (result < 2000) {
                    finish()
                } else {
                    Toast.makeText(this@MainActivity, R.string.msg_backkey, Toast.LENGTH_SHORT).show()
                    BackKeyBeforeTime = System.currentTimeMillis()
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

    }

    public override fun onPause() {
        super.onPause()
        adView.pause();
    }
    public override fun onResume() {
        super.onResume()
        adView.resume()
    }
    public override fun onDestroy() {
        adView.destroy();
        super.onDestroy()
    }

}
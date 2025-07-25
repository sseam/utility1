package com.sseam.android.utility1

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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
        enableEdgeToEdge()
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(_binding.root)

        //하단에만 적절한 패딩 추가.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_layout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            // 현재 뷰의 기존 패딩 값을 가져옵니다.
            // Appbar가 상단 패딩을 처리했으므로, 여기서는 기존 top 패딩을 유지합니다.
            val initialPaddingTop = v.paddingTop
            val initialPaddingLeft = v.paddingLeft
            val initialPaddingRight = v.paddingRight

            // 하단 시스템 바 인셋만 적용합니다.
            // 기존의 좌, 상, 우 패딩은 유지하고, 하단 패딩만 systemBars.bottom으로 설정합니다.
            v.setPadding(
                initialPaddingLeft + systemBars.left,   // 기존 좌측 패딩 유지 + 시스템 바 좌측 인셋
                initialPaddingTop,                      // 기존 상단 패딩 유지 (Appbar가 처리했으므로)
                initialPaddingRight + systemBars.right, // 기존 우측 패딩 유지 + 시스템 바 우측 인셋
                systemBars.bottom                       // 하단 내비게이션 바 인셋 적용
            )
            insets
        }

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
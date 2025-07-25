package com.sseam.android.utility1

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.sseam.android.utility1.databinding.ActivityCompassBinding


class CompassActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var compassImage: ImageView

    private lateinit var _binding : ActivityCompassBinding

    private lateinit var adView : AdView

    private lateinit var sensorManager: SensorManager
    private lateinit var rotationSensor: Sensor
    private var currentDegree = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        _binding = ActivityCompassBinding.inflate(layoutInflater)
        setContentView(_binding.root)

        //하단에만 적절한 패딩 추가.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.compass_layout)) { v, insets ->
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
        supportActionBar!!.setTitle(R.string.title_compass)

        MobileAds.initialize(this) {}
        adView = _binding.adView
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        compassImage = _binding.ivCompass

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)!!

    }

    override fun onResume() {
        super.onResume()

        sensorManager.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_UI)

        adView.resume()
    }

    override fun onPause() {
        super.onPause()

        sensorManager.unregisterListener(this, rotationSensor)

        adView.pause()
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
            val rotationMatrix = FloatArray(9)
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            val orientation = FloatArray(3)
            SensorManager.getOrientation(rotationMatrix, orientation)
            var azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
            azimuth = (azimuth + 360) % 360  // 0~360도로 보정

            // 부드러운 이동: Moving Average(간단 예)
            azimuth = filterAzimuth(azimuth)

            val animation = RotateAnimation(
                currentDegree, -azimuth,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
            )
            animation.duration = 200
            animation.fillAfter = true
            compassImage.startAnimation(animation)
            currentDegree = -azimuth
        }

    }

    private val azimuthHistory = ArrayDeque<Float>()
    private val windowSize = 6
    private fun filterAzimuth(raw: Float): Float {
        azimuthHistory.add(raw)
        if (azimuthHistory.size > windowSize) azimuthHistory.removeFirst()
        return azimuthHistory.average().toFloat()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // 센서 정확도 변경 시 호출됨
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    public override fun onDestroy() {
        adView.destroy();
        super.onDestroy()
    }

}
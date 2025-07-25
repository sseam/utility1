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
import android.view.animation.TranslateAnimation
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.sseam.android.utility1.databinding.ActivityLevelSensorBinding
import java.lang.Math.sin
import kotlin.math.abs

class LevelSensorActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var magnetometer: Sensor? = null
    private var gravity = FloatArray(3)
    private var geomagnetic = FloatArray(3)
    private var lastPitch = 0.0
    private var lastRoll = 0.0
    private val alpha = 0.1  // 로우패스 필터 계수

    private lateinit var _binding : ActivityLevelSensorBinding
    private lateinit var adView : AdView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        _binding = ActivityLevelSensorBinding.inflate(layoutInflater)
        setContentView(_binding.root)

        //하단에만 적절한 패딩 추가.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.level_sensor_layout)) { v, insets ->
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
        supportActionBar!!.setTitle(R.string.title_level)

        MobileAds.initialize(this) {}
        adView = _binding.adView
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        // 크로스헤어 중심 이동 (정중앙 기준)
        _binding.ivCrosshair.pivotX = _binding.ivCrosshair.width / 2f
        _binding.ivCrosshair.pivotY = _binding.ivCrosshair.height / 2f

    }

    override fun onResume() {
        super.onResume()
        accelerometer?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        }
        magnetometer?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        }
        adView.resume()
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
        adView.pause()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}


    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> gravity = event.values.clone()
            Sensor.TYPE_MAGNETIC_FIELD -> geomagnetic = event.values.clone()
        }

        val Rx = FloatArray(9)
        val Ix = FloatArray(9)

        if (SensorManager.getRotationMatrix(Rx, Ix, gravity, geomagnetic)) {
            val orientation = FloatArray(3)
            SensorManager.getOrientation(Rx, orientation)

            val pitch = Math.toDegrees(orientation[1].toDouble()) // 전후 기울기
            val roll = Math.toDegrees(orientation[2].toDouble())  // 좌우 기울기

            // **로우패스 필터 적용** (노이즈 감소)
            lastPitch = lastPitch + alpha * (pitch - lastPitch)
            lastRoll = lastRoll + alpha * (roll - lastRoll)

            _binding.tvAngle.text = String.format("Pitch: %.1f°  Roll: %.1f°", lastPitch, lastRoll)

            // **크로스헤어 실제 위치 이동 (자연스러운 움직임)**
            _binding.ivCrosshair.translationX = (sin(Math.toRadians(-lastRoll)) * 100).toFloat()
            _binding.ivCrosshair.translationY = (sin(Math.toRadians(lastPitch)) * 100).toFloat()

            // **수평 오차 안내 (±1도 이내면 색상/메시지 강조)**
            if (abs(lastPitch) < 1.0 && abs(lastRoll) < 1.0) {
                _binding.tvAngle.setTextColor(getColor(R.color.RetroColor11))
                _binding.tvStatus.text = getString(R.string.msg_perfectly_level)
            } else {
                _binding.tvAngle.setTextColor(getColor(R.color.defaultFontColor))
                _binding.tvStatus.text = ""
            }
        }
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

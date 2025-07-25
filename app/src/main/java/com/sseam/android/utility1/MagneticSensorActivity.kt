package com.sseam.android.utility1

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.sseam.android.utility1.databinding.ActivityMagneticSensorBinding
import kotlin.math.sqrt

class MagneticSensorActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var magneticSensor: Sensor
    private var lastMagnitude = 0.0
    private val alpha = 0.1

    private lateinit var _binding : ActivityMagneticSensorBinding

    private lateinit var adView : AdView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        _binding = ActivityMagneticSensorBinding.inflate(layoutInflater)
        setContentView(_binding.root)

        //하단에만 적절한 패딩 추가.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.magnetic_sensor_layout)) { v, insets ->
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
        supportActionBar!!.setTitle(R.string.title_magnetic_field)

        MobileAds.initialize(this) {}
        adView = _binding.adView
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)!!
    }

    override fun onResume() {
        super.onResume()
        // 자기장 센서 리스너 등록
        sensorManager.registerListener(this, magneticSensor, SensorManager.SENSOR_DELAY_UI)
        adView.resume()
    }

    override fun onPause() {
        super.onPause()
        // 자기장 센서 리스너 해제
        sensorManager.unregisterListener(this)
        adView.pause()
    }

    override fun onSensorChanged(event: SensorEvent) {
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]
        val magnitude = sqrt(x * x + y * y + z * z)
        lastMagnitude = lastMagnitude + alpha * (magnitude - lastMagnitude)
        _binding.magneticTextView.text = String.format("%.2f μT", lastMagnitude)
        // 강한 자기장 경고
        if (lastMagnitude > 60.0) {
            _binding.warningTextView.text = getString(R.string.msg_strong_magnetic)
        } else {
            _binding.warningTextView.text = ""
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        if (sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            _binding.accuracyTextView.text = when (accuracy) {
                SensorManager.SENSOR_STATUS_UNRELIABLE -> getString(R.string.SENSOR_STATUS_UNRELIABLE)
                SensorManager.SENSOR_STATUS_ACCURACY_LOW -> getString(R.string.SENSOR_STATUS_ACCURACY_LOW)
                SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> getString(R.string.SENSOR_STATUS_ACCURACY_MEDIUM)
                SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> getString(R.string.SENSOR_STATUS_ACCURACY_HIGH)
                else -> getString(R.string.SENSOR_STATUS_UNKNOWN)
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
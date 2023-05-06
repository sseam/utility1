package com.sseam.android.utility1

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.sseam.android.utility1.databinding.ActivityMagneticSensorBinding

class MagneticSensorActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var magneticSensor: Sensor

    private lateinit var _binding : ActivityMagneticSensorBinding
    private lateinit var adView : AdView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMagneticSensorBinding.inflate(layoutInflater)
        setContentView(_binding.root)

        val toolbar = _binding.toolbar
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setTitle(R.string.title_magnetic_field)

        MobileAds.initialize(this) {}
        adView = _binding.adView
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        // SensorManager 객체 생성
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        // 자기장 센서 객체 생성
        magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)


    }

    override fun onResume() {
        super.onResume()
        // 자기장 센서 리스너 등록
        sensorManager.registerListener(this, magneticSensor, SensorManager.SENSOR_DELAY_NORMAL)
        adView.resume()

    }

    override fun onPause() {
        super.onPause()
        // 자기장 센서 리스너 해제
        sensorManager.unregisterListener(this)
        adView.pause()
    }

    override fun onSensorChanged(event: SensorEvent) {
        val magneticField = event.values[0]
        _binding.magneticTextView.text = String.format("%.2f", magneticField) + " μT"
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // 센서 정확도 변경 시 호출됨
        if (sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            when (accuracy) {
                SensorManager.SENSOR_STATUS_UNRELIABLE -> {
                    Toast.makeText(this, "Magnetic field sensor accuracy is unreliable", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    Toast.makeText(this, "Magnetic field sensor accuracy is reliable", Toast.LENGTH_SHORT).show()
                }
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
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
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.sseam.android.utility1.databinding.ActivityLevelSensorBinding
import java.lang.Math.sin

class LevelSensorActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var magnetometer: Sensor? = null
    private var gravity = FloatArray(3)
    private var geomagnetic = FloatArray(3)

    private lateinit var _binding : ActivityLevelSensorBinding
    private lateinit var adView : AdView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityLevelSensorBinding.inflate(layoutInflater)
        setContentView(_binding.root)

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

        _binding.ivCrosshair.pivotY = 0f
        _binding.ivCrosshair.pivotY = 0f

    }

    override fun onResume() {
        super.onResume()
        accelerometer?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
        magnetometer?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
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

            val pitch = orientation[1]
            val roll = orientation[2]

            val angle = Math.toDegrees(orientation[1].toDouble())
            _binding.tvAngle.text = String.format("%s: %.1f",getString(R.string.title_angle), angle)

            val xTranslate = (sin(-roll.toDouble()) * 100).toFloat()
            val yTranslate = (sin(pitch.toDouble()) * 100).toFloat()

            val translateAnimation = TranslateAnimation(
                Animation.ABSOLUTE, xTranslate,
                Animation.ABSOLUTE, xTranslate,
                Animation.ABSOLUTE, yTranslate,
                Animation.ABSOLUTE, yTranslate
            )
            translateAnimation.duration = 100
            translateAnimation.fillAfter = true
            _binding.ivCrosshair.startAnimation(translateAnimation)
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

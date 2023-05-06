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
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.sseam.android.utility1.databinding.ActivityCompassBinding


class CompassActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var compassImage: ImageView
    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometer: Sensor
    private lateinit var magnetometer: Sensor

    private var lastAccelerometer = FloatArray(3)
    private var lastMagnetometer = FloatArray(3)
    private var lastAccelerometerSet = false
    private var lastMagnetometerSet = false
    private var currentDegree = 0f

    private lateinit var _binding : ActivityCompassBinding

    private lateinit var simpleKalmanFilter: SimpleKalmanFilter
    private lateinit var adView : AdView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityCompassBinding.inflate(layoutInflater)
        setContentView(_binding.root)

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
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        simpleKalmanFilter = SimpleKalmanFilter(q = 0.1f, r = 0.1f)
    }

    override fun onResume() {
        super.onResume()

        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL)
        adView.resume()
    }

    override fun onPause() {
        super.onPause()

        sensorManager.unregisterListener(this, accelerometer)
        sensorManager.unregisterListener(this, magnetometer)
        adView.pause()
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            lowPass(event.values.clone(), lastAccelerometer)
            lastAccelerometerSet = true
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            lowPass(event.values.clone(), lastMagnetometer)
            lastMagnetometerSet = true
        }

        if (lastAccelerometerSet && lastMagnetometerSet) {
            val rotationMatrix = FloatArray(9)
            val success = SensorManager.getRotationMatrix(rotationMatrix, null, lastAccelerometer, lastMagnetometer)
            if (success) {
                val orientation = FloatArray(3)
                SensorManager.getOrientation(rotationMatrix, orientation)
                val azimuthInRadians = orientation[0]
                val azimuthInDegrees = Math.toDegrees(azimuthInRadians.toDouble()).toFloat()

                // Apply Simple Kalman filter
                val filteredAzimuthInDegrees = simpleKalmanFilter.correct(azimuthInDegrees)

                val animation = RotateAnimation(
                    -currentDegree,
                    -filteredAzimuthInDegrees,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f
                )
                animation.duration = 250
                animation.fillAfter = true

                compassImage.startAnimation(animation)
                currentDegree = -filteredAzimuthInDegrees
            }
        }
    }
    private fun lowPass(input: FloatArray, output: FloatArray?): FloatArray {
        if (output == null) return input

        val alpha = 0.1f
        for (i in input.indices) {
            output[i] = output[i] + alpha * (input[i] - output[i])
        }
        return output
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // 센서 정확도 변경 시 호출됨
    }

    class SimpleKalmanFilter(var q: Float, var r: Float, var p: Float = 0f, var x: Float = 0f, var k: Float = 0f) {
        fun correct(measurement: Float): Float {
            k = p + q / (p + q + r)
            x += k * (measurement - x)
            p = (1 - k) * (p + q)
            return x
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
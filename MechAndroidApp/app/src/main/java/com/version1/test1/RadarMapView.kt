package com.version1.test1

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Overlay

class RadarMapOverlay(private val userLocation: GeoPoint) : Overlay() {
    private val yellowDotPaint = Paint()
    private val yellowDotRimPaint = Paint()
    private val radarWavePaint = Paint()
    private var pulseRadius = 0f
    private val maxPulseRadius = 300f
    private val pulseSpeed = 3.5f

    init {
        yellowDotPaint.color = Color.parseColor("#F0B400")
        yellowDotPaint.style = Paint.Style.FILL
        yellowDotPaint.isAntiAlias = true

        yellowDotRimPaint.color = Color.WHITE
        yellowDotRimPaint.style = Paint.Style.STROKE
        yellowDotRimPaint.strokeWidth = 7f
        yellowDotRimPaint.isAntiAlias = true

        radarWavePaint.color = Color.parseColor("#F0B400")
        radarWavePaint.style = Paint.Style.STROKE
        radarWavePaint.strokeWidth = 5f
        radarWavePaint.isAntiAlias = true
    }

    override fun draw(canvas: Canvas, mapView: MapView, shadow: Boolean) {
        if (shadow) return

        val mapProjection = mapView.projection
        val screenPositionPoint = Point()
        mapProjection.toPixels(userLocation, screenPositionPoint)

        val centerX = screenPositionPoint.x.toFloat()
        val centerY = screenPositionPoint.y.toFloat()

        pulseRadius += pulseSpeed
        if (pulseRadius > maxPulseRadius) {
            pulseRadius = 0f
        }

        val calculatedAlpha = ((1f - (pulseRadius / maxPulseRadius)) * 160).toInt()
        radarWavePaint.alpha = calculatedAlpha
        canvas.drawCircle(centerX, centerY, pulseRadius, radarWavePaint)

        radarWavePaint.alpha = (calculatedAlpha * 0.25f).toInt()
        radarWavePaint.style = Paint.Style.FILL
        canvas.drawCircle(centerX, centerY, pulseRadius, radarWavePaint)
        radarWavePaint.style = Paint.Style.STROKE

        canvas.drawCircle(centerX, centerY, 22f, yellowDotPaint)
        canvas.drawCircle(centerX, centerY, 22f, yellowDotRimPaint)

        mapView.postInvalidateDelayed(16)
    }
}
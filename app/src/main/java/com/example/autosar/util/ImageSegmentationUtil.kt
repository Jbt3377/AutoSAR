package com.example.autosar.util

import android.graphics.Bitmap
import com.mapbox.geojson.Point
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.MatOfPoint2f
import org.opencv.imgproc.Imgproc

object ImageSegmentationUtil {

    /**
     * Extracts polygons from a given bitmap image using OpenCV.
     *
     * @param bitmap The input bitmap image.
     * @param center The geographic center point of the bitmap image.
     * @param zoom The map zoom level at which the bitmap was captured.
     * @return A list of polygons, where each polygon is represented by a list of GeoJSON Points.
     */
    fun extractPolygonsFromImage(
        bitmap: Bitmap,
        center: Point,
        zoom: Double
    ): List<List<Point>> {
        // Convert Bitmap to Mat
        val mat = Mat()
        Utils.bitmapToMat(bitmap, mat)

        // Convert to grayscale
        val grayMat = Mat()
        Imgproc.cvtColor(mat, grayMat, Imgproc.COLOR_BGR2GRAY)

        // Apply thresholding to get a binary image
        val threshMat = Mat()
        Imgproc.threshold(grayMat, threshMat, 128.0, 255.0, Imgproc.THRESH_BINARY)

        // Find contours
        val contours = mutableListOf<MatOfPoint>()
        val hierarchy = Mat()
        Imgproc.findContours(
            threshMat,
            contours,
            hierarchy,
            Imgproc.RETR_EXTERNAL,
            Imgproc.CHAIN_APPROX_SIMPLE
        )

        val allPolygons = mutableListOf<List<Point>>()

        // Approximate and simplify polygons
        for (contour in contours) {
            val contour2f = MatOfPoint2f(*contour.toArray())
            val approxCurve = MatOfPoint2f()
            val epsilon = 0.01 * Imgproc.arcLength(contour2f, true)
            Imgproc.approxPolyDP(contour2f, approxCurve, epsilon, true)

            // Filter out small or insignificant polygons
            if (Imgproc.contourArea(approxCurve) > 100) {
                val approxPoints = approxCurve.toList()
                if (approxPoints.size >= 3) {
                    val geoJsonPoints = convertPixelToGeo(
                        pixels = approxPoints,
                        center = center,
                        zoom = zoom,
                        tileWidth = bitmap.width,
                        tileHeight = bitmap.height
                    )
                    allPolygons.add(geoJsonPoints)
                }
            }
        }
        return allPolygons
    }

    /**
     * Converts a list of pixel coordinates (from OpenCV) to geographic coordinates (Mapbox Points).
     *
     * @param pixels List of OpenCV points representing pixel coordinates.
     * @param center The geographic center of the tile.
     * @param zoom The zoom level of the tile.
     * @param tileWidth The width of the tile in pixels.
     * @param tileHeight The height of the tile in pixels.
     * @return A list of Mapbox GeoJSON Points.
     */
    private fun convertPixelToGeo(
        pixels: List<org.opencv.core.Point>,
        center: Point,
        zoom: Double,
        tileWidth: Int,
        tileHeight: Int
    ): List<Point> {
        val scale = Math.pow(2.0, zoom)
        val centerPixelX = tileWidth / 2.0
        val centerPixelY = tileHeight / 2.0

        // Convert center lat/lon to world coordinates
        val centerLonRad = Math.toRadians(center.longitude())
        val centerLatRad = Math.toRadians(center.latitude())
        val worldCoordX = (center.longitude() + 180) / 360
        val worldCoordY = (1 - Math.log(Math.tan(centerLatRad) + 1 / Math.cos(centerLatRad)) / Math.PI) / 2

        return pixels.map { pixel ->
            // Calculate the pixel's offset from the center of the tile
            val pixelOffsetX = pixel.x - centerPixelX
            val pixelOffsetY = pixel.y - centerPixelY

            // Calculate the pixel's world coordinate
            val pointWorldX = worldCoordX + pixelOffsetX / (tileWidth * scale)
            val pointWorldY = worldCoordY + pixelOffsetY / (tileHeight * scale)

            // Convert the world coordinate back to lat/lon
            val lonDeg = pointWorldX * 360 - 180
            val n = Math.PI - 2 * Math.PI * pointWorldY
            val latDeg = Math.toDegrees(Math.atan(0.5 * (Math.exp(n) - Math.exp(-n))))

            Point.fromLngLat(lonDeg, latDeg)
        }
    }
}

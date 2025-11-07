package com.example.autosar.util

import android.content.Context
import android.graphics.Bitmap
import com.mapbox.geojson.Point
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import org.opencv.core.TermCriteria
import org.opencv.core.Core
import org.opencv.core.CvType

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
        context: Context,
        bitmap: Bitmap,
        center: Point,
        zoom: Double
    ): List<List<Point>> {
        return extractPolygonsFromImageApproach1(
            context = context,
            bitmap = bitmap,
            center = center,
            zoom = zoom
        )
    }

    fun extractPolygonsFromImageApproach1(
        context: Context,
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
        SaveImageUtil.saveMatAsImage(context, grayMat, "01_grayscale")

        // Apply Gaussian blur to reduce noise and smooth the image
        val blurredMat = Mat()
        Imgproc.GaussianBlur(grayMat, blurredMat, Size(15.0, 15.0), 5.0)
        SaveImageUtil.saveMatAsImage(context, blurredMat, "02_gaussian_blur")

        // Apply thresholding to get a binary image
        val threshMat = Mat()
        Imgproc.adaptiveThreshold(
            blurredMat,
            threshMat,
            255.0,
            Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
            Imgproc.THRESH_BINARY,
            35,
            5.0
        )
        SaveImageUtil.saveMatAsImage(context, threshMat, "03_threshold")

        // Morphological smoothing
        val kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, Size(7.0, 7.0))
        Imgproc.morphologyEx(threshMat, threshMat, Imgproc.MORPH_CLOSE, kernel)
        Imgproc.morphologyEx(threshMat, threshMat, Imgproc.MORPH_OPEN, kernel)
        SaveImageUtil.saveMatAsImage(context, threshMat, "04_morph_cleaned")

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

    fun extractPolygonsFromImageApproach2(
        context: Context,
        bitmap: Bitmap,
        center: Point,
        zoom: Double
    ): List<List<Point>> {
        // Convert Bitmap to Mat
        val mat = Mat()
        Utils.bitmapToMat(bitmap, mat)

        // Convert to LAB color space (helps separate color differences like vegetation/soil)
        val labMat = Mat()
        Imgproc.cvtColor(mat, labMat, Imgproc.COLOR_BGR2Lab)
        SaveImageUtil.saveMatAsImage(context, labMat, "01_lab")

        // Apply CLAHE (enhance contrast in L-channel)
        val labChannels = mutableListOf<Mat>()
        Core.split(labMat, labChannels)
        val clahe = Imgproc.createCLAHE(2.0, Size(8.0, 8.0))
        clahe.apply(labChannels[0], labChannels[0])
        Core.merge(labChannels, labMat)
        SaveImageUtil.saveMatAsImage(context, labMat, "02_clahe_lab")

        // Flatten image for clustering (K-Means expects Nx3 float data)
        val samples = labMat.reshape(1, labMat.rows() * labMat.cols())
        samples.convertTo(samples, CvType.CV_32F)

        // Run K-Means clustering
        val k = 4
        val criteria = TermCriteria(TermCriteria.EPS + TermCriteria.MAX_ITER, 15, 1.0)
        val labels = Mat()
        val centers = Mat()
        Core.kmeans(samples, k, labels, criteria, 3, Core.KMEANS_PP_CENTERS, centers)

        // Ensure centers are float so Mat.get() works
        centers.convertTo(centers, CvType.CV_32F)

        val clustered = Mat(labMat.size(), labMat.type())
        val numChannels = centers.cols() // should be 3

        var labelIndex = 0
        for (y in 0 until labMat.rows()) {
            for (x in 0 until labMat.cols()) {
                val clusterIdx = labels.get(labelIndex, 0)[0].toInt()

                // Read each channel individually (works for CV_32S)
                val color = DoubleArray(numChannels)
                for (c in 0 until numChannels) {
                    color[c] = centers.get(clusterIdx, c)[0] // get returns DoubleArray of length 1
                }

                val bgrColor = ByteArray(numChannels)
                for (i in 0 until numChannels) {
                    bgrColor[i] = color[i].toInt().coerceIn(0, 255).toByte()
                }

                clustered.put(y, x, bgrColor)
                labelIndex++
            }
        }
        SaveImageUtil.saveMatAsImage(context, clustered, "03_kmeans_clustered")

        // Convert back to grayscale for contour extraction
        val clusteredBgr = Mat()
        Imgproc.cvtColor(clustered, clusteredBgr, Imgproc.COLOR_Lab2BGR)
        val gray = Mat()
        Imgproc.cvtColor(clusteredBgr, gray, Imgproc.COLOR_BGR2GRAY)
        SaveImageUtil.saveMatAsImage(context, gray, "04_gray")

        // Optional smoothing before morphology
        Imgproc.GaussianBlur(gray, gray, Size(5.0, 5.0), 2.0)

        // Morphological cleanup (close gaps and remove noise)
        val kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, Size(5.0, 5.0))
        val morph = Mat()
        Imgproc.morphologyEx(gray, morph, Imgproc.MORPH_CLOSE, kernel)
        Imgproc.morphologyEx(morph, morph, Imgproc.MORPH_OPEN, kernel)
        SaveImageUtil.saveMatAsImage(context, morph, "05_morph_cleaned")

        // Threshold to binary for contour detection
        val binary = Mat()
        Imgproc.threshold(morph, binary, 0.0, 255.0, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU)
        SaveImageUtil.saveMatAsImage(context, binary, "06_binary")

        // Find contours
        val contours = mutableListOf<MatOfPoint>()
        Imgproc.findContours(binary, contours, Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE)

        val allPolygons = mutableListOf<List<Point>>()

        // Approximate & convert contours to polygons
        for (contour in contours) {
            val contour2f = MatOfPoint2f(*contour.toArray())
            val approxCurve = MatOfPoint2f()
            val epsilon = 0.005 * Imgproc.arcLength(contour2f, true)
            Imgproc.approxPolyDP(contour2f, approxCurve, epsilon, true)

            // Filter out small/noisy areas
            if (Imgproc.contourArea(approxCurve) > 500) {
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

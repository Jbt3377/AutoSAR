package com.example.autosar.util

import android.graphics.Bitmap
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Point
import org.opencv.core.Size
import org.opencv.core.TermCriteria
import org.opencv.imgproc.Imgproc
import kotlin.math.abs

object ImageSegmentationUtil {

    /**
     * Segments a satellite image into color regions and extracts polygons.
     * @param bitmap The input satellite tile bitmap.
     * @param clusterCount Number of color clusters (e.g., 5–8).
     * @return List of polygons (each polygon is a list of LatLng-like points in image coordinates).
     */
    fun extractPolygonsFromImage(bitmap: Bitmap, clusterCount: Int = 6): List<List<Point>> {
        val src = Mat()
        Utils.bitmapToMat(bitmap, src)
        Imgproc.cvtColor(src, src, Imgproc.COLOR_RGBA2RGB)

        // Resize for performance
        val scale = 0.5
        val resized = Mat()
        Imgproc.resize(src, resized, Size(src.width() * scale, src.height() * scale))

        // Prepare for K-means
        val samples = resized.reshape(1, resized.cols() * resized.rows())
        samples.convertTo(samples, CvType.CV_32F)

        // Run K-means clustering
        val criteria = TermCriteria(TermCriteria.EPS + TermCriteria.MAX_ITER, 10, 1.0)
        val labels = Mat()
        val centers = Mat()
        Core.kmeans(samples, clusterCount, labels, criteria, 3, Core.KMEANS_PP_CENTERS, centers)

        // Recolor the clustered image
        val clustered = Mat(resized.size(), resized.type())
        val centersUchar = Mat()
        centers.convertTo(centersUchar, CvType.CV_8UC3) // Explicitly set the type to 3-channel color

        var dataIndex = 0
        for (y in 0 until resized.rows()) {
            for (x in 0 until resized.cols()) {
                val clusterIdx = labels.get(dataIndex, 0)[0].toInt()
                // Get the color data as a DoubleArray
                val colorData = centersUchar.get(clusterIdx, 0)
                // Convert the DoubleArray to a ByteArray for the put method, avoiding the deprecated toByte() call
                val colorByteArray = byteArrayOf(
                    colorData[0].toInt().toByte(),
                    colorData[1].toInt().toByte(),
                    colorData[2].toInt().toByte()
                )
                clustered.put(y, x, colorByteArray)
                dataIndex++
            }
        }

        // Convert to grayscale and threshold
        val gray = Mat()
        Imgproc.cvtColor(clustered, gray, Imgproc.COLOR_RGB2GRAY)
        Imgproc.GaussianBlur(gray, gray, Size(5.0, 5.0), 0.0)
        val thresh = Mat()
        Imgproc.adaptiveThreshold(gray, thresh, 255.0, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
            Imgproc.THRESH_BINARY, 11, 2.0)

        // Find contours (polygons)
        val contours = mutableListOf<MatOfPoint>()
        val hierarchy = Mat()
        Imgproc.findContours(thresh, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE)

        // Approximate and simplify polygons
        val polygons = mutableListOf<List<Point>>()
        for (c in contours) {
            val contour2f = MatOfPoint2f(*c.toArray())
            val approx = MatOfPoint2f()
            Imgproc.approxPolyDP(contour2f, approx, 4.0, true)
            val area = abs(Imgproc.contourArea(approx))
            if (area > 500.0) { // ignore small noise
                polygons.add(approx.toArray().map {
                    Point(it.x / scale, it.y / scale) // rescale to original size
                })
            }
        }

        src.release()
        resized.release()
        clustered.release()
        gray.release()
        thresh.release()

        return polygons
    }
}
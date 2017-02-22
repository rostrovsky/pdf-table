package pdftable;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.opencv.core.Core.bitwise_xor;
import static org.opencv.imgproc.Imgproc.*;
import static pdftable.PdfTableSettings.*;

/**
 * Class responsible for determining table cells bounding boxes.
 * Should be used as static.
 */
public class TableExtractor {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    private TableExtractor() {
    }

    /**
     * Applies series of filters on page image and extracts table cells bounding rectangles.
     * Additionally dumps debug PNG images when pdftable.PdfTableSettings.requestedDebugImages() is true.
     *
     * @param inImage Input image
     * @return List of org.opencv.core.Rect objects representing cell bounding rectangles.
     */
    public static List<Rect> getTableBoundingRectangles(Mat inImage) {
        List<Rect> out = new ArrayList<>();

        if (requestedDebugImages()) {
            Imgcodecs.imwrite(buildDebugFilename("original_grayscaled"), inImage);
        }

        // binary inverted threshold
        Mat bit = binaryInvertedThreshold(inImage);
        if (requestedDebugImages()) {
            Imgcodecs.imwrite(buildDebugFilename("binary_inverted_threshold"), bit);
        }

        // find contours
        List<MatOfPoint> contours = new ArrayList<>();
        if (hasCannyFiltering()) {
            Mat canny = cannyFilter(inImage);
            findContours(canny, contours, new Mat(), RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);
            if (requestedDebugImages()) {
                Imgcodecs.imwrite(buildDebugFilename("canny1"), canny);
            }
        } else {
            findContours(bit, contours, new Mat(), RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);
        }

        // draw contour
        Mat contourMask = bit.clone();
        drawContours(contourMask, contours, -1, new Scalar(255, 255, 255), Core.FILLED);
        if (requestedDebugImages()) {
            Imgcodecs.imwrite(buildDebugFilename("contour_mask"), contourMask);
        }

        // XOR threshold and mask
        Mat xored = new Mat();
        bitwise_xor(bit, contourMask, xored);
        if (requestedDebugImages()) {
            Imgcodecs.imwrite(buildDebugFilename("xored"), xored);
        }

        // find contours #2
        List<MatOfPoint> contours2 = new ArrayList<>();
        if (hasCannyFiltering()) {
            Mat canny2 = cannyFilter(xored);
            findContours(canny2, contours2, new Mat(), RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);
            if (requestedDebugImages()) {
                Imgcodecs.imwrite(buildDebugFilename("canny2"), canny2);
            }
        } else {
            findContours(xored, contours2, new Mat(), RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);
        }

        // draw contour #2
        if (requestedDebugImages()) {
            Mat contourMask2 = inImage.clone();
            drawContours(contourMask2, contours2, -1, new Scalar(255, 255, 255), Core.FILLED);
            Imgcodecs.imwrite(buildDebugFilename("final_contours"), contourMask2);
        }

        // find contours #2 bounding rectangles
        for (int i = 0; i < contours2.size(); i++) {
            MatOfPoint2f approxCurve = new MatOfPoint2f();
            MatOfPoint2f contour2f = new MatOfPoint2f(contours2.get(i).toArray());
            double approxDistance = Imgproc.arcLength(contour2f, true) * getApproxDistScaleFactor();
            Imgproc.approxPolyDP(contour2f, approxCurve, approxDistance, true);
            MatOfPoint points = new MatOfPoint(approxCurve.toArray());
            Rect rect = Imgproc.boundingRect(points);
            out.add(rect);
        }

        Collections.reverse(out);

        if (requestedDebugImages()) {
            int ri = 0;
            for (Rect rect : out) {
                Mat outImage = inImage.clone();
                Point p1 = new Point(rect.x, rect.y);
                Point p2 = new Point(rect.x + rect.width, rect.y + rect.height);
                rectangle(outImage, p1, p2, new Scalar(0, 0, 0, 255), 3);
                Imgcodecs.imwrite(buildDebugFilename(String.format("box_%03d", ri)), outImage);
                ri++;
            }
        }

        return out;
    }

    /**
     * Applies Binary Inverted Threshold (BIT) to Mat image.
     *
     * @param input Input image
     * @return org.opencv.core.Mat image with applied BIT
     */
    private static Mat binaryInvertedThreshold(Mat input) {
        Mat out = new Mat();
        threshold(input, out, getBITThreshold(), getBITMaxval(), THRESH_BINARY_INV);
        return out;
    }

    /**
     * Applies Canny filter to Mat image.
     *
     * @param input Input image
     * @return org.opencv.core.Mat image with applied Canny filter
     */
    private static Mat cannyFilter(Mat input) {
        Mat out = new Mat();
        Canny(input, out, getCannyThreshold1(), getCannyThreshold2(), getCannyApertureSize(), hasCannyL2Gradient());
        return out;
    }

    /**
     * String helper used for constructing debug image output path.
     *
     * @param suffix Image filename suffix
     * @return String representing image path
     */
    private static String buildDebugFilename(String suffix) {
        return getDebugFileOutputDir().resolve(getDebugFilename() + "_" + suffix + ".png").toString();
    }

}

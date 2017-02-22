package pdftable;


import java.nio.file.Path;

public class PdfTableSettings {

    // DPI SETTINGS
    private static final int DEFAULT_PDF_DPI = 72;
    private static int PDF_RENDERING_DPI = 120;

    // CANNY EDGE DETECTION FLAG
    private static boolean CANNY_FILTERING = false;

    // BINARY INVERTED THRESHOLD SETTINGS
    private static double BIT_THRESHOLD = 150;
    private static double BIT_MAXVAL = 255;

    // CANNY FILTER SETTINGS
    private static double CANNY_THRESHOLD_1 = 50;
    private static double CANNY_THRESHOLD_2 = 200;
    private static int CANNY_APERTURE_SIZE = 3;
    private static boolean CANNY_L2_GRADIENT = false;

    // BOUNDING RECT PARAMS
    private static double APPROX_DIST_SCALE_FACTOR = 0.02;

    // DEBUG IMAGES PARAMS
    private static boolean DEBUG_IMAGES = false;
    private static Path DEBUG_FILE_OUTPUT_DIR;
    private static String DEBUG_FILENAME;

    private PdfTableSettings() {
    }

    public static int getDefaultPdfDpi() {
        return DEFAULT_PDF_DPI;
    }

    public static int getPdfRenderingDpi() {
        return PDF_RENDERING_DPI;
    }

    public static void setPdfRenderingDpi(int pdfRenderingDpi) {
        PDF_RENDERING_DPI = pdfRenderingDpi;
    }

    public static double getDpiRatio() {
        return (double) DEFAULT_PDF_DPI / PDF_RENDERING_DPI;
    }

    public static void setCannyFiltering(boolean canny) {
        CANNY_FILTERING = canny;
    }

    public static boolean hasCannyFiltering() {
        return CANNY_FILTERING;
    }

    public static double getBITThreshold() {
        return BIT_THRESHOLD;
    }

    public static void setBitThreshold(double bitThreshold) {
        BIT_THRESHOLD = bitThreshold;
    }

    public static double getBITMaxval() {
        return BIT_MAXVAL;
    }

    public static void setBitMaxval(double bitMaxval) {
        BIT_MAXVAL = bitMaxval;
    }

    public static double getCannyThreshold1() {
        return CANNY_THRESHOLD_1;
    }

    public static void setCannyThreshold1(double cannyThreshold1) {
        CANNY_THRESHOLD_1 = cannyThreshold1;
    }

    public static double getCannyThreshold2() {
        return CANNY_THRESHOLD_2;
    }

    public static void setCannyThreshold2(double cannyThreshold2) {
        CANNY_THRESHOLD_2 = cannyThreshold2;
    }

    public static int getCannyApertureSize() {
        return CANNY_APERTURE_SIZE;
    }

    public static void setCannyApertureSize(int cannyApertureSize) {
        CANNY_APERTURE_SIZE = cannyApertureSize;
    }

    public static boolean hasCannyL2Gradient() {
        return CANNY_L2_GRADIENT;
    }

    public static void setCannyL2Gradient(boolean cannyL2Gradient) {
        CANNY_L2_GRADIENT = cannyL2Gradient;
    }

    public static double getApproxDistScaleFactor() {
        return APPROX_DIST_SCALE_FACTOR;
    }

    public static void setApproxDistScaleFactor(double approxDistScaleFactor) {
        APPROX_DIST_SCALE_FACTOR = approxDistScaleFactor;
    }

    public static boolean requestedDebugImages() {
        return DEBUG_IMAGES;
    }

    public static void setDebugImages(boolean debugImages) {
        DEBUG_IMAGES = debugImages;
    }

    public static Path getDebugFileOutputDir() {
        return DEBUG_FILE_OUTPUT_DIR;
    }

    public static void setDebugFileOutputDir(Path debugFileOutputDir) {
        DEBUG_FILE_OUTPUT_DIR = debugFileOutputDir;
    }

    public static String getDebugFilename() {
        return DEBUG_FILENAME;
    }

    public static void setDebugFileName(String debugFilename) {
        DEBUG_FILENAME = debugFilename;
    }
}

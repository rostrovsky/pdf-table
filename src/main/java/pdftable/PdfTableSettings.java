package pdftable;


import java.nio.file.Path;

/**
 * Image conversion settings.
 */
public class PdfTableSettings {

    public static class PdfTableSettingsBuilder {

        // --------------
        // DEFAULT VALUES
        // --------------

        // DPI SETTINGS
        private static final int DEFAULT_PDF_DPI = 72;
        private int pdfRenderingDpi = 120;

        // CANNY EDGE DETECTION FLAG
        private boolean cannyFiltering = false;

        // BINARY INVERTED THRESHOLD SETTINGS
        private double bitThreshold = 150;
        private double bitMaxVal = 255;

        // CANNY FILTER SETTINGS
        private double cannyThreshold1 = 50;
        private double cannyThreshold2 = 200;
        private int cannyApertureSize = 3;
        private boolean cannyL2Gradient = false;

        // BOUNDING RECT PARAMS
        private double approxDistScaleFactor = 0.02;

        // DEBUG IMAGES PARAMS
        private boolean debugImages = false;
        private Path debugFileOutputDir;
        private String debugFilename;

        public PdfTableSettingsBuilder setPdfRenderingDpi(int pdfRenderingDpi) {
            this.pdfRenderingDpi = pdfRenderingDpi;
            return this;
        }

        public PdfTableSettingsBuilder setCannyFiltering(boolean cannyFiltering) {
            this.cannyFiltering = cannyFiltering;
            return this;
        }

        public PdfTableSettingsBuilder setBitThreshold(double bitThreshold) {
            this.bitThreshold = bitThreshold;
            return this;
        }

        public PdfTableSettingsBuilder setBitMaxVal(double bitMaxVal) {
            this.bitMaxVal = bitMaxVal;
            return this;
        }

        public PdfTableSettingsBuilder setCannyThreshold1(double cannyThreshold1) {
            this.cannyThreshold1 = cannyThreshold1;
            return this;
        }

        public PdfTableSettingsBuilder setCannyThreshold2(double cannyThreshold2) {
            this.cannyThreshold2 = cannyThreshold2;
            return this;
        }

        public PdfTableSettingsBuilder setCannyApertureSize(int cannyApertureSize) {
            this.cannyApertureSize = cannyApertureSize;
            return this;
        }

        public PdfTableSettingsBuilder setCannyL2Gradient(boolean cannyL2Gradient) {
            this.cannyL2Gradient = cannyL2Gradient;
            return this;
        }

        public PdfTableSettingsBuilder setApproxDistScaleFactor(double approxDistScaleFactor) {
            this.approxDistScaleFactor = approxDistScaleFactor;
            return this;
        }

        public PdfTableSettingsBuilder setDebugImages(boolean debugImages) {
            this.debugImages = debugImages;
            return this;
        }

        public PdfTableSettingsBuilder setDebugFileOutputDir(Path debugFileOutputDir) {
            this.debugFileOutputDir = debugFileOutputDir;
            return this;
        }

        public PdfTableSettingsBuilder setDebugFilename(String debugFilename) {
            this.debugFilename = debugFilename;
            return this;
        }

        public PdfTableSettings build() {
            return new PdfTableSettings(this);
        }
    }

    // DPI SETTINGS
    private int defaultPdfDpi;
    private int pdfRenderingDpi;

    // CANNY EDGE DETECTION FLAG
    private boolean cannyFiltering;

    // BINARY INVERTED THRESHOLD SETTINGS
    private double bitThreshold;
    private double bitMaxVal;

    // CANNY FILTER SETTINGS
    private double cannyThreshold1;
    private double cannyThreshold2;
    private int cannyApertureSize;
    private boolean cannyL2Gradient;

    // BOUNDING RECT PARAMS
    private double approxDistScaleFactor;

    // DEBUG IMAGES PARAMS
    private boolean debugImages;
    private Path debugFileOutputDir;
    private String debugFilename;

    private PdfTableSettings(PdfTableSettingsBuilder builder) {
        this.defaultPdfDpi = PdfTableSettingsBuilder.DEFAULT_PDF_DPI;
        this.pdfRenderingDpi = builder.pdfRenderingDpi;
        this.cannyFiltering = builder.cannyFiltering;
        this.bitThreshold = builder.bitThreshold;
        this.bitMaxVal = builder.bitMaxVal;
        this.cannyThreshold1 = builder.cannyThreshold1;
        this.cannyThreshold2 = builder.cannyThreshold2;
        this.cannyApertureSize = builder.cannyApertureSize;
        this.cannyL2Gradient = builder.cannyL2Gradient;
        this.approxDistScaleFactor = builder.approxDistScaleFactor;
        this.debugImages = builder.debugImages;
        this.debugFileOutputDir = builder.debugFileOutputDir;
        this.debugFilename = builder.debugFilename;
    }

    public PdfTableSettings() {
        this(new PdfTableSettingsBuilder());
    }

    public static PdfTableSettingsBuilder getBuilder() {
        return new PdfTableSettingsBuilder();
    }

    public int getDefaultPdfDpi() {
        return defaultPdfDpi;
    }

    public int getPdfRenderingDpi() {
        return pdfRenderingDpi;
    }

    public boolean hasCannyFiltering() {
        return cannyFiltering;
    }

    public double getBitThreshold() {
        return bitThreshold;
    }

    public double getBitMaxVal() {
        return bitMaxVal;
    }

    public double getCannyThreshold1() {
        return cannyThreshold1;
    }

    public double getCannyThreshold2() {
        return cannyThreshold2;
    }

    public int getCannyApertureSize() {
        return cannyApertureSize;
    }

    public boolean hasCannyL2Gradient() {
        return cannyL2Gradient;
    }

    public double getApproxDistScaleFactor() {
        return approxDistScaleFactor;
    }

    public boolean hasDebugImages() {
        return debugImages;
    }

    public Path getDebugFileOutputDir() {
        return debugFileOutputDir;
    }

    public String getDebugFilename() {
        return debugFilename;
    }

    public double getDpiRatio() {
        return (double) defaultPdfDpi / pdfRenderingDpi;
    }
}

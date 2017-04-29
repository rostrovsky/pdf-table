package pdftable;


import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;
import org.opencv.core.Core;
import org.opencv.core.Rect;
import pdftable.models.ParsedTablePage;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static pdftable.Utils.bufferedImage2GrayscaleMat;


public class PdfTableReader {

    private TableExtractor extractor;
    private PdfTableSettings settings;

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public PdfTableReader(PdfTableSettings settings) {
        this.settings = settings;
        this.extractor = new TableExtractor(settings);
    }

    public PdfTableReader() {
        this(new PdfTableSettings());
    }

    /**
     * Renders PDF page with DPI specified in settings and saves it in specified directory.
     *
     * @param renderer  PDF renderer instance
     * @param page      page number
     * @param outputDir output directory
     * @throws IOException
     */
    private void savePdfPageAsPNG(PDFRenderer renderer, int page, Path outputDir) throws IOException {
        BufferedImage bim;
        synchronized (this) {
            bim = renderer.renderImageWithDPI(page, settings.getPdfRenderingDpi(), ImageType.RGB);
        }
        Path outPath = outputDir.resolve(Paths.get("page_" + (page + 1) + ".png"));
        ImageIOUtil.writeImage(bim, outPath.toString(), settings.getPdfRenderingDpi());

    }

    /**
     * Renders PDF pages range with DPI specified in settings and saves images in specified directory.
     *
     * @param document  PDF document instance
     * @param startPage first page in range (first page == 1)
     * @param endPage   last page in range
     * @param outputDir output directory
     * @throws IOException
     */
    public void savePdfPagesAsPNG(PDDocument document, int startPage, int endPage, Path outputDir) throws IOException {
        PDFRenderer pdfRenderer = new PDFRenderer(document);
        for (int page = startPage - 1; page < endPage; ++page) {
            savePdfPageAsPNG(pdfRenderer, page, outputDir);
        }
    }

    /**
     * Renders single PDF page with DPI specified in settings and saves image in specified directory.
     *
     * @param document  PDF document instance
     * @param page      page number (first page == 1)
     * @param outputDir output directory
     * @throws IOException
     */
    public void savePdfPageAsPNG(PDDocument document, int page, Path outputDir) throws IOException {
        savePdfPagesAsPNG(document, page, page, outputDir);
    }

    /**
     * Parses single PDF page and returns list of rows containing cell texts.
     *
     * @param bi     PDF page in image format
     * @param pdPage PDF page in PDPage format
     * @return parsed page
     * @throws IOException
     */
    private ParsedTablePage parsePdfTablePage(BufferedImage bi, PDPage pdPage, int pageNumber) throws IOException {
        List<Rect> rectangles = extractor.getTableBoundingRectangles(bufferedImage2GrayscaleMat(bi));
        return parsePageByRectangles(pdPage, rectangles, pageNumber);
    }

    /**
     * Parses range of PDF pages and returns list of lists of rows containing cell texts.
     *
     * @param document  PDF document instance
     * @param startPage first page in range to parse (first page == 1)
     * @param endPage   last page in range
     * @return List of pages
     * @throws IOException
     */
    public List<ParsedTablePage> parsePdfTablePages(PDDocument document, int startPage, int endPage) throws IOException {
        List<ParsedTablePage> out = new ArrayList<>();
        PDFRenderer renderer = new PDFRenderer(document);
        for (int page = startPage - 1; page < endPage; ++page) {
            BufferedImage bi;
            synchronized (this) {
                bi = renderer.renderImageWithDPI(page, settings.getPdfRenderingDpi(), ImageType.RGB);
            }
            ParsedTablePage parsedTablePage = parsePdfTablePage(bi, document.getPage(page), page + 1);
            out.add(parsedTablePage);
        }
        return out;
    }

    /**
     * Parses single PDF page and returns list of rows containing cell texts.
     *
     * @param document PDF document instance
     * @param page     number of page to parse (first page == 1)
     * @return parsed page
     * @throws IOException
     */
    public ParsedTablePage parsePdfTablePage(PDDocument document, int page) throws IOException {
        return parsePdfTablePages(document, page, page).get(0);
    }

    /**
     * Saves debug images of PDF pages from specified range and saves them in specified directory.
     *
     * @param document  PDF document instance
     * @param startPage first page in range to process (first page == 1)
     * @param endPage   last page in range
     * @param outputDir destination directory
     * @throws IOException
     */
    public void savePdfTablePagesDebugImages(PDDocument document, int startPage, int endPage, Path outputDir) throws IOException {
        TableExtractor debugExtractor = new TableExtractor(settings);
        PDFRenderer renderer = new PDFRenderer(document);
        for (int page = startPage - 1; page < endPage; ++page) {
            PdfTableSettings debugSettings = PdfTableSettings.getBuilder()
                    .setDebugImages(true)
                    .setDebugFileOutputDir(outputDir)
                    .setDebugFilename("page_" + (page + 1))
                    .build();
            debugExtractor.setSettings(debugSettings);
            BufferedImage bi;
            synchronized (this) {
                bi = renderer.renderImageWithDPI(page, settings.getPdfRenderingDpi(), ImageType.RGB);
            }
            debugExtractor.getTableBoundingRectangles(bufferedImage2GrayscaleMat(bi));
        }
    }

    /**
     * Saves debug images of PDF page and saves them in specified directory.
     *
     * @param document  PDF document instance
     * @param page      page to process (first page == 1)
     * @param outputDir destination directory
     * @throws IOException
     */
    public void savePdfTablePageDebugImage(PDDocument document, int page, Path outputDir) throws IOException {
        savePdfTablePagesDebugImages(document, page, page, outputDir);
    }

    /**
     * Parses PDF page cell by cell using rectangles obtained from TableExtractor.
     *
     * @param page       PDF page
     * @param rectangles list of OpenCV rectangles recognized by TableExtractor
     * @return parsed page
     * @throws IOException
     */
    private ParsedTablePage parsePageByRectangles(PDPage page, List<Rect> rectangles, int pageNumber) throws IOException {
        List<List<Rect>> sortedRects = groupRectanglesByRow(rectangles);
        ParsedTablePage out = new ParsedTablePage(pageNumber);

        PDFTextStripperByArea stripper = new PDFTextStripperByArea();
        stripper.setSortByPosition(true);

        int iRow = 0;
        int iCol = 0;
        for (List<Rect> row : sortedRects) {
            for (Rect col : row) {
                Rectangle r = new Rectangle(
                        (int) (col.x * settings.getDpiRatio()),
                        (int) (col.y * settings.getDpiRatio()),
                        (int) (col.width * settings.getDpiRatio()),
                        (int) (col.height * settings.getDpiRatio())
                );
                stripper.addRegion(getRegionId(iRow, iCol), r);
                iCol++;
            }
            iRow++;
            iCol = 0;
        }

        stripper.extractRegions(page);

        iRow = 0;
        iCol = 0;
        for (List<Rect> row : sortedRects) {
            List<String> rowText = new ArrayList<>();
            for (Rect col : row) {
                String cellText = stripper.getTextForRegion(getRegionId(iRow, iCol));
                rowText.add(cellText);
                iCol++;
            }
            out.addRow(rowText);
            iRow++;
            iCol = 0;
        }

        return out;
    }

    /**
     * Groups rectangles by y coordinate effectively grouping them into rows.
     *
     * @param rectangles list of OpenCV Rectangles
     * @return list of Rectangle lists representing table rows.
     */
    private List<List<Rect>> groupRectanglesByRow(List<Rect> rectangles) {
        List<List<Rect>> out = new ArrayList<>();
        List<Integer> rowsCoords = rectangles.stream().map(r -> r.y).distinct().collect(Collectors.toList());
        for (int rowCoords : rowsCoords) {
            List<Rect> cols = rectangles.stream().filter(r -> r.y == rowCoords).collect(Collectors.toList());
            out.add(cols);
        }
        return out;
    }

    /**
     * Static helper for creating row/column markers.
     *
     * @param row table row
     * @param col table column
     * @return marker with row & column number
     */
    private static String getRegionId(int row, int col) {
        return String.format("r%dc%d", row, col);
    }

}

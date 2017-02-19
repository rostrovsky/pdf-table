package pdftable;


import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;
import org.opencv.core.Core;
import org.opencv.core.Rect;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static pdftable.PdfTableSettings.*;
import static pdftable.Utils.bufferedImage2GrayscaleMat;


public class PdfTableReader {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    private PdfTableReader() {}

    private static void savePdfPageAsPNG(PDFRenderer renderer, int page, Path outputDir) throws IOException {
        BufferedImage bim = renderer.renderImageWithDPI(page, getPdfRenderingDpi(), ImageType.RGB);
        Path outPath = outputDir.resolve(Paths.get("page_" + (page + 1) + ".png"));
        ImageIOUtil.writeImage(bim, outPath.toString(), getPdfRenderingDpi());
    }

    public static void savePdfPagesAsPNG(PDDocument document, int startPage, int endPage, Path outputDir) throws IOException {
        PDFRenderer pdfRenderer = new PDFRenderer(document);
        for (int page = startPage - 1; page < endPage; ++page) {
            savePdfPageAsPNG(pdfRenderer, page, outputDir);
        }
    }

    private static List<List<String>> parsePdfTablePage(BufferedImage bi, PDPage pdPage) throws IOException {
        List<Rect> rectangles = TableExtractor.getTableBoundingRectangles(bufferedImage2GrayscaleMat(bi));
        return parsePageByRectangles(pdPage, rectangles);
    }

    public static List<List<List<String>>> parsePdfTablePages(PDDocument document, int startPage, int endPage) throws IOException {
        List<List<List<String>>> out = new ArrayList<>();
        PDFRenderer renderer = new PDFRenderer(document);
        for (int page = startPage - 1; page < endPage; ++page) {
            BufferedImage bi = renderer.renderImageWithDPI(page, getPdfRenderingDpi(), ImageType.RGB);
            out.add(parsePdfTablePage(bi, document.getPage(page)));
        }
        return out;
    }

    public static void savePdfTablePagesDebugImages(PDDocument document, int startPage, int endPage, Path outputDir) throws IOException {
        setDebugImages(true);
        setDebugFileOutputDir(outputDir);
        PDFRenderer renderer = new PDFRenderer(document);
        for (int page = startPage - 1; page < endPage; ++page) {
            setDebugFileName("page_" + (page + 1));
            BufferedImage bi = renderer.renderImageWithDPI(page, getPdfRenderingDpi(), ImageType.RGB);
            TableExtractor.getTableBoundingRectangles(bufferedImage2GrayscaleMat(bi));
        }
        setDebugImages(false);
    }

    public static List<List<String>> parsePageByRectangles(PDPage page, List<Rect> rectangles) throws IOException {
        List<List<Rect>> sortedRects = groupRectanglesByRow(rectangles);
        List<List<String>> out = new ArrayList<>();

        PDFTextStripperByArea stripper = new PDFTextStripperByArea();
        stripper.setSortByPosition(true);

        int iRow = 0;
        int iCol = 0;
        for (List<Rect> row : sortedRects) {
            for (Rect col : row) {
                Rectangle r = new Rectangle(
                        (int) (col.x * getDpiRatio()),
                        (int) (col.y * getDpiRatio()),
                        (int) (col.width * getDpiRatio()),
                        (int) (col.height * getDpiRatio())
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
            out.add(rowText);
            iRow++;
            iCol = 0;
        }

        return out;
    }

    private static String getRegionId(int row, int col) {
        return String.format("r%dc%d", row, col);
    }

    public static List<List<Rect>> groupRectanglesByRow(List<Rect> rectangles) {
        List<List<Rect>> out = new ArrayList<>();
        List<Integer> rowsCoords = rectangles.stream().map(r -> r.y).distinct().collect(Collectors.toList());
        for (int rowCoords : rowsCoords) {
            List<Rect> cols = rectangles.stream().filter(r -> r.y == rowCoords).collect(Collectors.toList());
            out.add(cols);
        }
        return out;
    }

}

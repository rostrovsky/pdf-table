package pdftable;


import org.apache.pdfbox.pdmodel.PDDocument;
import org.testng.Assert;
import org.testng.TestException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pdftable.models.ParsedTablePage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class PdfTableReaderTest {

    private static final Path TEST_OUT_PATH = Paths.get("C:", "pdf_tests");
    private static final String TEST_FILENAME = "test_tables.pdf";
    private static PDDocument PDFdoc;
    private static final int THREAD_COUNT = 8;
    private static final int PAGE_CYCLE = 4;

    @BeforeMethod
    private void setUp() {
        PDFdoc = getTestPDF();
    }

    @AfterMethod
    private void tearDown() {
        if (PDFdoc != null) {
            try {
                PDFdoc.close();
            } catch (IOException ioe) {
                throw new TestException(ioe);
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    private PDDocument getTestPDF() {
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource(TEST_FILENAME).getFile());
            return PDDocument.load(file);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getCause());
        }
    }

    @Test
    public void savePdfPagesAsPNG() throws IOException {
        PdfTableReader reader = new PdfTableReader();
        reader.savePdfPagesAsPNG(PDFdoc, 1, 3, TEST_OUT_PATH);
    }

    @Test
    public void savePdfPageAsPNG() throws IOException {
        PdfTableReader reader = new PdfTableReader();
        reader.savePdfPageAsPNG(PDFdoc, 4, TEST_OUT_PATH);
    }

    @Test
    public void savePdfDebugImages() throws IOException {
        PdfTableReader reader = new PdfTableReader();
        reader.savePdfTablePagesDebugImages(PDFdoc, 1, 3, TEST_OUT_PATH);
    }

    @Test
    public void savePdfDebugImage() throws IOException {
        PdfTableReader reader = new PdfTableReader();
        reader.savePdfTablePageDebugImage(PDFdoc, 4, TEST_OUT_PATH);
    }

    @Test
    public void singleThreadedSavePdfPageAsPNG() throws IOException {
        long start = System.currentTimeMillis();
        PdfTableReader reader = new PdfTableReader();
        reader.savePdfPagesAsPNG(PDFdoc, 1, PDFdoc.getNumberOfPages(), TEST_OUT_PATH);
        long end = System.currentTimeMillis();
        System.out.println("save page image - Single thread: " + (end - start) / 1000.0);
    }

    @Test
    public void multiThreadedSavePdfPageAsPNG() throws IOException {
        long start = System.currentTimeMillis();
        PdfTableReader reader = new PdfTableReader();
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);

        List<Future<Boolean>> futures = new ArrayList<>();
        for (final int pageNum : IntStream.rangeClosed(1, PDFdoc.getNumberOfPages()).toArray()) {
            Callable<Boolean> callable = () -> {
                reader.savePdfPageAsPNG(PDFdoc, pageNum, TEST_OUT_PATH);
                return true;
            };
            futures.add(executor.submit(callable));
        }

        try {
            for (Future<Boolean> f : futures) {
                f.get();
            }
        } catch (Exception e) {
            throw new TestException(e);
        }

        long end = System.currentTimeMillis();
        System.out.println("save page image - multi thread: " + (end - start) / 1000.0);
    }

    @Test
    public void singleThreadedSavePdfTablePageDebugImage() throws IOException {
        long start = System.currentTimeMillis();
        PdfTableReader reader = new PdfTableReader();
        reader.savePdfTablePagesDebugImages(PDFdoc, 1, PDFdoc.getNumberOfPages(), TEST_OUT_PATH);
        long end = System.currentTimeMillis();
        System.out.println("save debug images - single thread: " + (end - start) / 1000.0);
    }

    @Test
    public void multiThreadedSavePdfTablePageDebugImage() throws IOException {
        long start = System.currentTimeMillis();
        PdfTableReader reader = new PdfTableReader();
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);

        List<Future<Boolean>> futures = new ArrayList<>();
        for (final int pageNum : IntStream.rangeClosed(1, PDFdoc.getNumberOfPages()).toArray()) {
            Callable<Boolean> callable = () -> {
                reader.savePdfTablePageDebugImage(PDFdoc, pageNum, TEST_OUT_PATH);
                return true;
            };
            futures.add(executor.submit(callable));
        }

        try {
            for (Future<Boolean> f : futures) {
                f.get();
            }
        } catch (Exception e) {
            throw new TestException(e);
        }

        long end = System.currentTimeMillis();
        System.out.println("save debug images - multi thread: " + (end - start) / 1000.0);
    }

    @Test
    public void singleThreadedParsePdfTablePages() throws IOException {
        long start = System.currentTimeMillis();
        PdfTableReader reader = new PdfTableReader();
        List<ParsedTablePage> parsed = reader.parsePdfTablePages(PDFdoc, 1, PDFdoc.getNumberOfPages());
        long end = System.currentTimeMillis();
        System.out.println("parse pages - single thread: " + (end - start) / 1000.0);
        validatePdfContent(parsed);
    }

    @Test
    public void multiThreadedParsePdfTablePages() throws IOException {
        long start = System.currentTimeMillis();
        PdfTableReader reader = new PdfTableReader();
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);

        List<Future<ParsedTablePage>> futures = new ArrayList<>();
        for (final int pageNum : IntStream.rangeClosed(1, PDFdoc.getNumberOfPages()).toArray()) {
            Callable<ParsedTablePage> callable = () -> {
                ParsedTablePage page = reader.parsePdfTablePage(PDFdoc, pageNum);
                return page;
            };
            futures.add(executor.submit(callable));
        }

        List<ParsedTablePage> parsedPages = new ArrayList<>(PDFdoc.getNumberOfPages());
        try {
            for (Future<ParsedTablePage> f : futures) {
                ParsedTablePage page = f.get();
                parsedPages.add(page.getPageNum() - 1, page);
            }
        } catch (Exception e) {
            throw new TestException(e);
        }

        long end = System.currentTimeMillis();
        System.out.println("parse pages - multi thread: " + (end - start) / 1000.0);

        List<ParsedTablePage> sortedParsedPages = parsedPages.stream()
                .sorted((p1, p2) -> Integer.compare(p1.getPageNum(), p2.getPageNum())).collect(Collectors.toList());

        validatePdfContent(sortedParsedPages);
    }

    private static String normalizeWhitespaces(String input) {
        return input.replaceAll("[\\s\\u00A0]+", " ").trim();
    }

    private static void validatePdfContent(List<ParsedTablePage> parsedPdf) {

        // --------------
        // PAGE 1, 5 etc.
        // --------------

        for (int i : IntStream.iterate(0, x -> x + PAGE_CYCLE).limit(PDFdoc.getNumberOfPages() / PAGE_CYCLE).toArray()) {
            Assert.assertEquals(normalizeWhitespaces(parsedPdf.get(i).getRow(0).getCell(0)), "Heading 1");

            Assert.assertEquals(normalizeWhitespaces(parsedPdf.get(i).getRow(1).getCell(0)), "First");
            Assert.assertEquals(normalizeWhitespaces(parsedPdf.get(i).getRow(1).getCell(2)), "Third");
            Assert.assertEquals(normalizeWhitespaces(parsedPdf.get(i).getRow(1).getCell(5)), "Sixth");

            Assert.assertEquals(normalizeWhitespaces(parsedPdf.get(i).getRow(4).getCell(0)), "Sed ut perspiciatis unde omnis iste natus " +
                    "error sit voluptatem accusantium doloremque laudantium, totam rem aperiam, eaque ipsa quae ab illo " +
                    "inventore veritatis et quasi architecto beatae vitae dicta sunt explicabo.");
            Assert.assertEquals(normalizeWhitespaces(parsedPdf.get(i).getRow(4).getCell(1)), "Sed");

            Assert.assertEquals(normalizeWhitespaces(parsedPdf.get(i).getRow(5).getCell(0)), "But");
            Assert.assertEquals(normalizeWhitespaces(parsedPdf.get(i).getRow(5).getCell(1)), "But I must explain to you how all this " +
                    "mistaken idea of denouncing pleasure and praising pain was born and I will give you a complete account " +
                    "of the system, and expound the actual teachings of the great explorer of the truth, the masterbuilder " +
                    "of human happiness");

            Assert.assertEquals(normalizeWhitespaces(parsedPdf.get(i).getRow(6).getCell(0)), "Joined 1");
            Assert.assertEquals(normalizeWhitespaces(parsedPdf.get(i).getRow(6).getCell(1)), "Rest 1");
            Assert.assertEquals(normalizeWhitespaces(parsedPdf.get(i).getRow(6).getCell(2)), "Joined 2 Joined 2");

            Assert.assertEquals(normalizeWhitespaces(parsedPdf.get(i).getRow(9).getCell(0)), "AA");
            Assert.assertEquals(normalizeWhitespaces(parsedPdf.get(i).getRow(9).getCell(1)), "BB");
            Assert.assertEquals(normalizeWhitespaces(parsedPdf.get(i).getRow(9).getCell(2)), "CC");

            Assert.assertEquals(normalizeWhitespaces(parsedPdf.get(i).getRow(10).getCell(0)), "Joined 4 Joined 4 Joined 4 Joined 4");
            Assert.assertEquals(normalizeWhitespaces(parsedPdf.get(i).getRow(10).getCell(1)), "Subheading 1");

            Assert.assertEquals(normalizeWhitespaces(parsedPdf.get(i).getRow(11).getCell(0)), "X");
            Assert.assertEquals(normalizeWhitespaces(parsedPdf.get(i).getRow(11).getCell(1)), "XX");

            Assert.assertEquals(normalizeWhitespaces(parsedPdf.get(i).getRow(12).getCell(0)), "Y");
            Assert.assertEquals(normalizeWhitespaces(parsedPdf.get(i).getRow(12).getCell(1)), "YY");

            Assert.assertEquals(normalizeWhitespaces(parsedPdf.get(i).getRow(13).getCell(0)), "Z");
            Assert.assertEquals(normalizeWhitespaces(parsedPdf.get(i).getRow(13).getCell(1)), "ZZ");
            Assert.assertEquals(normalizeWhitespaces(parsedPdf.get(i).getRow(13).getCell(2)), "ZZZ");
        }

        // --------------
        // PAGE 2, 6 etc.
        // --------------

        for (int i : IntStream.iterate(1, x -> x + PAGE_CYCLE).limit(PDFdoc.getNumberOfPages() / PAGE_CYCLE).toArray()) {
            Assert.assertEquals(normalizeWhitespaces(parsedPdf.get(i).getRow(0).getCell(0)), "Heading 1");

            Assert.assertEquals(normalizeWhitespaces(parsedPdf.get(i).getRow(1).getCell(0)), "First");
            Assert.assertEquals(normalizeWhitespaces(parsedPdf.get(i).getRow(1).getCell(2)), "Third");
            Assert.assertEquals(normalizeWhitespaces(parsedPdf.get(i).getRow(1).getCell(5)), "Sixth");

            Assert.assertEquals(normalizeWhitespaces(parsedPdf.get(i).getRow(4).getCell(0)), "Sed ut perspiciatis unde omnis iste natus " +
                    "error sit voluptatem accusantium doloremque laudantium, totam rem aperiam, eaque ipsa quae ab illo " +
                    "inventore veritatis et quasi architecto beatae vitae dicta sunt explicabo.");
            Assert.assertEquals(normalizeWhitespaces(parsedPdf.get(i).getRow(4).getCell(1)), "Sed");

            Assert.assertEquals(normalizeWhitespaces(parsedPdf.get(i).getRow(5).getCell(0)), "But");
            Assert.assertEquals(normalizeWhitespaces(parsedPdf.get(i).getRow(5).getCell(1)), "But I must explain to you how all this " +
                    "mistaken idea of denouncing pleasure and praising pain was born and I will give you a complete account " +
                    "of the system, and expound the actual teachings of the great explorer of the truth, the masterbuilder " +
                    "of human happiness");

            Assert.assertEquals(normalizeWhitespaces(parsedPdf.get(i).getRow(6).getCell(0)), "Joined 1");
            Assert.assertEquals(normalizeWhitespaces(parsedPdf.get(i).getRow(6).getCell(1)), "Rest 1");
            Assert.assertEquals(normalizeWhitespaces(parsedPdf.get(i).getRow(6).getCell(2)), "Joined 2 Joined 2");

            Assert.assertEquals(normalizeWhitespaces(parsedPdf.get(i).getRow(9).getCell(0)), "AA");
            Assert.assertEquals(normalizeWhitespaces(parsedPdf.get(i).getRow(9).getCell(1)), "BB");
            Assert.assertEquals(normalizeWhitespaces(parsedPdf.get(i).getRow(9).getCell(2)), "CC");

            Assert.assertEquals(normalizeWhitespaces(parsedPdf.get(i).getRow(10).getCell(0)), "Joined 4 Joined 4 Joined 4 Joined 4");
            Assert.assertEquals(normalizeWhitespaces(parsedPdf.get(i).getRow(10).getCell(1)), "Subheading 1");

            Assert.assertEquals(normalizeWhitespaces(parsedPdf.get(i).getRow(11).getCell(0)), "X");
            Assert.assertEquals(normalizeWhitespaces(parsedPdf.get(i).getRow(11).getCell(1)), "XX");

            Assert.assertEquals(normalizeWhitespaces(parsedPdf.get(i).getRow(12).getCell(0)), "Y");
            Assert.assertEquals(normalizeWhitespaces(parsedPdf.get(i).getRow(12).getCell(1)), "YY");

            Assert.assertEquals(normalizeWhitespaces(parsedPdf.get(i).getRow(13).getCell(0)), "Z");
            Assert.assertEquals(normalizeWhitespaces(parsedPdf.get(i).getRow(13).getCell(1)), "ZZ");
            Assert.assertEquals(normalizeWhitespaces(parsedPdf.get(i).getRow(13).getCell(2)), "ZZZ");
        }

        for (int i : IntStream.iterate(2, x -> x + PAGE_CYCLE).limit(PDFdoc.getNumberOfPages() / PAGE_CYCLE).toArray()) {
            Assert.assertEquals(normalizeWhitespaces(parsedPdf.get(i).getRow(0).getCell(0)), "A");
            Assert.assertEquals(normalizeWhitespaces(parsedPdf.get(i).getRow(0).getCell(4)), "E");

            Assert.assertEquals(normalizeWhitespaces(parsedPdf.get(i).getRow(1).getCell(0)), "0.01");
            Assert.assertEquals(normalizeWhitespaces(parsedPdf.get(i).getRow(1).getCell(4)), "0.05");

            Assert.assertEquals(normalizeWhitespaces(parsedPdf.get(i).getRow(37).getCell(0)), "0.37");
            Assert.assertEquals(normalizeWhitespaces(parsedPdf.get(i).getRow(37).getCell(2)), "0.111");
            Assert.assertEquals(normalizeWhitespaces(parsedPdf.get(i).getRow(37).getCell(4)), "0.185");
        }

        for (int i : IntStream.iterate(3, x -> x + PAGE_CYCLE).limit(PDFdoc.getNumberOfPages() / PAGE_CYCLE).toArray()) {
            Assert.assertEquals(normalizeWhitespaces(parsedPdf.get(i).getRow(0).getCell(0)), "Table 1 Heading");

            Assert.assertEquals(normalizeWhitespaces(parsedPdf.get(i).getRow(1).getCell(0)), "AAA");
            Assert.assertEquals(normalizeWhitespaces(parsedPdf.get(i).getRow(1).getCell(1)), "111");

            Assert.assertEquals(normalizeWhitespaces(parsedPdf.get(i).getRow(5).getCell(0)), "GGG");
            Assert.assertEquals(normalizeWhitespaces(parsedPdf.get(i).getRow(5).getCell(1)), "444");

            Assert.assertEquals(normalizeWhitespaces(parsedPdf.get(i).getRow(9).getCell(0)), "Table 3 Heading 2");
            Assert.assertEquals(normalizeWhitespaces(parsedPdf.get(i).getRow(11).getCell(0)), "000");
            Assert.assertEquals(normalizeWhitespaces(parsedPdf.get(i).getRow(11).getCell(1)), "QQQ QQQ QQQ");
        }
    }

}

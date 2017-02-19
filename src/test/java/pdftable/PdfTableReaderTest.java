package pdftable;


import org.apache.pdfbox.pdmodel.PDDocument;
import org.testng.TestException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PdfTableReaderTest {

    private static final Path TEST_OUT_PATH = Paths.get("C:", "pdf_tests");
    private static final String TEST_FILENAME = "test_tables.pdf";
    private static PDDocument PDFdoc;

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
    public void savePdfPageAsPNG() throws IOException {
        PdfTableReader.savePdfPagesAsPNG(PDFdoc, 1, 3, TEST_OUT_PATH);
    }

    @Test
    public void savePdfDebugImages() throws IOException {
        PdfTableReader.savePdfTablePagesDebugImages(PDFdoc, 1, 3, TEST_OUT_PATH);
    }

}

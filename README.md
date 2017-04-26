# pdf-table
Java utility for parsing PDF tabular data using Apache PDFBox and OpenCV.

## Prerequisites

### JDK
JAVA 8 is required.

### External dependencies
pdf-table requires compiled *OpenCV 3.x.x* to work properly:
1. Download latest OpenCV release from http://opencv.org/releases.html
2. Unpack it and add to your system PATH

## Usage

### Parsing PDFs
what is parsing etc.

#### single-threaded example
```java
class SingleThreadParser {
    public static void main(String[] args) throws IOException {
        PDDocument pdfDoc = PDDocument.load(new File("some.pdf"));
        PdfTableReader reader = new PdfTableReader();
        List<ParsedTablePage> parsed = reader.parsePdfTablePages(pdfDoc, 1, pdfDoc.getNumberOfPages());
    }
}
```

#### multi-threaded example
```java
class MultiThreadParser {
    public static void main(String[] args) throws IOException {
        final int THREAD_COUNT = 8;
        PDDocument pdfDoc = PDDocument.load(new File("some.pdf"));
        PdfTableReader reader = new PdfTableReader();

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        List<Future<ParsedTablePage>> futures = new ArrayList<>();
        for (final int pageNum : IntStream.rangeClosed(1, pdfDoc.getNumberOfPages()).toArray()) {
            Callable<ParsedTablePage> callable = () -> {
                ParsedTablePage page = reader.parsePdfTablePage(pdfDoc, pageNum);
                page.setPageNum(pageNum);
                return page;
            };
            futures.add(executor.submit(callable));
        }

        List<ParsedTablePage> unsortedParsedPages = new ArrayList<>(pdfDoc.getNumberOfPages());
        try {
            for (Future<ParsedTablePage> f : futures) {
                ParsedTablePage page = f.get();
                unsortedParsedPages.add(page.getPageNum() - 1, page);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        List<ParsedTablePage> sortedParsedPages = unsortedParsedPages.stream()
                .sorted((p1, p2) -> Integer.compare(p1.getPageNum(), p2.getPageNum())).collect(Collectors.toList());
    }
}
```

### Saving PDF pages as PNG images
what is saving etc.

#### single-threaded example
```java
class SingleThreadPNGDump {
    public static void main(String[] args) throws IOException {
        PDDocument pdfDoc = PDDocument.load(new File("some.pdf"));
        Path outputPath = Paths.get("C:", "some_directory");
        PdfTableReader reader = new PdfTableReader();
        reader.savePdfPagesAsPNG(pdfDoc, 1, pdfDoc.getNumberOfPages(), outputPath);
    }
}
```

#### multi-threaded example
```java
class MultiThreadPNGDump {
    public static void main(String[] args) throws IOException {
        final int THREAD_COUNT = 8;
        Path outputPath = Paths.get("C:", "some_directory");
        PDDocument pdfDoc = PDDocument.load(new File("some.pdf"));
        PdfTableReader reader = new PdfTableReader();

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        List<Future<Boolean>> futures = new ArrayList<>();
        for (final int pageNum : IntStream.rangeClosed(1, pdfDoc.getNumberOfPages()).toArray()) {
            Callable<Boolean> callable = () -> {
                reader.savePdfPageAsPNG(pdfDoc, pageNum, outputPath);
                return true;
            };
            futures.add(executor.submit(callable));
        }

        try {
            for (Future<Boolean> f : futures) {
                f.get();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
```

### Saving debug PNG images
what is saving debug images etc.

#### single-threaded example
```java
class SingleThreadDebugImgsDump {
    public static void main(String[] args) throws IOException {
        PDDocument pdfDoc = PDDocument.load(new File("some.pdf"));
        Path outputPath = Paths.get("C:", "some_directory");
        PdfTableReader reader = new PdfTableReader();
        reader.savePdfTablePagesDebugImages(pdfDoc, 1, pdfDoc.getNumberOfPages(), outputPath);
    }
}
```
#### multi-threaded example
```java
class MultiThreadDebugImgsDump {
    public static void main(String[] args) throws IOException {
        final int THREAD_COUNT = 8;
        Path outputPath = Paths.get("C:", "some_directory");
        PDDocument pdfDoc = PDDocument.load(new File("some.pdf"));
        PdfTableReader reader = new PdfTableReader();

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        List<Future<Boolean>> futures = new ArrayList<>();
        for (final int pageNum : IntStream.rangeClosed(1, pdfDoc.getNumberOfPages()).toArray()) {
            Callable<Boolean> callable = () -> {
                reader.savePdfTablePagesDebugImage(pdfDoc, pageNum, outputPath);
                return true;
            };
            futures.add(executor.submit(callable));
        }

        try {
            for (Future<Boolean> f : futures) {
                f.get();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
```

### Parsing settings
about PdfTableSettings...

### Output format
about output format...

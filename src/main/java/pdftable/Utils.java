package pdftable;

import org.apache.pdfbox.io.IOUtils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Static utilities used for converting between image formats.
 */
public class Utils {

    private Utils() {
    }

    /**
     * Converts InputStream to OpenCV Mat
     *
     * @param stream Input stream
     * @param flag   org.opencv.imgcodecs.Imgcodecs flag
     * @return org.opencv.core.Mat
     * @throws IOException
     */
    public static Mat inputStream2Mat(InputStream stream, int flag) throws IOException {
        byte[] byteBuff = IOUtils.toByteArray(stream);
        return Imgcodecs.imdecode(new MatOfByte(byteBuff), flag);
    }

    /**
     * Converts BufferedImage to InputStream.
     *
     * @param inImg Buffered Image
     * @return java.io.InputStream
     * @throws IOException
     */
    public static InputStream bufferedImage2InputStream(BufferedImage inImg) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(inImg, "png", os);
        return new ByteArrayInputStream(os.toByteArray());
    }

    /**
     * Converts BufferedImage to OpenCV Mat using custom flag.
     *
     * @param inImg Buffered Image
     * @param flag  org.opencv.imgcodecs.Imgcodecs flag
     * @return org.opencv.core.Mat
     * @throws IOException
     */
    public static Mat bufferedImage2Mat(BufferedImage inImg, int flag) throws IOException {
        return inputStream2Mat(bufferedImage2InputStream(inImg), flag);
    }

    /**
     * Converts BufferedImage to grayscaled OpenCV Mat.
     *
     * @param inImg Buffered Image
     * @return org.opencv.core.Mat
     * @throws IOException
     */
    public static Mat bufferedImage2GrayscaleMat(BufferedImage inImg) throws IOException {
        return bufferedImage2Mat(inImg, Imgcodecs.IMREAD_GRAYSCALE);
    }

}

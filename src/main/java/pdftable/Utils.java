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


public class Utils {

    private Utils() {
    }

    public static Mat inputStream2Mat(InputStream stream, int flags) throws IOException {
        byte[] byteBuff = IOUtils.toByteArray(stream);
        return Imgcodecs.imdecode(new MatOfByte(byteBuff), flags);
    }

    public static InputStream bufferedImage2InputStream(BufferedImage inImg) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(inImg, "png", os);
        return new ByteArrayInputStream(os.toByteArray());
    }

    public static Mat bufferedImage2Mat(BufferedImage inImg, int flags) throws IOException {
        return inputStream2Mat(bufferedImage2InputStream(inImg), flags);
    }

    public static Mat bufferedImage2GrayscaleMat(BufferedImage inImg) throws IOException {
        return bufferedImage2Mat(inImg, Imgcodecs.IMREAD_GRAYSCALE);
    }

}

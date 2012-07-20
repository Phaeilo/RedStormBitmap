import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

/**
 * This class implements read-only support for the Red Storm Bitmap image format.
 * 
 * @author Philip Huppert
 */
public class RedStormBitmap {

    private static final int ALPHA_OFFSET = 3;
    private static final int BLUE_OFFSET = 2;
    private static final int GREEN_OFFSET = 1;
    private static final int RED_OFFSET = 0;

    private int fileType;
    private int width;
    private int height;
    private int redDepth;
    private int greenDepth;
    private int blueDepth;
    private int alphaDepth;
    private BufferedImage image;

    /**
     * @return the bitmap's file type.
     */
    public int getFileType() {
        return this.fileType;
    }

    /**
     * @return the bitmap's width.
     */
    public int getWidth() {
        return this.width;
    }

    /**
     * @return the bitmap's height.
     */
    public int getHeight() {
        return this.height;
    }

    /**
     * @return the number of bits used for the red channel.
     */
    public int getRedDepth() {
        return this.redDepth;
    }

    /**
     * @return the number of bits used for the green channel.
     */
    public int getGreenDepth() {
        return this.greenDepth;
    }

    /**
     * @return the number of bits used for the blue channel.
     */
    public int getBlueDepth() {
        return this.blueDepth;
    }

    /**
     * @return the number of bits used for the alpha channel.
     */
    public int getAlphaDepth() {
        return this.alphaDepth;
    }

    /**
     * @return the bitmap as a {@code BufferedImage}.
     */
    public BufferedImage getImage() {
        return new BufferedImage(this.image.getColorModel(),
                this.image.copyData(null),
                this.image.isAlphaPremultiplied(),
                null);
    }

    @Override
    public String toString() {
        return String.format("RedStormBitmap [fileType=%d, width=%d, height=%d, redDepth=%d, greenDepth=%d, blueDepth=%d, alphaDepth=%d]",
                this.fileType,
                this.width,
                this.height,
                this.redDepth,
                this.greenDepth,
                this.blueDepth,
                this.alphaDepth);
    }

    /**
     * Load a {@code RedStormBitmap} from {@code file}.
     * @param file to load the bitmap from.
     * @return the loaded bitmap.
     * @throws FileNotFoundException if {@code file} was not found.
     * @throws IOException if {@code file} could not be read.
     */
    public static RedStormBitmap fromFile(File file) throws FileNotFoundException, IOException {
        InputStream input = new FileInputStream(file);
        RedStormBitmap bitmap = null;

        try {
            bitmap = RedStormBitmap.fromStream(input);
        } finally {
            input.close();
        }

        return bitmap;
    }

    /**
     * Load a {@code RedStormBitmap} from a file at {@code pathname}.
     * @param pathname of the file to load the bitmap from.
     * @return the loaded bitmap.
     * @throws FileNotFoundException if no file was found at {@code pathname}.
     * @throws IOException if the file could not be read.
     */
    public static RedStormBitmap fromFile(String pathname) throws FileNotFoundException, IOException {
        return RedStormBitmap.fromFile(new File(pathname));
    }

    /**
     * Load a {@code RedStormBitmap} from {@code stream}.
     * @param stream to load bitmap from.
     * @return the loaded bitmap.
     * @throws IOException if {@code stream} could not be read.
     */
    public static RedStormBitmap fromStream(InputStream stream) throws IOException {
        RedStormBitmap bitmap = new RedStormBitmap();
        bitmap.loadHeader(stream);
        bitmap.loadImage(stream);
        return bitmap;
    }

    /**
     * Load information from file header into class fields.
     * @param stream to load data from.
     * @throws IOException if {@code stream} could not be read. 
     */
    private void loadHeader(InputStream stream) throws IOException {
        byte[] header = new byte[28];
        stream.read(header);

        ByteBuffer byteBuffer = ByteBuffer.wrap(header);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        IntBuffer intBuffer = byteBuffer.asIntBuffer();

        this.fileType = intBuffer.get();
        this.width = intBuffer.get();
        this.height = intBuffer.get();
        this.redDepth = intBuffer.get();
        this.greenDepth = intBuffer.get();
        this.blueDepth = intBuffer.get();
        this.alphaDepth = intBuffer.get();
    }

    /**
     * Load image data into a {@code BufferedImage}.
     * @param stream to load data from.
     * @throws IOException if {@code stream} could not be read. 
     */
    private void loadImage(InputStream stream) throws IOException {
        int inputType = 0;

        if (this.redDepth == 4 && this.greenDepth == 4
                && this.blueDepth == 4 && this.alphaDepth == 4) {
            inputType = 4444;

        } else if (this.redDepth == 5 && this.greenDepth == 6
                && this.blueDepth == 5 && this.alphaDepth == 0) {
            inputType = 5650;

        } else if (this.redDepth == 8 && this.greenDepth == 8
                && this.blueDepth == 8 && this.alphaDepth == 0) {
            inputType = 8880;

        } else if (this.redDepth == 8 && this.greenDepth == 8
                && this.blueDepth == 8 && this.alphaDepth == 8) {
            inputType = 8888;

        } else {
            throw new RuntimeException(
                    String.format("Unsupported pixel format (redDepth=%d, greenDepth=%d, blueDepth=%d, alphaDepth=%d).",
                            this.redDepth, this.greenDepth, this.blueDepth, this.alphaDepth));
        }

        int pixelCount = this.width * this.height;
        int pixelLength = this.redDepth + this.blueDepth + this.greenDepth + this.alphaDepth;
        byte[] outputBuffer = new byte[pixelCount * 4];
        byte[] inputBuffer = new byte[pixelLength / 8 * pixelCount];
        stream.read(inputBuffer);

        for (int i = 0; i < pixelCount; i++) {
            switch (inputType) {
                case 8888:
                    outputBuffer[i*4+RED_OFFSET] = inputBuffer[i*4+1];
                    outputBuffer[i*4+GREEN_OFFSET] = inputBuffer[i*4+2];
                    outputBuffer[i*4+BLUE_OFFSET] = inputBuffer[i*4+3];
                    outputBuffer[i*4+ALPHA_OFFSET] = inputBuffer[i*4+0];
                    break;
                case 8880:
                    outputBuffer[i*4+RED_OFFSET] = inputBuffer[i*3+0];
                    outputBuffer[i*4+GREEN_OFFSET] = inputBuffer[i*3+1];
                    outputBuffer[i*4+BLUE_OFFSET] = inputBuffer[i*3+2];
                    outputBuffer[i*4+ALPHA_OFFSET] = (byte) 0xff;
                    break;
                case 5650:
                    outputBuffer[i*4+RED_OFFSET] = (byte) (inputBuffer[i*2+1] & 0xf8);
                    outputBuffer[i*4+GREEN_OFFSET] = (byte) (((inputBuffer[i*2+1] & 0x07) << 5) | ((inputBuffer[i*2+0] & 0xe0) >> 3));
                    outputBuffer[i*4+BLUE_OFFSET] = (byte) ((inputBuffer[i*2+0] & 0x1f) << 3);
                    outputBuffer[i*4+ALPHA_OFFSET] = (byte) 0xff;
                    break;
                case 4444:
                    outputBuffer[i*4+RED_OFFSET] = (byte) ((inputBuffer[i*2+1] & 0x0f) << 4);
                    outputBuffer[i*4+GREEN_OFFSET] = (byte) (inputBuffer[i*2+0] & 0xf0);
                    outputBuffer[i*4+BLUE_OFFSET] = (byte) ((inputBuffer[i*2+0] & 0x0f) << 4);
                    outputBuffer[i*4+ALPHA_OFFSET] = (byte) (inputBuffer[i*2+1] & 0xf0);
                    break;
            }
        }

        this.image = new BufferedImage(this.width, this.height, BufferedImage.TYPE_4BYTE_ABGR);
        WritableRaster raster = this.image.getRaster();
        raster.setDataElements(0, 0, this.width, this.height, outputBuffer);
    }

    /**
     * Private constructor, as {@code RedStormBitmap}s are constructed using static methods.
     */
    private RedStormBitmap() { }

}

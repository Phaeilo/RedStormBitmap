import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

import javax.imageio.ImageIO;

/**
 * This class implements a simple RSB to PNG converter.
 * It is mostly used to debug {@code RedStormBitmap} and also provides an
 * example on how to use {@code RedStormBitmap}.
 * 
 * @author Philip Huppert
 */
public class Converter {

    public static void main(String[] args) {
        try {
            _main(args);
        } catch (Throwable e) {
            System.err.println("FAIL!");
            System.err.println(e.toString());
            e.printStackTrace();
            System.err.println(Arrays.toString(args));
            System.err.println("---");
        }
    }

    private static void _main(String[] args) {
        if (args.length != 1) {
            System.err.println("Wrong number of arguments provided.");
            System.err.println(Arrays.toString(args));
            return;
        }

        File file = new File(args[0]);
        RedStormBitmap rsb;
        long start, stop;
        try {
            start = System.nanoTime();
            rsb = RedStormBitmap.fromFile(file);
            stop = System.nanoTime();
        } catch (FileNotFoundException e) {
            System.err.printf("File not found: %e%n", e.getMessage());
            return;
        } catch (IOException e) {
            System.err.printf("I/O error reading image: %e%n", e.getMessage());
            e.printStackTrace();
            return;
        }

        double duration = Math.round(((stop - start)/1000000000d)*10000)/10000d;
        System.out.printf("%s: %s (%f s)%n", file.getName(), rsb.toString(), duration);
        BufferedImage bimg = rsb.getImage();

        try {
            ImageIO.write(bimg, "PNG", new File(args[0]+".png"));
        } catch (IOException e) {
            System.err.printf("I/O error writing image: %e%n", e.getMessage());
            e.printStackTrace();
            return;
        }
    }

}

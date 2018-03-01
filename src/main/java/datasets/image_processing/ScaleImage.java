package datasets.image_processing;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;

import datasets.DatasetCreator;
import datasets.InputParameter;
import weka.core.Instance;

/**
 * Created by Vanessa Ackermann on 11.02.18.
 *
 * @author Vanessa Ackermann
 * @version 1.0
 */
public class ScaleImage extends DatasetCreator {

  private static final String DATASETNAME = "ScaleImage";
  private static final int NUMBEROFPARAMETERS = 3;

  public ScaleImage() {
    super(DATASETNAME, NUMBEROFPARAMETERS);
    List<InputParameter> inputParameters = new LinkedList<InputParameter>();
    inputParameters.add(new InputParameter("ImageWidth", 100, 6500, true, false));
    inputParameters.add(new InputParameter("ImageHeight", 100, 4010, true, false));
    inputParameters.add(new InputParameter("ScaleFactor", 0.1, 3, false, false));

    this.setInputParameters(inputParameters);
  }

  public double getRuntime(Instance instance) {
    BufferedImage img = null;
    try {
      img = ImageIO.read(new File("data/images/desk_6500x4010.jpg"));
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    int w = (int) instance.value(0);
    int h = (int) instance.value(1);
    double scaleFactor = instance.value(2);

    BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
    Graphics g = bi.getGraphics();
    g.drawImage(img, 0, 0, null);
    long startTime = System.nanoTime();
    int scaledWidth = (int) (w * scaleFactor);
    int scaledHeight = (int) (h * scaleFactor);
    BufferedImage scaledImage = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_RGB);
    AffineTransform at = new AffineTransform();
    at.scale(scaleFactor, scaleFactor);
    AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
    Graphics2D g2d = (Graphics2D) scaledImage.getGraphics();
    g2d.drawImage(bi, scaleOp, 0, 0);
    long stopTime = System.nanoTime();
    //Create output file from filtered image
    /*try {
      ImageIO.write(scaledImage, "jpg", new File("C:\\out2.jpg"));
    }
    catch (IOException e) {
      e.printStackTrace();
    } */
    g.dispose();
    g2d.dispose();
    return (stopTime - startTime);
  }

}

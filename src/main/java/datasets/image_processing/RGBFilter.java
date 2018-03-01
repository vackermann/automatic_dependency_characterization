package datasets.image_processing;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RGBImageFilter;
import java.awt.image.RescaleOp;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;

import datasets.DatasetCreator;
import datasets.InputParameter;
import weka.core.Instance;

/**
 * Created by Vanessa Ackermann on 08.02.18.
 *
 * @author Vanessa Ackermann
 * @version 1.0
 */
public class RGBFilter extends DatasetCreator {

  private static final String DATASETNAME = "RGBFilter";
  private static final int NUMBEROFPARAMETERS = 2;

  public RGBFilter() {
    super(DATASETNAME, NUMBEROFPARAMETERS);
    List<InputParameter> inputParameters = new LinkedList<InputParameter>();
    inputParameters.add(new InputParameter("ImageWidth", 100, 6500, true, false));
    inputParameters.add(new InputParameter("ImageHeight", 100, 4010, true, false));
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
    BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
    Graphics g = bi.getGraphics();
    g.drawImage(img, 0, 0, null);
    /* Create output file from cropped image
    try {
      ImageIO.write(bi, "jpg", new File("C:\\out.jpg"));
    }
    catch (IOException e) {
      e.printStackTrace();
    } */
    long startTime = System.nanoTime();
    float[] scales = {.5f, .5f, .5f};
    float[] offsets = new float[4];
    RescaleOp rop = new RescaleOp(scales, offsets, null);
    // Draw the image, applying the filter
    Graphics2D g2d = (Graphics2D) g;
    g2d.drawImage(bi, rop, 0, 0);
    long stopTime = System.nanoTime();
    /* Create output file from filtered image
    try {

      ImageIO.write(bi, "jpg", new File("C:\\out2.jpg"));
    }
    catch (IOException e) {
      e.printStackTrace();
    } */
    g2d.dispose();
    return (stopTime - startTime);
  }

}

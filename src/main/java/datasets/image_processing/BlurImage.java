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
import ij.ImagePlus;
import ij.process.ImageProcessor;
import weka.core.Instance;
import ij.plugin.filter.*;

/**
 * Created by Vanessa Ackermann on 11.02.18.
 *
 * @author Vanessa Ackermann
 * @version 1.0
 */
public class BlurImage extends DatasetCreator {

  private static final String DATASETNAME = "GaussianFilter";
  private static final int NUMBEROFPARAMETERS = 3;

  public BlurImage() {
    super(DATASETNAME, NUMBEROFPARAMETERS);
    List<InputParameter> inputParameters = new LinkedList<InputParameter>();
    inputParameters.add(new InputParameter("ImageWidth", 100, 6500, true, false));
    inputParameters.add(new InputParameter("ImageHeight", 100, 4010, true, false));
    inputParameters.add(new InputParameter("Sigma", 1, 10, false, false));

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
    double sigma = instance.value(2);

    BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
    Graphics g = bi.getGraphics();
    g.drawImage(img, 0, 0, null);
    try {
      ImageIO.write(bi, "jpg", new File("C:\\out1.jpg"));
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    long startTime = System.nanoTime();

    ij.plugin.filter.GaussianBlur gaussianBlur = new ij.plugin.filter.GaussianBlur();
    ImagePlus imagePlus = new ImagePlus("Image", bi);
    ImageProcessor processor = imagePlus.getProcessor();
    gaussianBlur.blurGaussian(processor, sigma);
    BufferedImage blurredImage = imagePlus.getBufferedImage();
    long stopTime = System.nanoTime();
    //Create output file from filtered image
    try {
      ImageIO.write(blurredImage, "jpg", new File("C:\\out2.jpg"));
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    g.dispose();
    return (stopTime - startTime);
  }

}

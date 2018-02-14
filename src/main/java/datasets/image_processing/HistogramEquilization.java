package datasets.image_processing;

import java.awt.*;
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

import static datasets.image_processing.HistogramEQ.computeHistogramEQ;

/**
 * Created by Vanessa Ackermann on 11.02.18.
 *
 * @author Vanessa Ackermann
 * @version 1.0
 */
public class HistogramEquilization extends DatasetCreator {

  private static final String DATASETNAME = "HistogramEquilization";
  private static final int NUMBEROFPARAMETERS = 2;

  public HistogramEquilization() {
    super(DATASETNAME, NUMBEROFPARAMETERS);
    List<InputParameter> inputParameters = new LinkedList<InputParameter>();
    inputParameters.add(new InputParameter("ImageWidth", 100, 6500, true));
    inputParameters.add(new InputParameter("ImageHeight", 100, 4010, true));

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

    long startTime = System.nanoTime();
    BufferedImage equalizedImage = HistogramEQ.computeHistogramEQ(bi);
    long stopTime = System.nanoTime();
    //Create output file from filtered image
    try {
      ImageIO.write(equalizedImage, "jpg", new File("C:\\out2.jpg"));
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    g.dispose();
    return (stopTime - startTime);
  }

}

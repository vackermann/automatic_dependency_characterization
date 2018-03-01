package datasets.image_processing;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.CropImageFilter;
import java.awt.image.DataBuffer;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
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
 * Created by Vanessa Ackermann on 11.02.18.
 *
 * @author Vanessa Ackermann
 * @version 1.0
 */
public class CropImage extends DatasetCreator {

  private static final String DATASETNAME = "CropImage";
  private static final int NUMBEROFPARAMETERS = 4;

  public CropImage() {
    super(DATASETNAME, NUMBEROFPARAMETERS);
    List<InputParameter> inputParameters = new LinkedList<InputParameter>();
    inputParameters.add(new InputParameter("ImageWidth", 100, 6500, true, false));
    inputParameters.add(new InputParameter("ImageHeight", 100, 4010, true, false));
    inputParameters.add(new InputParameter("WantedImageWidth", 100, 6500, true, false));
    inputParameters.add(new InputParameter("WantedImageHeight", 100, 4010, true, false));

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
    int wantedWidth = Math.min((int) instance.value(2), w);
    int wantedHeight = Math.min((int) instance.value(3), h);

    BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
    Graphics g = bi.getGraphics();
    g.drawImage(img, 0, 0, null);
    long startTime = System.nanoTime();
    Toolkit tk = Toolkit.getDefaultToolkit();
    ImageFilter cropFilter = new CropImageFilter(0, 0, wantedWidth, wantedHeight);
    Image cropImage = tk.createImage(new FilteredImageSource(img.getSource(), cropFilter));
    long stopTime = System.nanoTime();
    g.dispose();
    return (stopTime - startTime);
  }

}

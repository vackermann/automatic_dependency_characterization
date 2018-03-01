package datasets.math_functions;

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

/**
 * Created by Vanessa Ackermann on 11.02.18.
 *
 * @author Vanessa Ackermann
 * @version 1.0
 */
public class AckermannFunction extends DatasetCreator {

  private static final String DATASETNAME = "AckermannFunction";
  private static final int NUMBEROFPARAMETERS = 2;

  public AckermannFunction() {
    super(DATASETNAME, NUMBEROFPARAMETERS);
    List<InputParameter> inputParameters = new LinkedList<InputParameter>();
    inputParameters.add(new InputParameter("n", 0, 3, true, false));
    inputParameters.add(new InputParameter("m", 0, 3, true, false));

    this.setInputParameters(inputParameters);
  }

  public static long ackermann(long n, long m) {
    if (n == 0) {
      return m + 1;
    }
    else if (m == 0) {
      return ackermann(n - 1, 1);
    }
    else {
      return ackermann(n - 1, ackermann(n, m - 1));
    }

  }

  public double getRuntime(Instance instance) {
    int n = (int) instance.value(0);
    int m = (int) instance.value(1);
    long startTime = System.nanoTime();
    long result = ackermann(n, m);
    long stopTime = System.nanoTime();
    return (stopTime - startTime);
  }

}

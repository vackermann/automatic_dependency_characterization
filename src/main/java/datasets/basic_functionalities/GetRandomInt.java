package datasets.basic_functionalities;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import datasets.DatasetCreator;
import datasets.InputParameter;
import weka.core.Instance;

import static com.sun.tools.javac.jvm.ByteCodes.ret;
import static javafx.scene.input.KeyCode.F;

/**
 * Created by Vanessa Ackermann on 19.03.18.
 *
 * @author Vanessa Ackermann
 * @version 1.0
 */
public class GetRandomInt extends DatasetCreator {

  private static final String DATASETNAME = "GetRandomInt";
  private static final int NUMBEROFPARAMETERS = 2;

  public GetRandomInt() {
    super(DATASETNAME, NUMBEROFPARAMETERS);
    List<InputParameter> inputParameters = new LinkedList<InputParameter>();
    inputParameters.add(new InputParameter("MinInt", 1, 100000, true, false));
    inputParameters.add(new InputParameter("MaxInt", 1, 200000, true, false));
    this.setInputParameters(inputParameters);
  }

  public double getRuntime(Instance instance) {
    int minInt = (int) instance.value(0);
    int maxInt = (int) instance.value(1);

    long startTime = System.nanoTime();
    if (minInt > maxInt) {
      int randomNum = -1;
    }
    else {
      int randomNum =
          ThreadLocalRandom.current().nextInt(minInt, maxInt + 1); //+1 because nextInt is normally exclusive
    }
    long stopTime = System.nanoTime();
    return (stopTime - startTime);
  }

}

package datasets.basic_functionalities;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import com.sun.tools.javac.util.ArrayUtils;

import datasets.DatasetCreator;
import datasets.InputParameter;
import weka.core.Instance;

/**
 * Created by Vanessa Ackermann on 06.02.18.
 *
 * @author Vanessa Ackermann
 * @version 1.0
 */
public class SearchArray extends DatasetCreator {

  private static final String DATASETNAME = "SearchArray";
  private static final int NUMBEROFPARAMETERS = 2;

  public SearchArray() {
    super(DATASETNAME, NUMBEROFPARAMETERS);
    List<InputParameter> inputParameters = new LinkedList<InputParameter>();
    inputParameters.add(new InputParameter("ArraySize", 0, 100000, true, false));
    inputParameters.add(new InputParameter("Key", 0, 100000, true, false));
    this.setInputParameters(inputParameters);
  }

  public double getRuntime(Instance instance) {
    int arraySize = (int) instance.value(0);
    int[] randomArray = randomIntArray(arraySize);
    int key = (int) instance.value(1);
    long startTime = System.nanoTime();
    boolean containsKey = Arrays.asList(randomArray).contains(key);
    long stopTime = System.nanoTime();
    return (stopTime - startTime);
  }

  public int[] randomIntArray(int size) {
    Random rand = new Random();
    int[] result = new int[size];
    for (int i = 0; i < result.length; i++) {
      result[i] = rand.nextInt();
    }
    return result;
  }
}

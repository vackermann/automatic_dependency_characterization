package datasets.basic_functionalities;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import datasets.DatasetCreator;
import datasets.InputParameter;
import weka.core.Instance;

/**
 * Created by Vanessa Ackermann on 22.01.18.
 *
 * @author Vanessa Ackermann
 * @version 1.0
 */
public class SortArray extends DatasetCreator {

  private static final String DATASETNAME = "SortArray";
  private static final int NUMBEROFPARAMETERS = 1;

  public SortArray() {
    super(DATASETNAME, NUMBEROFPARAMETERS);
    List<InputParameter> inputParameters = new LinkedList<InputParameter>();
    inputParameters.add(new InputParameter("ArraySize", 1, 10000, true, false));
    this.setInputParameters(inputParameters);
  }

  public double getRuntime(Instance instance) {
    int arraySize = (int) instance.value(0);
    int[] randomArray = randomIntArray(arraySize);
    long startTime = System.nanoTime();
    Arrays.sort(randomArray);
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

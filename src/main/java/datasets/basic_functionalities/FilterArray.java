package datasets.basic_functionalities;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import datasets.DatasetCreator;
import datasets.InputParameter;
import weka.core.Instance;

import static java.util.Arrays.stream;

/**
 * Created by Vanessa Ackermann on 06.02.18.
 *
 * @author Vanessa Ackermann
 * @version 1.0
 */
public class FilterArray extends DatasetCreator {

  private static final String DATASETNAME = "FilterArray";
  private static final int NUMBEROFPARAMETERS = 2;

  public FilterArray() {
    super(DATASETNAME, NUMBEROFPARAMETERS);
    List<InputParameter> inputParameters = new LinkedList<InputParameter>();
    inputParameters.add(new InputParameter("ArraySize", 0, 100000, true, false));
    inputParameters.add(new InputParameter("FilterKey", 0, 100000, true, false));
    this.setInputParameters(inputParameters);
  }

  public double getRuntime(Instance instance) {
    int arraySize = (int) instance.value(0);
    int[] randomArray = randomIntArray(arraySize);
    int filterKey = (int) instance.value(1);
    long startTime = System.nanoTime();
    int[] result = Arrays.stream(randomArray).filter(i -> i <= filterKey).toArray();
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

package datasets.math_functions;

/**
 * Created by Vanessa Ackermann on 11.02.18.
 *
 * @author Vanessa Ackermann
 * @version 1.0
 */

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import datasets.DatasetCreator;
import datasets.InputParameter;
import weka.core.Instance;

import static datasets.math_functions.AckermannFunction.ackermann;
import static org.boon.core.Typ.intArray;

/**
 * Find a subset of elements that are selected from a given set whose sum adds
 * up to a given number K. Assume that the set contains non-negative, unique
 * values.
 */
public class SubsetSum extends DatasetCreator {

  private static final String DATASETNAME = "SubsetSum";
  private static final int NUMBEROFPARAMETERS = 2;

  public SubsetSum() {
    super(DATASETNAME, NUMBEROFPARAMETERS);
    List<InputParameter> inputParameters = new LinkedList<InputParameter>();
    inputParameters.add(new InputParameter("ArraySize", 1, 10000, true, false));
    inputParameters.add(new InputParameter("Sum", 1, 100000, true, false));

    this.setInputParameters(inputParameters);
  }

  public double getRuntime(Instance instance) {
    int arraySize = (int) instance.value(0);
    int sum = (int) instance.value(1);
    int[] array = randomUniqueIntArray(arraySize);
    long startTime = System.nanoTime();
    List<Integer> result = findSubsetSum(array, sum);
    long stopTime = System.nanoTime();
    return (stopTime - startTime);
  }

  public static List<Integer> findSubsetSum(int[] numbers, int sum) {
    ArrayList<Integer> subset = new ArrayList<Integer>();
    for (int i = 0; i < numbers.length; i++) {
      if (findSubsetSum(numbers, i, subset, sum)) {
        return subset;
      }
    }

    return null;
  }

  private static boolean findSubsetSum(int[] numbers, int index, ArrayList<Integer> subset, int sum) {
    if (index >= numbers.length) {
      return false;
    }

    if (sum - numbers[index] == 0) {
      subset.add(numbers[index]);
      return true;
    }

    if (sum - numbers[index] < 0) {
      return false;
    }

    sum -= numbers[index];
    for (int i = index + 1; i < numbers.length; i++) {
      if (findSubsetSum(numbers, i, subset, sum)) {
        subset.add(numbers[index]);
        return true;
      }
    }

    return false;
  }

  public int[] randomUniqueIntArray(int size) {
    int[] result = ThreadLocalRandom.current().ints(1, 100000).distinct().limit(size).toArray();
    return result;
  }

}
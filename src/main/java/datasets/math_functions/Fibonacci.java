package datasets.math_functions;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.boon.core.Sys;

import datasets.DatasetCreator;
import datasets.InputParameter;
import weka.core.Instance;

import static datasets.math_functions.AckermannFunction.ackermann;

/**
 * Created by Vanessa Ackermann on 11.02.18.
 *
 * @author Vanessa Ackermann
 * @version 1.0
 */
public class Fibonacci extends DatasetCreator {

  private static final String DATASETNAME = "Fibonacci";
  private static final int NUMBEROFPARAMETERS = 2;

  public Fibonacci() {
    super(DATASETNAME, NUMBEROFPARAMETERS);
    List<InputParameter> inputParameters = new LinkedList<InputParameter>();
    inputParameters.add(new InputParameter("FibonacciNumber", 1, 40, true, false));
    inputParameters.add(
        new InputParameter("ComputationMode (0=Iterative / 1=RecursiveOptimized / 2=Recursive)", 0, 2, true, true));

    this.setInputParameters(inputParameters);
  }

  //--------------- iterative version ---------------------
  static int FibonacciIterative(int n) {
    if (n == 0) {
      return 0;
    }
    if (n == 1) {
      return 1;
    }

    int prevPrev = 0;
    int prev = 1;
    int result = 0;

    for (int i = 2; i <= n; i++) {
      result = prev + prevPrev;
      prevPrev = prev;
      prev = result;
    }
    return result;
  }

  //--------------- naive recursive version ---------------------
  static int FibonacciRecursive(int n) {
    if (n == 0) {
      return 0;
    }
    if (n == 1) {
      return 1;
    }

    return FibonacciRecursive(n - 1) + FibonacciRecursive(n - 2);
  }

  //--------------- optimized recursive version ---------------------
  static HashMap<Integer, Integer> resultHistory = new HashMap<Integer, Integer>();

  static int FibonacciRecursiveOpt(int n) {
    if (n == 0) {
      return 0;
    }
    if (n == 1) {
      return 1;
    }
    if (resultHistory.containsKey(n)) {
      return resultHistory.get(n);
    }

    int result = FibonacciRecursiveOpt(n - 1) + FibonacciRecursiveOpt(n - 2);
    resultHistory.put(n, result);

    return result;
  }

  public double getRuntime(Instance instance) {
    int n = (int) instance.value(0);
    int mode = (int) instance.value(1);
    int result;

    long startTime = System.nanoTime();
    switch (mode) {
      case 0:
        result = FibonacciIterative(n);
        break;
      case 1:
        result = FibonacciRecursiveOpt(n);
        break;
      case 2:
        result = FibonacciRecursive(n);
        break;
    }
    long stopTime = System.nanoTime();
    return (stopTime - startTime);
  }
}

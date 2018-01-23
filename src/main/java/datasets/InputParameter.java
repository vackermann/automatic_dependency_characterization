package datasets;

/**
 * Class representing an numeric input parameter. Value range of parameter is described by lower and upper bound.
 * Enables to get equidistant values within value range, either as doubles or integer doubles.
 * <p>
 * Created by Vanessa Ackermann on 22.01.18.
 *
 * @author Vanessa Ackermann
 * @version 1.0
 */
public class InputParameter {

  private String name;
  private double lowerBound;
  private double upperBound;
  boolean onlyIntegerValues;

  /**
   * @return the name of the input parameter
   */
  public String getName() {
    return name;
  }

  /**
   * Constructs an InputParamter object.
   *
   * @param name
   *     name of the input parameter
   * @param lowerBound
   *     lower bound of the parameters value range
   * @param upperBound
   *     upper bound of the parameters value range
   * @param onlyIntegerValues
   *     only integer values are allowed as parameter values
   */
  public InputParameter(String name, double lowerBound, double upperBound, boolean onlyIntegerValues) {
    this.name = name;
    this.lowerBound = lowerBound;
    this.upperBound = upperBound;
    this.onlyIntegerValues = onlyIntegerValues;
  }

  /**
   * Returns the value of the index-th step if the input parameters value range was equidistantly divided into
   * totalSteps.
   *
   * @param index
   *     index of step within all steps through the parameters value range
   * @param totalSteps
   *     total number of steps in which the parameters value range is divided
   *
   * @return the value at the index-th step of the value range
   */
  double getValueAt(int index, int totalSteps) {
    double stepSize = (upperBound - lowerBound) / totalSteps;
    double result = stepSize * index;
    if (onlyIntegerValues) {
      result = (int) Math.round(result);
    }
    return result;
  }
}

package prediction_tool;

/**
 * SetDescription is a container object for the runtime data sets and their attributes (e.g., name, numParameters, filepath).
 *
 * @author Vanessa Ackermann
 * @version 1.0
 */
public class SetDescription {

  private String name;
  private String filepath;
  private int numParameter;
  boolean hasNominal;

  /**
   * Creates new SetDescription object for a data set.
   *
   * @param name
   * @param filepath
   * @param numParameter
   * @param hasNominal
   */
  public SetDescription(String name, String filepath, int numParameter, boolean hasNominal) {
    this.name = name;
    this.filepath = filepath;
    this.numParameter = numParameter;
    this.hasNominal = hasNominal;
  }

  /**
   * Get name of data set (e.g. SortArray).
   *
   * @return name
   */
  public String getName() {
    return name;
  }

  /**
   * Get filepath of data set.
   * @return filepath
   */
  public String getFilepath() {
    return filepath;
  }

  /**
   * Get number of observed input parameters in data set (e.g., 1 for SortArray).
   * @return number of parameters
   */
  public int getNumParameter() {
    return numParameter;
  }

  /**
   * Returns true if one of the observed input parameters is nominal variable
   * (e.g., hasNominal() of Fibonacci data set is true because of 'Mode' parameter).
   * @return data set has nominal parameter
   */
  public boolean isHasNominal() {
    return hasNominal;
  }
}

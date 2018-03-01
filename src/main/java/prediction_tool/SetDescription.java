package prediction_tool;

/**
 * Created by Vanessa Ackermann on 01.03.18.
 *
 * @author Vanessa Ackermann
 * @version 1.0
 */
public class SetDescription {

  private String name;
  private String filepath;
  private int numParameter;
  boolean hasNominal;

  public SetDescription(String name, String filepath, int numParameter, boolean hasNominal) {
    this.name = name;
    this.filepath = filepath;
    this.numParameter = numParameter;
    this.hasNominal = hasNominal;
  }

  public String getName() {
    return name;
  }

  public String getFilepath() {
    return filepath;
  }

  public int getNumParameter() {
    return numParameter;
  }

  public boolean isHasNominal() {
    return hasNominal;
  }
}

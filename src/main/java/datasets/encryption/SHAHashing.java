package datasets.encryption;

import java.security.MessageDigest;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.text.RandomStringGenerator;

import datasets.DatasetCreator;
import datasets.InputParameter;
import weka.core.Instance;

/**
 * Created by Vanessa Ackermann on 12.02.18.
 *
 * @author Vanessa Ackermann
 * @version 1.0
 */
public class SHAHashing extends DatasetCreator {

  private static final String DATASETNAME = "SHAHashing";
  private static final int NUMBEROFPARAMETERS = 2;

  public SHAHashing() {
    super(DATASETNAME, NUMBEROFPARAMETERS);
    List<InputParameter> inputParameters = new LinkedList<InputParameter>();
    inputParameters.add(new InputParameter("StringLength", 0, 10000, true, false));
    inputParameters.add(new InputParameter("SHA-Mode (0=SHA-1, 1= SHA-256, 2=SHA-512)", 0, 2, true, true));

    this.setInputParameters(inputParameters);
  }

  public double getRuntime(Instance instance) {
    int length = (int) instance.value(0);
    int mode = (int) instance.value(1);
    String randomString = createRandomString(length);

    long startTime = System.nanoTime();
    MessageDigest digest;
    try {
      switch (mode) {
        case 0:
          digest = MessageDigest.getInstance("SHA-1"); //has 160 bits as output size
          break;
        case 1:
          digest = MessageDigest.getInstance("SHA-256");
          break;
        case 2:
          digest = MessageDigest.getInstance("SHA-512");
          break;
        default:
          digest = MessageDigest.getInstance("SHA-1");
      }
      byte[] encodedhash = digest.digest(randomString.getBytes());
    }
    catch (Exception e) {
      System.out.print(e.getMessage());
    }
    long stopTime = System.nanoTime();
    return (stopTime - startTime);
  }

  public String createRandomString(int length) {
    RandomStringGenerator randomStringGenerator = new RandomStringGenerator.Builder().withinRange('0', 'z').build();
    return randomStringGenerator.generate(length);
  }

}

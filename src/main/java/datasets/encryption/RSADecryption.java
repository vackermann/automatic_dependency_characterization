package datasets.encryption;

import java.security.KeyPair;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.text.RandomStringGenerator;

import datasets.DatasetCreator;
import datasets.InputParameter;
import weka.core.Instance;

/**
 * Created by Vanessa Ackermann on 14.02.18.
 *
 * @author Vanessa Ackermann
 * @version 1.0
 */
public class RSADecryption extends DatasetCreator {

  private static final String DATASETNAME = "RSADecryption";
  private static final int NUMBEROFPARAMETERS = 2;

  public RSADecryption() {
    super(DATASETNAME, NUMBEROFPARAMETERS);
    List<InputParameter> inputParameters = new LinkedList<InputParameter>();
    inputParameters.add(new InputParameter("StringLength", 0, 30, true, false));
    // https://www.javamex.com/tutorials/cryptography/rsa_key_length.shtml
    inputParameters.add(new InputParameter("KeySizeExponent(2^x)", 9, 11, true, false));

    this.setInputParameters(inputParameters);
  }

  public double getRuntime(Instance instance) {
    int length = (int) instance.value(0);
    int keySizeExponent = (int) instance.value(1);
    int keySize = (int) Math.pow(2, keySizeExponent);
    String randomString = createRandomString(length);
    KeyPair kp = null;
    byte[] encryptedData = new byte[0];
    try {
      kp = RSA.generateKeyPair(keySize);
      encryptedData = RSA.encrypt(randomString, kp.getPublic());
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    long startTime = System.nanoTime();
    try {
      String decryptedString = RSA.decrypt(encryptedData, kp.getPrivate());
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    long stopTime = System.nanoTime();
    return (stopTime - startTime);
  }

  public String createRandomString(int length) {
    RandomStringGenerator randomStringGenerator = new RandomStringGenerator.Builder().withinRange('0', 'z').build();
    return randomStringGenerator.generate(length);
  }

}

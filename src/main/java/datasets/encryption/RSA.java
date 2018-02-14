package datasets.encryption;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;

/**
 * Created by Vanessa Ackermann on 14.02.18.
 *
 * @author Vanessa Ackermann
 * @version 1.0
 */
public class RSA {

  public static KeyPair generateKeyPair(int keySize) throws Exception {
    KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
    generator.initialize(keySize, new SecureRandom());
    KeyPair pair = generator.generateKeyPair();
    return pair;
  }

  public static byte[] encrypt(String plainText, PublicKey publicKey) throws Exception {
    Cipher encryptCipher = Cipher.getInstance("RSA");
    encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);

    return encryptCipher.doFinal(plainText.getBytes());
  }

  public static String decrypt(byte[] bytes, PrivateKey privateKey) throws Exception {
    Cipher decriptCipher = Cipher.getInstance("RSA");
    decriptCipher.init(Cipher.DECRYPT_MODE, privateKey);

    return new String(decriptCipher.doFinal(bytes));
  }
}

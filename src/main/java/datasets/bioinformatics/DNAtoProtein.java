package datasets.bioinformatics;

import java.security.MessageDigest;
import java.util.LinkedList;
import java.util.List;

import datasets.DatasetCreator;
import datasets.InputParameter;
import weka.core.Instance;

//import org.biojava.nbio.structure.*;
//import org.biojava.nbio.core.sequence.DNASequence;

/**
 * Created by Vanessa Ackermann on 12.02.18.
 *
 * @author Vanessa Ackermann
 * @version 1.0
 */
public class DNAtoProtein extends DatasetCreator {

  private static final String DATASETNAME = "DNAtoProtein";
  private static final int NUMBEROFPARAMETERS = 1;

  public DNAtoProtein() {
    super(DATASETNAME, NUMBEROFPARAMETERS);
    List<InputParameter> inputParameters = new LinkedList<InputParameter>();
    inputParameters.add(new InputParameter("DNA_Length", 0, 100000, true, false));
    this.setInputParameters(inputParameters);
  }

  public double getRuntime(Instance instance) {
    int length = (int) instance.value(0);
    int mode = (int) instance.value(1);
    //String randomString = createRandomString(length);
    //DNASequence seq = new DNASequence("GTAC");

    long startTime = System.nanoTime();

    long stopTime = System.nanoTime();
    return (stopTime - startTime);
  }
/* TODO
  public void translateDNAtoProtein(String dna) {
    try {
      //create a DNA SymbolList
      SymbolList symL = DNATools.createDNA("atggccattgaatga");

      //transcribe to RNA (after biojava 1.4 this method is deprecated)
      symL = RNATools.transcribe(symL);

      //transcribe to RNA (after biojava 1.4 use this method instead)
      symL = DNATools.toRNA(symL);

      //translate to protein
      symL = RNATools.translate(symL);

      //prove that it worked
      System.out.println(symL.seqString());
    }catch (IllegalAlphabetException ex) {


     /*
      * this will occur if you try and transcribe a non DNA sequence or translate
      * a sequence that isn't a triplet view on a RNA sequence.

      ex.printStackTrace();
    }catch (IllegalSymbolException ex) {
      // this will happen if non IUB characters are used to create the DNA SymbolList
      ex.printStackTrace();
    }
  }
  */

}

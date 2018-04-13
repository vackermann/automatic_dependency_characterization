package datasets;

import java.util.ArrayList;
import java.util.List;

import datasets.basic_functionalities.FilterArray;
import datasets.basic_functionalities.GetRandomInt;
import datasets.basic_functionalities.LoadFile;
import datasets.basic_functionalities.SearchArray;
import datasets.basic_functionalities.SortArray;
import datasets.encryption.RSADecryption;
import datasets.encryption.RSAEncryption;
import datasets.encryption.SHAHashing;
import datasets.image_processing.BlurImage;
import datasets.image_processing.HistogramEqualization;
import datasets.image_processing.RGBFilter;
import datasets.image_processing.ScaleImage;
import datasets.math_functions.AckermannFunction;
import datasets.math_functions.Fibonacci;
import datasets.math_functions.SubsetSum;
import prediction_tool.MetaClassifierEvaluation;
import prediction_tool.SetDescription;

public class Main {

  public static void main(String[] args) {

    //Creates objects for the runime data sets

    // Java functionalities
    SortArray a1 = new SortArray();
    SearchArray a2 = new SearchArray();
    FilterArray a3 = new FilterArray();
    LoadFile a4 = new LoadFile();
    GetRandomInt a5 = new GetRandomInt();

    // Math functions
    AckermannFunction m1 = new AckermannFunction();
    Fibonacci m2 = new Fibonacci();
    SubsetSum m3 = new SubsetSum();

    //Image processing
    RGBFilter i1 = new RGBFilter();
    ScaleImage i2 = new ScaleImage();
    BlurImage i3 = new BlurImage();
    HistogramEqualization i4 = new HistogramEqualization();

    //Encryption
    SHAHashing e1 = new SHAHashing();
    RSAEncryption e2 = new RSAEncryption();
    RSADecryption e3 = new RSADecryption();

    //Run the following in order to build a runtime data set for SortArray (object a1) of size 100000.
    //a1.createDatasetFile(100000);

    List<DatasetCreator> datasetCreators = new ArrayList<DatasetCreator>();

    datasetCreators.add(a1);
    datasetCreators.add(a2);
    datasetCreators.add(a3);
    datasetCreators.add(a4);
    datasetCreators.add(a5);
    datasetCreators.add(m1);
    datasetCreators.add(m2);
    datasetCreators.add(m3);
    datasetCreators.add(i1);
    datasetCreators.add(i2);
    datasetCreators.add(i3);
    datasetCreators.add(i4);
    datasetCreators.add(e1);
    datasetCreators.add(e2);
    datasetCreators.add(e3);

    List<SetDescription> setDescriptions = new ArrayList<SetDescription>();

    for (DatasetCreator datasetCreator : datasetCreators) {
      setDescriptions.add(
          new SetDescription(datasetCreator.getDatasetName(), setnameToARFFFilepath(datasetCreator.getDatasetName()),
              datasetCreator.getNumberOfParameters(), datasetCreator.hasNominal()));
    }

    // This belongs to the predictor evaluation, where the predictors were evaluated in 10 runs on the above specified
    // data sets
    /*
    StepwiseEvaluation.evaluatePredictorsOnSetsMultipleTimes(setDescriptions, 10);
     */
    
    // This belongs to the DML Case study, where a CSV with multiple prediction results for the transcription service were created
    /*
    RuntimePrediction runtimePrediction = new RuntimePrediction(StepwiseEvaluation.loadDatasetFromFilepath(setnameToCSVFilepath("TranscriptionService")));
    //System.out.println("Hallo");
    String result = runtimePrediction.predictAllInFile(setnameToCSVFilepath("TranscriptionService_test"));
    //System.out.print(result);
    try {
      File file = new File("results2.csv");
      FileWriter fileWriter = new FileWriter(file);
      fileWriter.write(result);
      //fileWriter.flush();
      fileWriter.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    */

    // This belongs to the meta-classifier evaluation, where the t-value of the paired sample test is determined and
    // printed to the console
    /*
      double[] arr = MetaClassifierEvaluation.getMAEDifferencesOnSets(setDescriptions, 15);
      System.out.println("t-Value is: "+MetaClassifierEvaluation.getTValue(arr)+" and n is: "+arr.length);
    */


    /*
      String datasetName = "GetRandomInt";
      try {
        Simulation sim = new Simulation("./data/arff/" + datasetName + ".arff");
        sim.startSimulation(10);
        sim.incrementallyAddInstances(50);
        sim.predictAfter(100);
        sim.predictAfter(1000);
        sim.predictAfter(2000);
        sim.predictAfter(5000);
        sim.predictAfter(10000);
        sim.predictAfter(50000);
        sim.predictAfter(100000);
      }
      catch (IOException e) {
        e.printStackTrace();
      }
     */

  }

  /**
   * Returns the full filepath to the Arff-file of the runtime data set with the given name.
   *
   * @param setname
   *
   * @return
   */
  static public String setnameToARFFFilepath(String setname) {
    return "./data/arff/" + setname + ".arff";
  }

  /**
   * Returns the full filepath to the CSV-file of the runtime data set with the given name.
   *
   * @param setname
   *
   * @return
   */
  static public String setnameToCSVFilepath(String setname) {
    return "./data/csv/" + setname + ".csv";
  }
}

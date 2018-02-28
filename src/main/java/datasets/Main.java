package datasets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import datasets.basic_functionalities.FilterArray;
import datasets.basic_functionalities.SearchArray;
import datasets.basic_functionalities.SortArray;
import datasets.encryption.RSADecryption;
import datasets.encryption.RSAEncryption;
import datasets.encryption.SHAHashing;
import datasets.image_processing.BlurImage;
import datasets.image_processing.CropImage;
import datasets.image_processing.HistogramEqualization;
import datasets.image_processing.RGBFilter;
import datasets.image_processing.ScaleImage;
import datasets.math_functions.AckermannFunction;
import datasets.math_functions.Fibonacci;
import datasets.math_functions.SubsetSum;
import prediction_tool.PredictorEvaluation;
import prediction_tool.Simulation;
import prediction_tool.StepwiseEvaluation;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.functions.SGD;
import weka.core.Utils;

import static javafx.scene.input.KeyCode.M;

public class Main {

  public static void main(String[] args) {

    // Java functionalities
    SortArray a1 = new SortArray();
    SearchArray a2 = new SearchArray();
    FilterArray a3 = new FilterArray();

    // Math functions
    AckermannFunction m1 = new AckermannFunction();
    Fibonacci m2 = new Fibonacci();
    SubsetSum m3 = new SubsetSum();

    //Image processing
    RGBFilter i1 = new RGBFilter();
    CropImage i2 = new CropImage();
    ScaleImage i3 = new ScaleImage();
    BlurImage i4 = new BlurImage();
    HistogramEqualization i5 = new HistogramEqualization();

    //Encryption
    SHAHashing e1 = new SHAHashing();
    RSAEncryption e2 = new RSAEncryption();
    RSADecryption e3 = new RSADecryption();

    //e1.createDatasetFile(100);
    //e2.createDatasetFile(100);
    //e3.createDatasetFile(100);

    String name = "SortArray";
    try {
      StepwiseEvaluation eval = new StepwiseEvaluation("./data/arff/" + name + ".arff", name);
      eval.evaluate();
    }
    catch (IOException e) {
      e.printStackTrace();
    }

/*
    String datasetName = "SortArray";
    try {
      Simulation sim = new Simulation("./data/arff/" + datasetName + ".arff");
      sim.startSimulation(100);
      sim.incrementallyAddInstances(10);
      sim.predictAfter(100);
    }
    catch (IOException e) {
      e.printStackTrace();
    }
*/

    /*
    List<String> datasetNames = new ArrayList<String>();
    datasetNames.add("CropImage");
    datasetNames.add(("SortArray"));

    MultilayerPerceptron mlp = new MultilayerPerceptron();
    SGD sdg = new SGD();
    String[] options = new String[2];
    options[0] = "-F";
    options[1] = "2";
    try {
      sdg.setOptions(options);
      mlp.setOptions(Utils.splitOptions("-L 0.1 -M 0.2 -N 2000 -V 0 -S 0 -E 20 -H 3"));
    }
    catch (Exception e) {
      e.printStackTrace();
    }

    String report = PredictorEvaluation.evaluatePredictor(sdg, PredictorEvaluation.getFileListFromDatasetNames(datasetNames));
    System.out.println(report);
*/

    /*
    String datasetName = "SortArray";
    ArffLoader loader = new ArffLoader();
    try {
      loader.setFile(new File("./data/arff/" + datasetName + ".arff"));
      Instances data = loader.getStructure();
      Instance insta = loader.getNextInstance(data);
      double runtime = insta.value(insta.numAttributes()-1);
      System.out.println("Actual runtime: "+runtime);
      for (int i=0; i<=90000; i++) {
        data.add(loader.getNextInstance(data));
        if (i % 1000 == 0) {
          RuntimePrediction pred = new RuntimePrediction(data);
          double predicted = pred.predictRuntimeForInstance(insta);
          System.out.println("Error: "+Math.round(runtime-predicted)+" for prediction: "+predicted);
        }
      }

    }
    catch (Exception e) {
      e.printStackTrace();
    }
    */


  }
}

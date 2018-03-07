package datasets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.boon.core.Sys;

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
import prediction_tool.SetDescription;
import prediction_tool.Simulation;
import prediction_tool.StepwiseEvaluation;
import weka.classifiers.Evaluation;
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

    //e1.createDatasetFile(100000);
    System.out.println("done 1");
    e2.createDatasetFile(100000);
    System.out.println("done 2");
    e3.createDatasetFile(100000);
    System.out.println("done 3");

     /*
        CorrelationAttributeEval correlationAttributeEval = new CorrelationAttributeEval();
        correlationAttributeEval.buildEvaluator(trainingSet);
        System.out.println(correlationAttributeEval.evaluateAttribute(0)+" "+correlationAttributeEval.evaluateAttribute(1)+" "+
            correlationAttributeEval.evaluateAttribute(2)+" "+correlationAttributeEval.evaluateAttribute(3));
        */

    List<DatasetCreator> datasetCreators = new ArrayList<DatasetCreator>();

    datasetCreators.add(a1);
    datasetCreators.add(a2);
    datasetCreators.add(a3);
    datasetCreators.add(m1);
    datasetCreators.add(m2);
    datasetCreators.add(m3);
    datasetCreators.add(i1);
    datasetCreators.add(i2);
    datasetCreators.add(i3);
    datasetCreators.add(i4);
    datasetCreators.add(i5);
    datasetCreators.add(e1);
    datasetCreators.add(e2);
    datasetCreators.add(e3);

    List<SetDescription> setDescriptions = new ArrayList<>();

    for (DatasetCreator datasetCreator : datasetCreators) {
      setDescriptions.add(
          new SetDescription(datasetCreator.getDatasetName(), setnameToARFFFilepath(datasetCreator.getDatasetName()),
              datasetCreator.getNumberOfParameters(), datasetCreator.hasNominal()));
    }

    StepwiseEvaluation.evaluatePredictorsOnSet(setDescriptions);

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

  }

  static public String setnameToARFFFilepath(String setname) {
    return "./data/arff/" + setname + ".arff";
  }
}

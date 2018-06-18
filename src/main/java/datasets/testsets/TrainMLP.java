package datasets.testsets;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import datasets.DatasetCreator;
import datasets.InputParameter;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;
import weka.core.converters.AbstractFileLoader;
import weka.core.converters.ArffLoader;
import weka.core.converters.CSVLoader;

/**
 * Created by Vanessa Ackermann on 16.06.18.
 *
 * @author Vanessa Ackermann
 * @version 1.0
 */
public class TrainMLP extends DatasetCreator {

  private static final String DATASETNAME = "TrainMLP";
  private static final int NUMBEROFPARAMETERS = 2;

  public TrainMLP() {
    super(DATASETNAME, NUMBEROFPARAMETERS);
    List<InputParameter> inputParameters = new LinkedList<InputParameter>();
    inputParameters.add(new InputParameter("numInstances", 1, 2000, true, false));
    inputParameters.add(new InputParameter("numEpochs", 1, 2000, true, false));
    this.setInputParameters(inputParameters);
  }

  public double getRuntime(Instance instance) {
    int numInstances = (int) instance.value(0);
    int numEpochs = (int) instance.value(1);

    MultilayerPerceptron mlp = new MultilayerPerceptron();
    mlp.setTrainingTime(numEpochs);

    Instances dataset = loadDatasetFromFilepath("./data/csv/Fibonacci.csv");
    Instances trainingSet = new Instances(dataset, 0, numInstances);

    long startTime = System.nanoTime();
    try {
      mlp.buildClassifier(trainingSet);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    long stopTime = System.nanoTime();
    return (stopTime - startTime);
  }

  static Instances loadDatasetFromFilepath(String filepath) {
    File file = new File(filepath);
    AbstractFileLoader loader;
    if (filepath.endsWith(".csv")) {
      loader = new CSVLoader();
    }
    else if (filepath.endsWith(".arff")) {
      loader = new ArffLoader();
    }
    else {
      System.out.println("Wrong file type :(");
      return null;
    }
    try {
      Instances dataset;
      loader.setFile(file);
      dataset = loader.getDataSet();
      dataset.setClassIndex(dataset.numAttributes() - 1);
      return dataset;
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

}
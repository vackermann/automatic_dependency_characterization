package prediction_tool;

import java.io.File;
import java.io.IOException;

import org.boon.core.Sys;

import weka.core.Instances;
import weka.core.converters.AbstractFileLoader;
import weka.core.converters.ArffLoader;
import weka.core.converters.CSVLoader;

/**
 * Created by Vanessa Ackermann on 27.02.18.
 *
 * @author Vanessa Ackermann
 * @version 1.0
 */
public class Simulation {

  private Instances dataset;
  private int datasetSize;
  private RuntimePrediction runtimePrediction;
  private int currentIndexInDataset = 0;

  public Simulation(String filename) throws IOException {
    File file = new File(filename);
    AbstractFileLoader loader;
    if (filename.endsWith(".csv")) {
      loader = new CSVLoader();
    }
    else if (filename.endsWith(".arff")) {
      loader = new ArffLoader();
    }
    else {
      System.out.println("Wrong file type :(");
      return;
    }
    loader.setFile(file);
    dataset = loader.getDataSet();
    datasetSize = dataset.size();
  }

  public void startSimulation(int numBatchInstances) {
    Instances batchDataset = new Instances(dataset, 0, numBatchInstances);
    currentIndexInDataset = numBatchInstances + 1;
    runtimePrediction = new RuntimePrediction(batchDataset);
    System.out.println("Currently chosen predictor: " + runtimePrediction.predictor.toString());
  }

  public void incrementallyAddInstances(int numInstances) {
    for (int i = currentIndexInDataset; i < numInstances + currentIndexInDataset; i++) {
      runtimePrediction.addTrainingInstance(dataset.get(i));
    }
    currentIndexInDataset += numInstances;
  }

  public void predictAfter(int numAddedValues) {
    int numInstancesToTrainBeforePrediction = numAddedValues - currentIndexInDataset;
    if (numInstancesToTrainBeforePrediction < 0) {
      System.out.println("Failed to predict this instance, because I've already learned it.");
    }
    else {
      incrementallyAddInstances(numInstancesToTrainBeforePrediction);
      int prediction = (int) runtimePrediction.predictRuntimeForInstance(dataset.get(numAddedValues + 1));
      System.out.println("Predicted: " + prediction + " --- Was: " +
          dataset.get(numAddedValues + 1).value(dataset.numAttributes() - 1));
    }
  }

}

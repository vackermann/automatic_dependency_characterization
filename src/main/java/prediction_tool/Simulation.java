package prediction_tool;

import java.io.File;
import java.io.IOException;

import weka.core.Instances;
import weka.core.converters.AbstractFileLoader;
import weka.core.converters.ArffLoader;
import weka.core.converters.CSVLoader;

/**
 * The Similation class can be used to simulate the the RuntimePrediction for an application over time (i.e., as more and
 * more monitoring instances are learned). Uses one performance behaviour data set (i.e., monitoring data), which instances
 * can be used both for training and prediction simulation.
 *
 *
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

  /**
   * Initializes Simulation with the data set to be used specified via the filename. Data set must be in CSV or Arff
   * format.
   *
   * @param filename
   *
   * @throws IOException
   */
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

  /**
   * Start Simulation by specifying the numebr of training instances (e.g., monitoring data) to be handet to the
   * RuntimePrediction object in the beginning. Equals the starting of the RuntimePrediction framework in real DML deployment.
   * @param numBatchInstances
   */
  public void startSimulation(int numBatchInstances) {
    Instances batchDataset = new Instances(dataset, 0, numBatchInstances);
    currentIndexInDataset = numBatchInstances + 1;
    runtimePrediction = new RuntimePrediction(batchDataset);
  }

  /**
   * Simulates the iterative passing of monitoring data instances to the RuntimePrediction framework.
   * @param numInstances
   */
  public void incrementallyAddInstances(int numInstances) {
    if (currentIndexInDataset + numInstances > datasetSize) {
      System.out.println(
          "Oooops. Only " + (datasetSize - currentIndexInDataset) + " Instances left. Choose any number <= this.");
      return;
    }
    for (int i = currentIndexInDataset; i < numInstances + currentIndexInDataset; i++) {
      runtimePrediction.addTrainingInstance(dataset.get(i));
    }
    currentIndexInDataset += numInstances;
  }

  /**
   * Simualtes making a prediction with the RuntimePrediction framework if numAddedValues monitoring data instances
   * are available in the RuntimePrediction object for fitting a model. Prints the prediction made by the framework in
   * contrast to the really observed value to the console.
   */
  public void predictAfter(int numAddedValues) {
    int numInstancesToTrainBeforePrediction = numAddedValues - currentIndexInDataset;
    if (numInstancesToTrainBeforePrediction < 0) {
      System.out.println("Failed to predict this instance, because I've already learned it.");
    }
    else if (numAddedValues >= datasetSize) {
      System.out.println(
          "Oooops. There are only " + datasetSize + " Instances in the dataset. Choose any number smaller than this.");
    }
    else {
      incrementallyAddInstances(numInstancesToTrainBeforePrediction);
      int prediction = (int) runtimePrediction.predictInstance(dataset.get(numAddedValues + 1));
      System.out.println("Predicted: " + prediction + " --- Was: " +
          dataset.get(numAddedValues + 1).value(dataset.numAttributes() - 1));
    }
  }

}

package prediction_tool;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import weka.classifiers.Classifier;
import weka.classifiers.functions.SMOreg;
import weka.core.Instances;
import weka.core.converters.AbstractFileLoader;
import weka.core.converters.ArffLoader;
import weka.core.converters.CSVLoader;

import weka.classifiers.Evaluation;

/**
 * Used for the overall evaluation of the MetaClassifier. Compares prediction accuracy (via mean absolute error) of
 * predictors dynamically recommended by MetaClassifier to that of support vector regression (generally "best" method)
 * with paired-sample t-test.
 * <p>
 * Created by Vanessa Ackermann on 25.03.18.
 *
 * @author Vanessa Ackermann
 * @version 1.0
 */
public class MetaClassifierEvaluation {

  static final int[] STEPS = new int[]{20, 50, 200, 700, 2000, 4500, 7500};
  static final int TESTSET_SIZE = 1000;

  /**
   * Creates an array of MAE differences for the paired-samples over all sets specified in setDesciptions.
   *
   * @param setDescriptions
   * @param seed
   *
   * @return double array of MAE differences for paired-samples
   */
  static public double[] getMAEDifferencesOnSets(List<SetDescription> setDescriptions, int seed) {
    int numSets = setDescriptions.size();
    int indexSet = 0;
    MetaClassifier metaClassifier = new MetaClassifier();
    int resultSize = STEPS.length * setDescriptions.size();
    double[] result = new double[resultSize];
    int index = 0;
    int smrCounter = 0;

    for (SetDescription setDescription : setDescriptions) {
      indexSet++;
      String name = setDescription.getName();
      String filepath = setDescription.getFilepath();

      Instances dataset = loadDatasetFromFilepath(filepath);
      dataset.randomize(new Random(seed));
      Instances testSet = new Instances(dataset, 0, TESTSET_SIZE);

      for (Integer step : STEPS) {
        System.out.println("Set " + indexSet + "/" + numSets + " (steps: " + step + ")");
        if (step > dataset.size() - TESTSET_SIZE) {
          break;
        }
        try {
          Instances trainingSet = new Instances(dataset, TESTSET_SIZE, step);

          Classifier pred1 = new SMOreg();
          Classifier pred2 = metaClassifier.predictBestPredictorForSet(trainingSet);

          double difference = 0;

          if (pred2 instanceof SMOreg) {
            smrCounter++;
          }
          else {
            Evaluation eval1 = new Evaluation(trainingSet);
            Evaluation eval2 = new Evaluation(trainingSet);

            pred1.buildClassifier(trainingSet);
            pred2.buildClassifier(trainingSet);

            eval1.evaluateModel(pred1, testSet);
            eval2.evaluateModel(pred2, testSet);

            //How much is the MAE of the MC predictor smaller than the MAE of the MAE of SVR?
            difference = eval1.meanAbsoluteError() - eval2.meanAbsoluteError();
          }
          System.out.println(difference);
          result[index] = difference;
          index++;
        }
        catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
    System.out.println(smrCounter + " SVR predicctions out of " + result.length);
    return result;
  }

  /**
   * Loads data set as Weka Instances object from given filepath. Must be csv or arff file.
   *
   * @param filepath
   *
   * @return data set in Weka's Instances format
   */
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

  /**
   * Returns mean values of all array entries.
   *
   * @param array
   *
   * @return mean
   */
  static private double getMean(double[] array) {
    double sum = 0;
    for (double d : array) {
      sum += d;
    }
    return sum / array.length;
  }

  /**
   * Returns standard deviation of all array entries.
   *
   * @param array
   *
   * @return standard deviation
   */
  static private double getStandardDeviation(double[] array) {
    return Math.sqrt(getVariance(array));
  }

  /**
   * Returns variance of all array entries.
   *
   * @param array
   *
   * @return variance
   */
  static private double getVariance(double[] array) {
    double sum = 0;
    double mean = getMean(array);
    for (double d : array) {
      sum = Math.pow(d - mean, 2);
    }
    return sum / array.length;
  }

  /**
   * Returns t-value for all entries in array. Assumes the array holds difference values for paired samples.
   *
   * @param array
   *
   * @return t-value
   */
  static public double getTValue(double[] array) {
    double nSqrt = Math.sqrt(array.length);
    double mean = getMean(array);
    double std = getStandardDeviation(array);
    System.out.println("t-value: " + nSqrt + "*(" + mean + "/" + std + ")");
    return nSqrt * (mean / std);
  }
}

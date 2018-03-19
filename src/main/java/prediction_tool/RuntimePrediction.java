package prediction_tool;

import java.util.ArrayList;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.UpdateableClassifier;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.functions.SGD;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import static weka.core.Utils.splitOptions;

/**
 * Created by Vanessa Ackermann on 14.02.18.
 *
 * @author Vanessa Ackermann
 * @version 1.0
 */
public class RuntimePrediction {

  private Instances dataset;
  public Classifier predictor;
  private boolean updateablePredictor;
  private int untrainedInstances = 0;
  private int trainedInstances = 0;
  final double TRIGGER_BATCH_LEARNING_PERCENTAGE = 0.1;
  private MetaClassifier metaClassifier = new MetaClassifier();

  /**
   * Constructor if no predictor exists / should be chosen according to the data set
   *
   * @param dataset
   *
   * @throws Exception
   */
  public RuntimePrediction(Instances dataset) {
    this.dataset = dataset;
    initialize();
  }

  public RuntimePrediction(int numberInputParameters) {
    ArrayList<Attribute> attributeList = new ArrayList<Attribute>();
    for (int i = 0; i < numberInputParameters; i++) {
      attributeList.add(new Attribute("InputParam_" + i));
    }
    Attribute labelAttribute = new Attribute("Runtime(ns)");
    attributeList.add(labelAttribute);
    Instances dataset = new Instances("AlgorithmRuntime", attributeList, 0);
    dataset.setClass(labelAttribute);
    dataset.setRelationName("AlgorithmRuntime");
    initialize();
  }

  public void initialize() {
    dataset.setClassIndex(dataset.numAttributes() - 1);
    predictor = getBestPredictor(dataset);
    batchLearning();
    updateablePredictor = (predictor instanceof UpdateableClassifier);
  }

  /**
   * Constructor if predictor already exists
   * (e.g. when getting back to a previously trained model or having a favoured prediction method to be used)
   *
   * @param dataset
   * @param predictor
   *
   * @throws Exception
   */
  public RuntimePrediction(Instances dataset, Classifier predictor) {
    this.dataset = dataset;
    this.dataset.setClassIndex(dataset.numAttributes() - 1);
    this.predictor = predictor;
    updateablePredictor = (predictor instanceof UpdateableClassifier);
  }

  private Classifier getBestPredictor(Instances dataset) {
    return metaClassifier.predictBestPredictorForSet(dataset);
  }

  public void addTrainingInstance(Instance instance) {
    instance.setDataset(dataset);
    dataset.add(instance);
    untrainedInstances++;
    /*
    if (updateablePredictor) {
      try {
        ((UpdateableClassifier) predictor).updateClassifier(instance);
        trainedInstances++;
        untrainedInstances--;
      }
      catch (Exception e) {
        System.out.println(e.getMessage());
      }
    }
    */
  }

  public void addTrainingInstance(String csv) {
    Instance instance = csvStringToInstance(csv, true);
    if (instance != null) {
      addTrainingInstance(instance);
    }
  }

  private Instance csvStringToInstance(String csv, boolean setRuntime) {
    String[] values = csv.split(",");
    int numAttributesNeeded = setRuntime ? dataset.numAttributes() : dataset.numAttributes() - 1;
    if (values.length == numAttributesNeeded) {
      Instance instance = new DenseInstance(dataset.numAttributes());
      for (int i = 0; i < values.length; i++) {
        try {
          double value = Double.parseDouble(values[i]);
          instance.setValue(i, value);
        }
        catch (NumberFormatException e) {
          System.out.println("Failed to convert this csv string to Weka instance. Value must be numberic.");
          return null;
        }
      }
      return instance;
    }
    else {
      System.out.println(
          "Failed to convert this csv string to Weka instance. Number of values does not match dataset structure.");
      return null;
    }
  }

  public void batchLearning() {
    predictor = getBestPredictor(dataset);
    try {
      predictor.buildClassifier(dataset);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    trainedInstances = dataset.size();
    untrainedInstances = 0;
  }

  public double predictRuntimeForInstance(String csv) {
    Instance instance = csvStringToInstance(csv, false);
    if (instance != null) {
      return predictRuntimeForInstance(instance);
    }
    return -1;
  }

  public double predictRuntimeForInstance(Instance instance) {
    instance.setDataset(dataset);
    if (untrainedInstances > trainedInstances * TRIGGER_BATCH_LEARNING_PERCENTAGE) {
      batchLearning();
    }
    double predictedRuntime = -1;
    try {
      System.out.println(instance.classIndex());
      predictedRuntime = predictor.classifyInstance(instance);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return predictedRuntime;
  }

}

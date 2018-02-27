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
  final boolean dynamicallyChangePredictor = false;
  private boolean updateablePredictor;
  private int untrainedInstances = 0;
  private int trainedInstances = 0;
  final double TRIGGER_BATCH_LEARNING_PERCENTAGE = 0.1;

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

  static private Classifier getBestPredictor(Instances dataset) {
    //TODO Selection Heurisitc
    /*
    Use SDG (For numeric class attributes, the squared, Huber or epsilon-insensitve loss
 * function must be used. Epsilon-insensitive and Huber loss may require a much
 * higher learning rate) --> http://book2s.com/java/src/package/weka/classifiers/functions/sgd.html
     */
    MultilayerPerceptron mlp = new MultilayerPerceptron();
    //mlp.setOptions(Utils.splitOptions("-L 0.1 -M 0.2 -N 2000 -V 0 -S 0 -E 20 -H 3"));
    //Setting Parameters
    mlp.setLearningRate(0.1);
    mlp.setMomentum(0.2);
    mlp.setTrainingTime(2000);
    mlp.setHiddenLayers("3");

    SGD sdg = new SGD();
    String[] options = new String[2];
    options[0] = "-F";
    options[1] = "2";
    try {
      sdg.setOptions(options);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return sdg;
  }

  public void addTrainingInstance(Instance instance) {
    instance.setDataset(dataset);
    dataset.add(instance);
    untrainedInstances++;
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
    try {
      Classifier auxclassifier = AbstractClassifier.makeCopy(predictor);
      auxclassifier.buildClassifier(dataset);
      predictor = auxclassifier;
      trainedInstances = dataset.size();
      untrainedInstances = 0;
    }
    catch (Exception e) {
      e.printStackTrace();
    }
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
    if (!updateablePredictor && untrainedInstances > trainedInstances * TRIGGER_BATCH_LEARNING_PERCENTAGE) {
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

  //Create instances
  //Create learner
  //Learn -> Predict -> Learn ...

  /*
  // load data
 ArffLoader loader = new ArffLoader();
 loader.setFile(new File("/some/where/data.arff"));
 Instances structure = loader.getStructure();

 // train Cobweb
 Cobweb cw = new Cobweb();
 cw.buildClusterer(structure);
 Instance current;
 while ((current = loader.getNextInstance(structure)) != null)
   cw.updateClusterer(current);
 cw.updateFinished();
   */
}

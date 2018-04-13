package prediction_tool;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
/**
 *
 * A RuntimePrediction object can be used to predict the performance behavoiour of an application via parametric
 * dependecies learned from monitoring data. All recieved monoring data instances and the current predictor are saved
 * as object variables.
 *
 * @author Vanessa Ackermann
 * @version 1.0
 */
public class RuntimePrediction {

  private Instances dataset;
  private Classifier predictor;
  private int untrainedInstances = 0;
  private int trainedInstances = 0;
  final double THRESHOLD_LEARNING = 0.1;
  private MetaClassifier metaClassifier = new MetaClassifier();

  /**
   * Constructor if no predictor exists / should be chosen according to the data set.
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
  }

  /**
   * Constructor if predictor already exists
   * (e.g. when getting back to a previously trained model or having a favoured prediction method to be used).
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
  }

  /**
   * Get the best predictor for the available data set via prediction by the meta-classifer.
   *
   * @param dataset
   *
   * @return Classifier
   */
  private Classifier getBestPredictor(Instances dataset) {
    return metaClassifier.predictBestPredictorForSet(dataset);
  }

  /**
   * Adds new monitoring data instance to training set.
   * @param instance
   */
  public void addTrainingInstance(Instance instance) {
    instance.setDataset(dataset);
    dataset.add(instance);
    untrainedInstances++;
  }

  /**
   * Adds CSV-String as monotoring data instance to training set. Values MUST be comma-seperated and numeric.
   * @param csv
   */
  public void addTrainingInstance(String csv) {
    Instance instance = csvStringToInstance(csv, true);
    if (instance != null) {
      addTrainingInstance(instance);
    }
  }

  /**
   * Transforms CSV-String to training instance, using the instance structure to the training data set.
   * @param csv
   * @param setRuntime
   * @return Instance
   */
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
          System.out.println("Failed to convert this csv string to Weka instance. Value must be numeric.");
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

  /**
   * Fits new prediction model for internal predictor. Uses prediction technique recommended by meta-classifier.
   *
   */
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

  /**
   * Predicts the performance behavoiur (e.g., runtime) on the learned application for the given CSV-instance.
   * If the amount of available, yet not-learned training instances surpasses the THRESHOLD_LEARNING, a new predictor is
   * fit to the training data prior to predicting the instance.
   *
   * @param csv
   *
   * @return Numeric performance prediction
   */
  public double predictInstance(String csv) {
    Instance instance = csvStringToInstance(csv, false);
    if (instance != null) {
      return predictInstance(instance);
    }
    return -1;
  }

  /**
   * Predicts the performance behavoiur (e.g., runtime) on the learned application for the given instance in training
   * sef format. If the amount of available, yet not-learned training instances surpasses the THRESHOLD_LEARNING, a new
   * predictor is fit to the training data prior to predicting the instance.
   *
   * @param instance
   *
   * @return Numeric performance prediction
   */
  public double predictInstance(Instance instance) {
    instance.setDataset(dataset);
    if (triggerLearning()){
      batchLearning();
    }
    double prediction = -1;
    try {
      prediction = predictor.classifyInstance(instance);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return prediction;
  }

  /**
   * Checks if the amount of available, yet not-learned training instances surpasses the THRESHOLD_LEARNING in order to
   * trigger the training of a new predictor..
   *
   * @return
   */
  private boolean triggerLearning() {
    return (untrainedInstances > trainedInstances * THRESHOLD_LEARNING);
  }

  /**
   * Helper-method (not necessary for DML-deployment). Was used for DML case study in order to create a CSV-File as
   * String that contains a list of predictons for the input CSV-File (via filepath). Each prediction belongs to the
   * respective "input parameter values" line in the input file.
   *
   * @param filepath
   *
   * @return CSV-String that contains all predictions for instances in input file
   */
  public String predictAllInFile(String filepath) {
    if (!filepath.endsWith(".csv")) {
      System.out.println("Wrong file type :(");
      return "";
    }
    StringBuffer result = new StringBuffer();
    File file = new File(filepath);

    try {
      Scanner scanner = new Scanner(file);
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine();
        int prediction = (int) predictInstance(line);
        result.append(prediction + "\n");
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return result.toString();
  }
}

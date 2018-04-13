package prediction_tool;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.REPTree;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;
import weka.core.converters.AbstractFileLoader;
import weka.core.converters.ArffLoader;
import weka.core.converters.CSVLoader;

/**
 * MetaClassifier builds a CART decision tree from the specified training set. It can be used to recommend a prediction
 * technique depending on the learned data set characteristics.
 *
 * Created by Vanessa Ackermann on 19.03.18.
 *
 * @author Vanessa Ackermann
 * @version 1.0
 */
public class MetaClassifier {

  private REPTree metaClassifier;
  private String TRAININGSET_PATH = "./evaluation/bestPredictor_total_10.csv";
  private Instances trainingSet;

  /**
   * Constructor that builds MetaClassifier object for the specified training set csv / arff file.
   */
  public MetaClassifier() {
    trainingSet = loadDatasetFromFilepath(TRAININGSET_PATH);
    metaClassifier = new REPTree();
    try {
      metaClassifier.setOptions(Utils.splitOptions("-N 3 -L 4"));
      metaClassifier.buildClassifier(trainingSet);
      System.out.print(metaClassifier.graph());
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Predicts the best prediction technique depending on characteristics of the given data set in Weka format.
   *
   * @param dataset
   *
   * @return Recommended prediction technique as Weka Classifier object
   */
  public Classifier predictBestPredictorForSet(Instances dataset) {
    try {
      int predictorNumber = (int) metaClassifier.classifyInstance(makeClassifictionInstanceFromDataset(dataset));

      return Predictors.getPredictorWithName(trainingSet.classAttribute().value(predictorNumber));
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Builds Instance that can be classified by the learned CART decision tree from the given data set.
   *
   * @param dataset
   * @return Instance
   */
  private Instance makeClassifictionInstanceFromDataset(Instances dataset) {
    Instance instance = new DenseInstance(trainingSet.numAttributes());
    instance.setDataset(trainingSet);
    instance.setValue(0, DatasetCharacteristics.getSize(dataset));
    instance.setValue(1, DatasetCharacteristics.getNumberOfInputParameters(dataset));
    instance.setValue(2, DatasetCharacteristics.getRangeOfClassAttribute(dataset));
    instance.setValue(3, DatasetCharacteristics.getCVOfClassAttribute(dataset));
    instance.setValue(4, DatasetCharacteristics.getHighestCorrelation(dataset));
    instance.setValue(5, DatasetCharacteristics.getLowestCorrelation(dataset));
    instance.setValue(6, DatasetCharacteristics.getR2ForLinReg(dataset));
    return instance;
  }

  /**
   * Loads training set for CART tree from given filepath. File must be csv or arff.
   *
   * @param filepath
   * @return Instances (training set in Weka format)
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
   * Evaluate the accuracy of the specified classification method for the MetaClassifier (e.g., CART decision tree)
   * with 10-fold cross validation on the training set. Prints evaluation results in console.
   */
  public void evaluateMethod() {
    try {
      Evaluation evaluation = new Evaluation(trainingSet);
      REPTree cls = new REPTree();
      cls.setOptions(Utils.splitOptions("-L 4"));
      evaluation.crossValidateModel(cls, trainingSet, 10, new Random(1));
      System.out.println(evaluation.toSummaryString("\nResults\n======\n", false));
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
}

package prediction_tool;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import weka.classifiers.Classifier;
import weka.classifiers.functions.LinearRegression;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.functions.SGD;
import weka.classifiers.functions.SMOreg;
import weka.classifiers.lazy.IBk;
import weka.classifiers.meta.Bagging;
import weka.classifiers.rules.ZeroR;
import weka.classifiers.trees.M5P;
import weka.classifiers.trees.REPTree;
import weka.classifiers.trees.RandomForest;
import weka.classifiers.trees.SimpleCart;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;
import weka.core.converters.AbstractFileLoader;
import weka.core.converters.ArffLoader;
import weka.core.converters.CSVLoader;
import weka.core.converters.CSVSaver;

import weka.classifiers.Evaluation;

import static javafx.scene.input.KeyCode.M;

/**
 * Provides methods to evaluate and compare the prediction performance of different Weka classifieres on multiple data sets.
 *
 * Created by Vanessa Ackermann on 27.02.18.
 *
 * @author Vanessa Ackermann
 * @version 1.0
 */
public class StepwiseEvaluation {

  static final int[] STEPS = new int[]{10, 100, 500, 1000, 3000, 6000, 9000};
  static final int TESTSET_SIZE = 1000;
  static Map<Attribute, Classifier> predictors = Predictors.getPredictorsAsMap();
  static final int RATING_PARAM_INDEX = 10; //10 = MAE, 11 = MAPE, 12 = TimeForTraining

  /**
   * Repeats the predictor evaluation on all specified data sets numIteration times, each time with a different seed
   * value for shuffling the data set. Saves csv file with evaluation results for each run. Saves csv file with all
   * bestPredictor data sets from the multiple runs.
   *
   * @param setDescriptions
   * @param numIterations
   */
  static public void evaluatePredictorsOnSetsMultipleTimes(List<SetDescription> setDescriptions, int numIterations) {
    Instances bestPredictorDataset = evaluatePredictorsOnSets(setDescriptions, 0);
    for (int seed = 1; seed < numIterations; seed++) {
      Instances currentBestPredictorDataset = evaluatePredictorsOnSets(setDescriptions, seed);
      bestPredictorDataset.addAll(currentBestPredictorDataset);
    }
    createCSVFileForDataset(bestPredictorDataset, "bestPredictor_total_" + numIterations);
  }

  /**
   * Evaluates different predictors stepwise on the sets specified on setDescriptions. Saves evaluation results in csv file.
   * Returns data set with entries that each contain the predictor that "performs best" on a certain evaluation sample,
   * together with characteristics of the sampled training set. This can be used for training the MetaClassifier.
   *
   * @param setDescriptions
   * @param seed
   * @return bestPredictor data set
   */
  static public Instances evaluatePredictorsOnSets(List<SetDescription> setDescriptions, int seed) {
    String setName = "total";
    Instances evaluationDataset = createEvaluationDataset(setName, setDescriptions);
    Instances bestPredictorDataset = createBestPredictorDataset(setName);

    int numSets = setDescriptions.size();
    int indexSet = 0;
    for (SetDescription setDescription : setDescriptions) {
      indexSet++;
      String name = setDescription.getName();
      String filepath = setDescription.getFilepath();
      int numParameter = setDescription.getNumParameter();
      int nominal = setDescription.hasNominal ? 1 : 0;

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
          List<Instance> evalInstancesForStep = new ArrayList<>();

          //calculate training set characteristics
          double size = DatasetCharacteristics.getSize(trainingSet);
          double runtimeRange = DatasetCharacteristics.getRangeOfClassAttribute(trainingSet);
          double runtimeCV = DatasetCharacteristics.getCVOfClassAttribute(trainingSet);
          double highestCorrelation = DatasetCharacteristics.getHighestCorrelation(trainingSet);
          double lowestCorrelation = DatasetCharacteristics.getLowestCorrelation(trainingSet);
          double r2LinReg = DatasetCharacteristics.getR2ForLinReg(trainingSet);

          for (Attribute predictorAttribute : predictors.keySet()) {
            Evaluation evaluation = new Evaluation(trainingSet);
            Classifier predictor = predictors.get(predictorAttribute);

            long startTime = System.nanoTime() / 1000;
            predictor.buildClassifier(trainingSet);
            long stopTime = System.nanoTime() / 1000;
            int buildTime = (int) (stopTime - startTime); //in microseconds

            double mape = getMeanAbsolutePercentageError(predictor, testSet);

            evaluation.evaluateModel(predictor, testSet);


            Instance evalInstance =
                createAndAddInstanceToEvaluationDataset(evaluationDataset, name, trainingSet.size(), numParameter,
                    nominal, runtimeRange, runtimeCV, highestCorrelation, lowestCorrelation, r2LinReg,
                    predictorAttribute.name(), evaluation.meanAbsoluteError(), mape, buildTime);
            evalInstancesForStep.add(evalInstance);
          }
          addToBestPredictorDataset(bestPredictorDataset, evalInstancesForStep, trainingSet.size(), numParameter,
              runtimeRange, runtimeCV, highestCorrelation, lowestCorrelation, r2LinReg);
        }
        catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
    createCSVFileForDataset(evaluationDataset, "eval_" + seed + "_" + setName);
    return bestPredictorDataset;
  }

  /**
   * Saves data set to csv file with the specified filename.
   * @param dataset
   * @param filename
   */
  static void createCSVFileForDataset(Instances dataset, String filename) {
    CSVSaver csvSaver = new CSVSaver();
    csvSaver.setInstances(dataset);
    File csvFile = new File("./evaluation/" + filename + ".csv");
    try {
      csvSaver.setFile(csvFile);
      csvSaver.writeBatch();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Loads data set as Weka Instances object from given filepath. Must be csv or arff file.
   * @param filepath
   * @return
   *
   *
   * @param filepath
   * @return data set in Weka's instances format
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
   * Returns penalty value for specified rating parameter (currently: MAE). This methods can be modified in order to change the meaning of
   * "performs best" for our MetaClassifier.
   * @param ratingParam
   * @return
   */
  static private double getPenalty(double ratingParam) {
    return (ratingParam);
  }

  /**
   * Return name of predictor that "performs best" on the evaluation sample.
   * @param instances
   * @return
   */
  static private String getBestPredictorName(List<Instance> instances) {
    String currentBestName = "";
    double currentBestValue = Double.MAX_VALUE;
    for (Instance instance : instances) {
      double instancePenalty = getPenalty(instance.value(RATING_PARAM_INDEX));
      if (instancePenalty < currentBestValue) {
        currentBestName = instance.stringValue(9); //Predictor name
        currentBestValue = instancePenalty;
      }
    }
    return currentBestName;
  }

  /**
   * Creates new instances with best predictor for the evaluation sample and adds it to the bestPredictor data set.
   *
   * @param bestPredictorDataset
   * @param instances
   * @param size
   * @param numParameter
   * @param runtimeRange
   * @param runtimeCV
   * @param highestCorrelation
   * @param lowestCorrelation
   * @param r2LinReg
   */
  static private void addToBestPredictorDataset(Instances bestPredictorDataset, List<Instance> instances, int size,
      int numParameter, double runtimeRange, double runtimeCV, double highestCorrelation, double lowestCorrelation,
      double r2LinReg) {
    String bestPredictorName = getBestPredictorName(instances);
    Instance instance = new DenseInstance(bestPredictorDataset.numAttributes());
    instance.setDataset(bestPredictorDataset);
    instance.setValue(0, size);
    instance.setValue(1, numParameter);
    instance.setValue(2, runtimeRange);
    instance.setValue(3, runtimeCV);
    instance.setValue(4, highestCorrelation);
    instance.setValue(5, lowestCorrelation);
    instance.setValue(6, r2LinReg);
    instance.setValue(7, bestPredictorName);
    bestPredictorDataset.add(instance);
  }

  /**
   * Creates new evaluation data set instance for given evaluation sample and adds it to evaluation data set.
   *
   * @param evaluationDatset
   * @param name
   * @param size
   * @param numParameter
   * @param hasNominal
   * @param runtimeRange
   * @param runtimeCV
   * @param highestCorrelation
   * @param lowestCorrelation
   * @param r2LinReg
   * @param predictorName
   * @param mae
   * @param mape
   * @param time
   * @return Instance
   */
  static private Instance createAndAddInstanceToEvaluationDataset(Instances evaluationDatset, String name, int size,
      int numParameter, int hasNominal, double runtimeRange, double runtimeCV, double highestCorrelation,
      double lowestCorrelation, double r2LinReg, String predictorName, double mae, double mape, int time) {
    Instance instance = new DenseInstance(evaluationDatset.numAttributes());
    instance.setDataset(evaluationDatset);
    instance.setValue(0, name);
    instance.setValue(1, size);
    instance.setValue(2, numParameter);
    instance.setValue(3, hasNominal);
    instance.setValue(4, runtimeRange);
    instance.setValue(5, runtimeCV);
    instance.setValue(6, highestCorrelation);
    instance.setValue(7, lowestCorrelation);
    instance.setValue(8, r2LinReg);
    instance.setValue(9, predictorName);
    instance.setValue(10, mae);
    instance.setValue(11, mape);
    instance.setValue(12, time);
    evaluationDatset.add(instance);
    return instance;
  }

  /**
   * Creates empty data set for training the MetaClassifier. Contains the following columns ("attributes"): Size (of training set),
   * numParam (number of observed input parameters in data set), runtimeRange (range of observed values for target variable),
   * runtimeCV (coefficient of determination of target variable), highestCorrelation (between any input parameter and target variable),
   * lowestCorrelation (between any input parameter and target variable), R2LinReg (coefficient of determination of
   * least-squared linear regression model for training set), predictor (that had least MAE for this training set).
   * @param name of best predictor
   * @return empty data set in Weka's Instances format
   */
  static private Instances createBestPredictorDataset(String name) {
    List<String> predictorNames = new ArrayList<String>();
    for (Attribute predictorAttribute : predictors.keySet()) {
      predictorNames.add(predictorAttribute.name());
    }
    ArrayList<Attribute> attributeList = new ArrayList<Attribute>();

    Attribute size = new Attribute("Size"); //Index 0
    attributeList.add(size);
    Attribute numParam = new Attribute("NumParam"); //Index 1
    attributeList.add(numParam);
    Attribute runtimeRange = new Attribute("RuntimeRange"); //Index 2
    attributeList.add(runtimeRange);
    Attribute runtimeCV = new Attribute("RuntimeCV"); //Index 3
    attributeList.add(runtimeCV);
    Attribute highestCorrelation = new Attribute("HighestCorrelation"); //Index 4
    attributeList.add(highestCorrelation);
    Attribute lowestCorrelation = new Attribute("LowestCorrelation"); //Index 5
    attributeList.add(lowestCorrelation);
    Attribute r2LinReg = new Attribute("R2LinReg"); //Index 6
    attributeList.add(r2LinReg);
    Attribute pred = new Attribute("Predictor", predictorNames); //Index 7
    attributeList.add(pred);
    Instances bestPredictorDataset = new Instances("bestPredictors_" + name, attributeList, 0);

    return bestPredictorDataset;
  }

  /**
   * Creates empty data set for evaluation result entries. Contains the following columns ("attributes"): Name (of data set),
   * size (of training set), numParam (number of observed input parameters in data set), hasNominal (data set has at least
   * one nominal attribute as input paramteter, e.g., ComputationMode in Fibonacci), runtimeRange (range of observed values
   * for target variable), runtimeCV (coefficient of determination of target variable), highestCorrelation (between any input
   * parameter and target variable), lowestCorrelation (between any input parameter and target variable), R2LinReg (coefficient
   * of determination of least-squared linear regression model for training set), predictor, MAE, MAPE, time (for fitting a model).
   * @param name
   * @param setDescriptions
   * @return empty data set in Weka's Instances format
   */
  static private Instances createEvaluationDataset(String name, List<SetDescription> setDescriptions) {
    List<String> setNames = new ArrayList<String>();
    for (SetDescription setDescription : setDescriptions) {
      setNames.add(setDescription.getName());
    }
    List<String> predictorNames = new ArrayList<String>();
    for (Attribute predictorAttribute : predictors.keySet()) {
      predictorNames.add(predictorAttribute.name());
    }
    ArrayList<Attribute> attributeList = new ArrayList<Attribute>();
    Attribute setName = new Attribute("Name", setNames); //Index 0
    attributeList.add(setName);
    Attribute size = new Attribute("Size"); //Index 1
    attributeList.add(size);
    Attribute numParam = new Attribute("NumParam"); //Index 2
    attributeList.add(numParam);
    Attribute hasNominal = new Attribute("HasNominal"); //Index 3
    attributeList.add(hasNominal);
    Attribute runtimeRange = new Attribute("RuntimeRange"); //Index 4
    attributeList.add(runtimeRange);
    Attribute runtimeCV = new Attribute("RuntimeCV"); //Index 5
    attributeList.add(runtimeCV);
    Attribute highestCorrelation = new Attribute("HighestCorrelation"); //Index 6
    attributeList.add(highestCorrelation);
    Attribute lowestCorrelation = new Attribute("LowestCorrelation"); //Index 7
    attributeList.add(lowestCorrelation);
    Attribute r2LinReg = new Attribute("R2LinReg"); //Index 8
    attributeList.add(r2LinReg);
    Attribute pred = new Attribute("Predictor", predictorNames); //Index 9
    attributeList.add(pred);
    Attribute mae = new Attribute("MAE"); //Index 10
    attributeList.add(mae);
    Attribute mape = new Attribute("MAPE"); //Index 11
    attributeList.add(mape);
    Attribute time = new Attribute("Time"); //Index 12
    attributeList.add(time);
    Instances evaluationDataset = new Instances("evaluation_" + name, attributeList, 0);

    return evaluationDataset;
  }

  /**
   * Returns mean absolute percentage error (MAPE) of trained predictor on the given test set.
   * @param predictor
   * @param testSet
   * @return mean absolute percentage error (MAPE)
   */
  static double getMeanAbsolutePercentageError(Classifier predictor, Instances testSet) {
    int setSize = testSet.size();
    double totalRelativeError = 0;
    for (int i = 0; i < setSize; i++) {
      double prediction = 0;
      try {
        prediction = predictor.classifyInstance(testSet.get(i));
      }
      catch (Exception e) {
        e.printStackTrace();
      }
      double observation = testSet.get(i).value(testSet.classAttribute());
      totalRelativeError += Math.abs((prediction - observation) / observation);
    }
    return (totalRelativeError / setSize) * 100;
  }
}

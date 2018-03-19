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

/*

* @startuml

* car --|> wheel


* @enduml

*/

/**
 * Created by Vanessa Ackermann on 27.02.18.
 *
 * @author Vanessa Ackermann
 * @version 1.0
 */
public class StepwiseEvaluation {

  static final int[] STEPS = new int[]{10, 100}; //{10, 100, 500, 1000, 3000, 6000, 9000}
  static final int TESTSET_SIZE = 1000;
  static Map<Attribute, Classifier> predictors = Predictors.getPredictorsAsMap();
  static final int RATING_PARAM_INDEX = 10; //10 = MAE, 11 = MAPE, 12 = TimeForTraining

  public StepwiseEvaluation() {
  }

  static public void evaluatePredictorsOnSetsMultipleTimes(List<SetDescription> setDescriptions, int numIterations) {
    Instances bestPredictorDataset = evaluatePredictorsOnSets(setDescriptions, 0);
    for (int seed = 1; seed < numIterations; seed++) {
      Instances currentBestPredictorDataset = evaluatePredictorsOnSets(setDescriptions, seed);
      bestPredictorDataset.addAll(currentBestPredictorDataset);
    }
    createCSVFileForDataset(bestPredictorDataset, "bestPredictor_total_" + numIterations);
  }

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

          //calculate trainin set charactersitics
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
            //System.out.println(mape + predictorAttribute.name());

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
    //createCSVFileForDataset(bestPredictorDataset, "bestPredictor_" + setName);
  }

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



  static private double getPenalty(double ratingParam) {
    return (ratingParam);
  }

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

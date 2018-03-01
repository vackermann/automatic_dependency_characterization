package prediction_tool;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

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

import static javax.management.Query.value;

/**
 * Created by Vanessa Ackermann on 27.02.18.
 *
 * @author Vanessa Ackermann
 * @version 1.0
 */
public class StepwiseEvaluation {

  static final long SEED = 1;
  static final int[] STEPS = new int[]{10, 100, 1000, 9000, 90000};
  static final int TESTSET_SIZE = 1000;
  static Map<Attribute, Classifier> predictors = getPredictorsForEvaluation();
  static final int RATING_PARAM_INDEX = 7; //6 = Correlation, 7 = RRSE, 8 = REA

  public StepwiseEvaluation() {
  }

  static public void evaluatePredictorsOnSet(List<SetDescription> setDescriptions) {
    String setName = "total";
    Instances evaluationDataset = createEvaluationDataset(setName, setDescriptions);
    Instances bestPredictorDataset = createBestPredictorDataset(setName);

    for (SetDescription setDescription : setDescriptions) {
      String name = setDescription.getName();
      String filepath = setDescription.getFilepath();
      int numParameter = setDescription.getNumParameter();
      int nominal = setDescription.hasNominal ? 1 : 0;

      Instances dataset = loadDatasetFromFilepath(filepath);
      Instances testSet = new Instances(dataset, 0, TESTSET_SIZE);

      for (Integer step : STEPS) {
        if (step > dataset.size() - 1000) {
          break;
        }
        try {
          Instances trainingSet = new Instances(dataset, 1000, step);
          double r2 = 0;
          List<Instance> evalInstancesForStep = new ArrayList<>();

          for (Attribute predictorAttribute : predictors.keySet()) {
            Evaluation evaluation = new Evaluation(trainingSet);
            Classifier predictor = predictors.get(predictorAttribute);

            long startTime = System.nanoTime() / 1000;
            predictor.buildClassifier(testSet);
            long stopTime = System.nanoTime() / 1000;
            int buildTime = (int) (stopTime - startTime); //in microseconds

            evaluation.evaluateModel(predictor, trainingSet);

            Instance evalInstance =
                createAndAddInstanceToEvaluationDataset(evaluationDataset, name, trainingSet.size(), numParameter,
                    nominal, r2, predictorAttribute.name(), evaluation.correlationCoefficient(),
                    evaluation.rootRelativeSquaredError(), evaluation.relativeAbsoluteError(), buildTime);
            evalInstancesForStep.add(evalInstance);
          }
          addToBestPredictorDataset(bestPredictorDataset, evalInstancesForStep, trainingSet.size(), numParameter,
              nominal, r2);
        }
        catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
    createCSVFileForDataset(evaluationDataset, "eval_" + setName);
    createCSVFileForDataset(bestPredictorDataset, "bestPredictor_" + setName);
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
      dataset.randomize(new Random(SEED));
      return dataset;
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  static private HashMap<Attribute, Classifier> getPredictorsForEvaluation() {
    HashMap<Attribute, Classifier> predictors = new HashMap<Attribute, Classifier>();
    ZeroR zer = new ZeroR();
    MultilayerPerceptron mlp = new MultilayerPerceptron();
    MultilayerPerceptron mlp2 = new MultilayerPerceptron();
    MultilayerPerceptron mlp3 = new MultilayerPerceptron();
    MultilayerPerceptron mlp4 = new MultilayerPerceptron();
    SGD sgd = new SGD();
    RandomForest rdf = new RandomForest();
    SMOreg smo = new SMOreg();
    LinearRegression lir = new LinearRegression();
    SimpleCart sca = new SimpleCart();
    IBk ibk = new IBk();
    Bagging bag = new Bagging();
    M5P m5p = new M5P();
    REPTree rep = new REPTree();

    try {
      //highest accuracy, but super low
      mlp.setOptions(Utils.splitOptions("-L 0.1 -M 0.2 -N 2000 -V 0 -S 0 -E 20 -H 3"));
      //highest accuracy, speed scales with #attributes
      mlp2.setOptions(Utils.splitOptions("-L 0.1 -M 0.2 -N 2000 -V 0 -S 0 -E 20"));
      //ok accuracy
      mlp3.setOptions(Utils.splitOptions("-L 0.1 -M 0.2 -N 500 -V 0 -S 0 -E 20"));
      //ok accuracy, even faster
      mlp3.setOptions(Utils.splitOptions("-L 0.3 -M 0.2 -N 500 -V 0 -S 0 -E 20"));
      sgd.setOptions(Utils.splitOptions("-F 2"));
    }
    catch (Exception e) {
      e.printStackTrace();
    }

    //MLPs with different configs
        /*predictors.put(new Attribute("MLP"), mlp);
    predictors.put(new Attribute("MLP2"), mlp2);
    predictors.put(new Attribute("MLP3"), mlp3);
    predictors.put(new Attribute("MLP4"), mlp4); */

    predictors.put(new Attribute("ZeroR"), zer);
    predictors.put(new Attribute("MLP"), mlp2);
    predictors.put(new Attribute("SGD"), sgd);
    predictors.put(new Attribute("RandomF"), rdf);
    predictors.put(new Attribute("SVM"), smo);
    predictors.put(new Attribute("LinReg"), lir);
    //predictors.put(new Attribute("CART"), sca);
    predictors.put(new Attribute("IBk"), ibk);
    predictors.put(new Attribute("Bagging"), bag);
    predictors.put(new Attribute("M5P"), m5p);
    predictors.put(new Attribute("REPTree"), rep);

    return predictors;
  }

  static private double getPenalty(double ratingParam) {
    //System.out.println(rrse+ " - "+(time/size)+" - "+name);
    return (ratingParam);
  }

  static private String getBestPredictorName(List<Instance> instances) {
    String currentBestName = "";
    double currentBestValue = Double.MAX_VALUE;
    for (Instance instance : instances) {
      double instancePenalty = getPenalty(instance.value(RATING_PARAM_INDEX));
      if (instancePenalty < currentBestValue) {
        currentBestName = instance.stringValue(5);
        currentBestValue = instancePenalty;
      }
    }
    return currentBestName;
  }

  static private void addToBestPredictorDataset(Instances bestPredictorDataset, List<Instance> instances, int size,
      int numParameter, int hasNominal, double r2) {
    String bestPredictorName = getBestPredictorName(instances);
    Instance instance = new DenseInstance(bestPredictorDataset.numAttributes());
    instance.setDataset(bestPredictorDataset);
    instance.setValue(0, size);
    instance.setValue(1, numParameter);
    instance.setValue(2, hasNominal);
    instance.setValue(3, r2);
    instance.setValue(4, bestPredictorName);
    bestPredictorDataset.add(instance);
  }

  static private Instance createAndAddInstanceToEvaluationDataset(Instances evaluationDatset, String name, int size,
      int numParameter, int hasNominal, double r2, String predictorName, double correlation, double rrse, double rea,
      int time) {
    Instance instance = new DenseInstance(evaluationDatset.numAttributes());
    instance.setDataset(evaluationDatset);
    instance.setValue(0, name);
    instance.setValue(1, size);
    instance.setValue(2, numParameter);
    instance.setValue(3, hasNominal);
    instance.setValue(4, r2);
    instance.setValue(5, predictorName);
    instance.setValue(6, correlation);
    instance.setValue(7, rrse);
    instance.setValue(8, rea);
    instance.setValue(9, time);
    evaluationDatset.add(instance);
    return instance;
  }

  static private Instances createBestPredictorDataset(String name) {
    List<String> predictorNames = new ArrayList<String>();
    for (Attribute predictorAttribute : predictors.keySet()) {
      predictorNames.add(predictorAttribute.name());
    }
    ArrayList<Attribute> attributeList = new ArrayList<Attribute>();
    Attribute trainingsetSize = new Attribute("Size"); //Index 0
    attributeList.add(trainingsetSize);
    Attribute numParam = new Attribute("NumParam"); //Index 1
    attributeList.add(numParam);
    Attribute hasNominal = new Attribute("HasNominal"); //Index 2
    attributeList.add(hasNominal);
    Attribute r2 = new Attribute("R2"); //Index 3
    attributeList.add(r2);
    Attribute pred = new Attribute("Predictor", predictorNames); //Index 4
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
    Attribute r2 = new Attribute("R2"); //Index 4
    attributeList.add(r2);
    Attribute pred = new Attribute("Predictor", predictorNames); //Index 5
    attributeList.add(pred);
    Attribute correlation = new Attribute("Correlation"); //Index 6
    attributeList.add(correlation);
    Attribute rrse = new Attribute("RRSE"); //Index 7
    attributeList.add(rrse);
    Attribute rea = new Attribute("REA"); //Index 8
    attributeList.add(rea);
    Attribute time = new Attribute("Time"); //Index 9
    attributeList.add(time);
    Instances evaluationDataset = new Instances("evaluation_" + name, attributeList, 0);

    return evaluationDataset;
  }

  static private Instances createRRSEDataset(String name) {
    ArrayList<Attribute> attributeList = new ArrayList<Attribute>();
    Attribute size = new Attribute("Size");
    attributeList.add(size);
    for (Attribute predictorAttribute : predictors.keySet()) {
      attributeList.add(predictorAttribute);
    }
    Instances rrseDataset = new Instances("rrse_" + name, attributeList, 0);

    return rrseDataset;
  }

}

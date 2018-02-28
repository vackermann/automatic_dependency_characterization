package prediction_tool;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
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

/**
 * Created by Vanessa Ackermann on 27.02.18.
 *
 * @author Vanessa Ackermann
 * @version 1.0
 */
public class StepwiseEvaluation {

  String name;
  Instances dataset;
  final long SEED = 1;
  final int testSetSize = 1000;
  int[] evaluationSteps = new int[]{10, 100, 1000, 9000};

  public StepwiseEvaluation(String filepath, String name) throws IOException {
    this.name = name;
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
      return;
    }
    loader.setFile(file);
    dataset = loader.getDataSet();
    dataset.setClassIndex(dataset.numAttributes() - 1);
    dataset.randomize(new Random(SEED));
  }

  public void evaluate() {
    Instances testSet = new Instances(dataset, 0, testSetSize);
    Map<Attribute, Classifier> predictors = getPredictorsForEvaluation();

    /*

    ArrayList<Attribute> attributeList = new ArrayList<Attribute>();
    Attribute trainingsetSize = new Attribute("Trainingset_size");
    //attributeList.add(trainingsetSize);
    Attribute dataCorrelation = new Attribute("Correlation");
    //attributeList.add(dataCorrelation);
    for (Attribute predictorAttribute: predictors.keySet()) {
      attributeList.add(predictorAttribute);
    }
    Instances rootMeanSquaredErrorDataset = new Instances("rootMeanSquaredError_"+name, attributeList, 0);

    */

    List<String> predictorNames = new ArrayList<String>();
    for (Attribute predictorAttribute : predictors.keySet()) {
      predictorNames.add(predictorAttribute.name());
    }

    ArrayList<Attribute> attributeList = new ArrayList<Attribute>();
    Attribute trainingsetSize = new Attribute("Size");
    attributeList.add(trainingsetSize);
    Attribute pred = new Attribute("Predictor", predictorNames);
    attributeList.add(pred);
    Attribute correlation = new Attribute("Correlation");
    attributeList.add(correlation);
    Attribute rrse = new Attribute("RRSE");
    attributeList.add(rrse);
    Attribute time = new Attribute("Time");
    attributeList.add(time);
    Instances rrse_data = new Instances("rrse_" + name, attributeList, 0);

    for (Integer step : evaluationSteps) {
      if (step > dataset.size() - 1000) {
        break;
      }
      try {
        Instances trainingSet = new Instances(dataset, 1000, step);
        /*
        Instance rootMeanSquaredErrorInstance = new DenseInstance(rootMeanSquaredErrorDataset.numAttributes());
        rootMeanSquaredErrorInstance.setValue(0, trainingSet.size());
        rootMeanSquaredErrorInstance.setValue(1, evaluation.correlationCoefficient());
        */

        for (Attribute predictorAttribute : predictors.keySet()) {
          Evaluation evaluation = new Evaluation(trainingSet);

          Classifier predictor = predictors.get(predictorAttribute);
          long startTime = System.nanoTime() / 1000;
          //TODO
          predictor.buildClassifier(testSet);
          long stopTime = System.nanoTime() / 1000;
          int buildTime = (int) (stopTime - startTime);

          evaluation.evaluateModel(predictor, trainingSet);
          //Round value to next integer -> undo for better accuracy
          //rootMeanSquaredErrorInstance.setValue(predictorAttribute, (int) evaluation.rootRelativeSquaredError());

          Instance rrse_instance = new DenseInstance(rrse_data.numAttributes());
          rrse_instance.setDataset(rrse_data);
          rrse_instance.setValue(0, trainingSet.size());
          rrse_instance.setValue(1, predictorAttribute.name());
          rrse_instance.setValue(2, evaluation.correlationCoefficient());
          rrse_instance.setValue(3, evaluation.rootRelativeSquaredError());
          rrse_instance.setValue(4, buildTime);
          rrse_data.add(rrse_instance);
        }
        //rootMeanSquaredErrorDataset.add(rootMeanSquaredErrorInstance);

        /*
        CorrelationAttributeEval correlationAttributeEval = new CorrelationAttributeEval();
        correlationAttributeEval.buildEvaluator(trainingSet);
        System.out.println(correlationAttributeEval.evaluateAttribute(0)+" "+correlationAttributeEval.evaluateAttribute(1)+" "+
            correlationAttributeEval.evaluateAttribute(2)+" "+correlationAttributeEval.evaluateAttribute(3));
        */
      }
      catch (Exception e) {
        e.printStackTrace();
      }

    }
    CSVSaver csvSaver = new CSVSaver();
    //csvSaver.setInstances(rootMeanSquaredErrorDataset);
    csvSaver.setInstances(rrse_data);
    File csvFile = new File("./evaluation/eval_" + name + ".csv");
    try {
      csvSaver.setFile(csvFile);
      csvSaver.writeBatch();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  private HashMap<Attribute, Classifier> getPredictorsForEvaluation() {
    HashMap<Attribute, Classifier> predictors = new HashMap<Attribute, Classifier>();
    ZeroR zer = new ZeroR();
    MultilayerPerceptron mlp = new MultilayerPerceptron();
    SGD sdg = new SGD();
    RandomForest rdf = new RandomForest();
    SMOreg smo = new SMOreg();
    LinearRegression lir = new LinearRegression();
    SimpleCart sca = new SimpleCart();
    IBk ibk = new IBk();
    Bagging bag = new Bagging();
    M5P m5p = new M5P();
    REPTree rep = new REPTree();

    try {
      mlp.setOptions(Utils.splitOptions("-L 0.1 -M 0.2 -N 2000 -V 0 -S 0 -E 20 -H 3"));
      sdg.setOptions(Utils.splitOptions("-F 2"));
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    predictors.put(new Attribute("ZeroR"), zer);
    predictors.put(new Attribute("MLP"), mlp);
    predictors.put(new Attribute("SDG"), sdg);
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

}

package prediction_tool;

import java.util.HashMap;

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
import weka.core.Utils;

/**
 * Created by Vanessa Ackermann on 19.03.18.
 *
 * @author Vanessa Ackermann
 * @version 1.0
 */
public class Predictors {

  static HashMap<Attribute, Classifier> getPredictorsAsMap() {
    HashMap<Attribute, Classifier> predictors = new HashMap<Attribute, Classifier>();

    ZeroR zer = new ZeroR();
    MultilayerPerceptron mlp = new MultilayerPerceptron();
    SGD sgd = new SGD();
    RandomForest rdf = new RandomForest();
    SMOreg smo = new SMOreg();
    LinearRegression lir = new LinearRegression();
    IBk ibk = new IBk();
    Bagging bag = new Bagging();
    M5P m5p = new M5P();
    REPTree rep = new REPTree();

    try {
      lir.setOptions(Utils.splitOptions("-S 1"));
      sgd.setOptions(Utils.splitOptions("-F 4"));
      //smo.setOptions(Utils.splitOptions(""));
      mlp.setOptions(Utils.splitOptions("-L 0.1 -M 0.2 -N 2000 -V 0 -S 0 -E 20"));
      //rep.setOptions(Utils.splitOptions(""));
      //m5p.setOptions(Utils.splitOptions(""));
      ibk.setOptions(Utils.splitOptions("-K 5 -X -I"));
      bag.setOptions(Utils.splitOptions("-I 25"));
      rdf.setOptions(Utils.splitOptions("-I 250"));

    }
    catch (Exception e) {
      e.printStackTrace();
    }

    predictors.put(new Attribute("ZeroR"), zer);
    predictors.put(new Attribute("ANN"), mlp);
    predictors.put(new Attribute("SGD"), sgd);
    predictors.put(new Attribute("RandomF"), rdf);
    predictors.put(new Attribute("SVR"), smo);
    predictors.put(new Attribute("LinReg"), lir);
    predictors.put(new Attribute("kNN"), ibk);
    predictors.put(new Attribute("Bagging"), bag);
    predictors.put(new Attribute("M5"), m5p);
    predictors.put(new Attribute("CART"), rep);

    return predictors;
  }

  static Classifier getPredictorWithName(String predictorName) {
    HashMap<Attribute, Classifier> predictorMap = getPredictorsAsMap();
    for (Attribute attribute : predictorMap.keySet()) {
      if (attribute.name().equals(predictorName)) {
        return predictorMap.get(attribute);
      }
    }
    System.out.println("Something went terribly wrong. Could not find a predictor with this name :(");
    return null;
  }

}

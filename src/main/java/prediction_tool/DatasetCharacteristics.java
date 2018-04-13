package prediction_tool;

import weka.attributeSelection.CorrelationAttributeEval;
import weka.classifiers.functions.LinearRegression;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.Utils;
import weka.experiment.Stats;

/**
 * Collection of methods to calculate charactersitics of a data set in Weka's Instances format.
 *
 * Created by Vanessa Ackermann on 19.03.18.
 *
 * @author Vanessa Ackermann
 * @version 1.0
 */
public class DatasetCharacteristics {

  /**
   * Returns number of data set entries, a.k.a. data set size.
   *
   * @param dataset
   *
   * @return size
   */
  static double getSize(Instances dataset) {
    return dataset.size();
  }

  static double getNumberOfInputParameters(Instances dataset) {
    return dataset.numAttributes() - 1;
  }

  static double getRangeOfClassAttribute(Instances dataset) {
    Stats numericStats = dataset.attributeStats(dataset.classIndex()).numericStats;
    return numericStats.max - numericStats.min;
  }

  /**
   * Returns mean value of target attribute (e.g., measured runtime, utilization).
   *
   * @param dataset
   * @return target attribute mean
   */
  static double getMeanOfClassAttribute(Instances dataset) {
    Stats numericStats = dataset.attributeStats(dataset.classIndex()).numericStats;
    return numericStats.mean;
  }

  /**
   * Returns value of coefficient of variance of target attribute (e.g., measured runtime, utilization).
   *
   * @param dataset
   * @return coefficient of variance of target attribute
   */
  static double getCVOfClassAttribute(Instances dataset) {
    Stats numericStats = dataset.attributeStats(dataset.classIndex()).numericStats;
    return numericStats.stdDev / numericStats.mean;
  }

  /**
   * Returns value of highest correlation between an input parameter (e.g., ArraySize) and target attribute
   * (e.g., measured runtime, utilization). Uses Pearson's correlation coefficient.
   *
   * @param dataset
   * @return highest correlation value (between -1 and 1).
   */
  static double getHighestCorrelation(Instances dataset) {
    double currentMax = -1;
    CorrelationAttributeEval correlationAttributeEval = new CorrelationAttributeEval();
    try {
      correlationAttributeEval.buildEvaluator(dataset);
      for (int i = 0; i < dataset.numAttributes() - 1; i++) {
        double correlation = correlationAttributeEval.evaluateAttribute(i);
        if (correlation > currentMax) {
          currentMax = correlation;
        }
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return currentMax;
  }

  /**
   * Returns value of lowest correlation between an input parameter (e.g., ArraySize) and target attribute
   * (e.g., measured runtime, utilization). Uses Pearson's correlation coefficient.
   *
   * @param dataset
   * @return lowest correlation value (between -1 and 1).
   */
  static double getLowestCorrelation(Instances dataset) {
    double currentMin = 1;
    CorrelationAttributeEval correlationAttributeEval = new CorrelationAttributeEval();
    try {
      correlationAttributeEval.buildEvaluator(dataset);
      for (int i = 0; i < dataset.numAttributes() - 1; i++) {
        double correlation = correlationAttributeEval.evaluateAttribute(i);
        if (correlation < currentMin) {
          currentMin = correlation;
        }
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return currentMin;
  }

  /**
   * Returns coefficient of determination R^2 of least-square linear regression model for data set. Indicates multiple
   * correlation coefficient between input parameters and target attribute.
   * @param dataset
   * @return
   */
  static double getR2ForLinReg(Instances dataset) {
    double mean = getMeanOfClassAttribute(dataset);
    LinearRegression l = new LinearRegression();
    try {
      l.setOptions(Utils.splitOptions("-S 1"));
      l.buildClassifier(dataset);
      double upperValue = 0;
      double lowerValue = 0;

      for (int i = 0; i < dataset.size(); i++) {
        upperValue += Math.pow(mean - l.classifyInstance(dataset.get(i)), 2);
        lowerValue += Math.pow(mean - dataset.get(i).value(dataset.numAttributes() - 1), 2);
      }
      return upperValue / lowerValue;
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return -1;
  }

}

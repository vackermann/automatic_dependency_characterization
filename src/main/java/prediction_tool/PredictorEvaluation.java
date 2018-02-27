package prediction_tool;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

/**
 * Created by Vanessa Ackermann on 14.02.18.
 *
 * @author Vanessa Ackermann
 * @version 1.0
 */
public class PredictorEvaluation {

  public PredictorEvaluation() {
  }

  static public String evaluatePredictor(Classifier predictor, List<File> arffFiles) {
    StringBuffer fullEvaluationReport = new StringBuffer();
    for (File arffFile : arffFiles) {
      fullEvaluationReport.append(evaluatePredictorForDataset(predictor, arffFile));
    }
    return fullEvaluationReport.toString();
  }

  static private String evaluatePredictorForDataset(Classifier predictor, File arffFile) {
    StringBuffer evaluationReport = new StringBuffer();
    ArffLoader loader = new ArffLoader();
    System.out.println("Start eval for " + arffFile.getName());
    try {
      loader.setFile(arffFile);
      Instances data = loader.getStructure();
      data.setClassIndex(data.numAttributes() - 1);
      Instance insta = loader.getNextInstance(data);
      for (int i = 1; i <= 10000; i++) {
        data.add(loader.getNextInstance(data));
        if (i % 1000 == 0) {
          Evaluation eval = new Evaluation(data);
          eval.crossValidateModel(predictor, data, 10, new Random());
          evaluationReport.append(predictor.toString() + "\n" + eval.toSummaryString());
        }
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return evaluationReport.toString();
  }

  static public List<File> getFileListFromDatasetNames(List<String> datasetNames) {
    List arffFiles = new ArrayList<File>();
    for (String datasetName : datasetNames) {
      arffFiles.add(new File("./data/arff/" + datasetName + ".arff"));
    }
    return arffFiles;
  }

}

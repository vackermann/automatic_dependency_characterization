package prediction_tool;

import java.io.File;
import java.io.IOException;

import weka.classifiers.Classifier;
import weka.classifiers.trees.REPTree;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;
import weka.core.converters.AbstractFileLoader;
import weka.core.converters.ArffLoader;
import weka.core.converters.CSVLoader;

/**
 * Created by Vanessa Ackermann on 19.03.18.
 *
 * @author Vanessa Ackermann
 * @version 1.0
 */
public class MetaClassifier {

  private REPTree metaClassifier;
  private String TRAININGSET_PATH = "./evaluation/bestPredictor_total_5.csv";
  private Instances trainingSet;

  public MetaClassifier() {
    trainingSet = loadDatasetFromFilepath(TRAININGSET_PATH);
    metaClassifier = new REPTree();
    try {
      metaClassifier.setOptions(Utils.splitOptions("-N 5"));
      metaClassifier.buildClassifier(trainingSet);
      System.out.print(metaClassifier.graph());
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  public Classifier predictBestPredictorForSet(Instances dataset) {
    try {
      int predictorNumber = (int) metaClassifier.classifyInstance(makeClassifictionInstanceFromDataset(dataset));

      System.out.println(trainingSet.classAttribute().value(predictorNumber));
      return Predictors.getPredictorWithName(trainingSet.classAttribute().value(predictorNumber));
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

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
}

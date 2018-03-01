package datasets;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.boon.core.Sys;
import org.boon.di.In;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVSaver;
import weka.core.Instances;

/**
 * DatasetCreator is the abstract base class for all application runtime measurements.
 * It creates a dataset object in the common Weka Instances format and fills it with equidistant measurement points.
 * The final datasets will be saved in .arff and .csv file format in the /data/ directory under the given dataset name.
 * <p>
 * Created by Vanessa Ackermann on 18.01.18.
 *
 * @author Vanessa Ackermann
 * @version 1.0
 */
public abstract class DatasetCreator {

  private String datasetName;
  private int numberOfParameters;
  private boolean hasNominal;
  private List<InputParameter> inputParameters;
  private static final String OUTPUTNAME = "runtime";

  /**
   * Abstract constructor that initializes the dataset name and directly takes all input parameters as objects.
   *
   * @param datasetName
   *     name of dataset
   * @param inputParameters
   *     numeric input parameters that influence the applications runtime
   */
  public DatasetCreator(String datasetName, List<InputParameter> inputParameters) {
    this.datasetName = datasetName;
    this.numberOfParameters = inputParameters.size();
    this.inputParameters = inputParameters;
    this.hasNominal = false;
    for (InputParameter inputParameter : inputParameters) {
      if (inputParameter.isNominal) {
        this.hasNominal = true;
      }
    }
  }

  /**
   * Abstract constructor that initializes the dataset name and creates [numberOfParametes] default numeric input parameters.
   *
   * @param datasetName
   *     name of dataset
   * @param numberOfParameters
   *     number of input parameters that influence the applications runtime
   */
  public DatasetCreator(String datasetName, int numberOfParameters) {
    this.datasetName = datasetName;
    this.numberOfParameters = numberOfParameters;
    this.inputParameters = new ArrayList<InputParameter>();
    for (int i = 0; i < numberOfParameters; i++) {
      inputParameters.add(new InputParameter("inputParam" + i, 0, 1000, true, false));
    }
  }

  /**
   * Creates and saves dataset with the given size. Uses equidistant measurement points for the given input parameters.
   * Measures runtime of the application method in the implementing class.
   * Saves dataset to .arff and .csv file under /data/[arff/csv]/[datasetName].
   *
   * @param size
   *     number of entries in dataset
   */
  public void createDatasetFile(int size) {
    Instances dataset = this.createDataset(size, getDatasetName(), getInputParameters(), OUTPUTNAME);
    createEquidistantInputInstances(dataset, size);
    fillDatasetWithClassValues(dataset);
    ArffSaver arffSaver = new ArffSaver();
    CSVSaver csvSaver = new CSVSaver();
    arffSaver.setInstances(dataset);
    csvSaver.setInstances(dataset);
    File arffFile = new File("./data/arff/" + getDatasetName() + ".arff");
    File csvFile = new File("./data/csv/" + getDatasetName() + ".csv");
    try {
      arffSaver.setFile(arffFile);
      csvSaver.setFile(csvFile);
      arffSaver.writeBatch();
      csvSaver.writeBatch();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Creates empty dataset in Wekas Instances format and sets relation name to given dataset name.
   * Adds all input parameters as numeric attributes and uses their names as attribute names.
   * Adds class attribute with labelName.
   * Total number of attributes in dataset is one more than number of input parameters.
   *
   * @param size
   *     number of entries in dataset
   * @param datasetName
   *     name of dataset
   * @param inputParameters
   *     numeric input parameters that influence the applications runtime
   * @param labelName
   *     name of label which represents the class attribute of each instance.
   *
   * @return
   */
  private Instances createDataset(int size, String datasetName, List<InputParameter> inputParameters,
      String labelName) {
    ArrayList<Attribute> attributeList = new ArrayList<Attribute>();
    for (InputParameter inputParameter : inputParameters) {
      attributeList.add(new Attribute(inputParameter.getName()));
    }
    Attribute labelAttribute = new Attribute(labelName);
    attributeList.add(labelAttribute);
    Instances dataset = new Instances(datasetName, attributeList, size);
    dataset.setClass(labelAttribute);
    dataset.setRelationName(datasetName);
    return dataset;
  }

  /**
   * Adds instance to dataset.
   *
   * @param dataset
   *     set of instances with same attributes
   * @param instance
   *     data tuple that has multiple attributes and values
   */
  private void addInstance(Instances dataset, Instance instance) {
    dataset.add(instance);
  }

  /**
   * Creates and adds size-many equidistant instances to given dataset. Each attribute has at most
   * size^(1/#inputParameters) different values over all instances. If size is smaller than full combination of all
   * parameter value configurations, the combinations of the highest values are missing.
   * <p>
   * Example for size=1000 and 3 input parameters: <ul> <li>Param 0: same value on consecutive 100 steps -> step-index =
   * (instance-index mod 1000)/100</li> <li>Param 1: same value on consecutive 10 steps every 100 steps-> step-index =
   * (instance-index mod 100)/10</li> <li>Param 1: same value on only 1 steps every 10 steps-> value-index = (step-index
   * mod 10)/1</li> </ul>
   *
   * @param dataset
   *     set of instances with same attributes
   * @param size
   *     number of entries in dataset
   */
  private void createEquidistantInputInstances(Instances dataset, int size) {
    int totalSteps = (int) Math.round(Math.pow(size, 1.0 / (getNumberOfParameters())));
    System.out.println("Total steps: " + totalSteps);
    for (int i = 0; i < size; i++) {
      Instance instance = new DenseInstance(getNumberOfParameters() + 1);
      instance.setDataset(dataset);
      for (int inputParam = 0; inputParam < getNumberOfParameters(); inputParam++) {
        int div = (int) Math.pow(totalSteps, getNumberOfParameters() - inputParam - 1);
        int mod = div * totalSteps;
        int stepIndex = (i % mod) / div;
        instance.setValue(inputParam, getInputParameters().get(inputParam).getValueAt(stepIndex, totalSteps));
      }
      dataset.add(instance);
    }
    System.out.println("All input instances created.");
  }

  /**
   * Adds runtime for instance parameter configuration as class value to each instance in dataset.
   *
   * @param dataset
   *     set of instances with same attributes
   */
  private void fillDatasetWithClassValues(Instances dataset) {
    //Shuffeln damit kein Bias gegen Ende hin kommt
    dataset.randomize(new Random());

    //THROW AWAY 1% OF VALUES (just in time compilation -> Java optimiert Schleife wenn er die ne Zeit lang gemacht hat)
    int dummyCounter = (int) (dataset.size() * .01);
    for (Instance instance : dataset) {
      instance.setClassValue(getRuntime(instance));
      dummyCounter--;
      System.out.print("+");
      if (dummyCounter == 0) {
        System.out.println("\n Dummy meassurement finished. Will start real computation now.");
        break;
      }
    }
    int instanceCounter = 0;
    for (Instance instance : dataset) {
      instance.setClassValue(getRuntime(instance));
      instanceCounter++;
      if (instanceCounter % 10 == 0) {
        System.out.println("Meassured instances: " + instanceCounter + "/" + dataset.size());
      }
    }
  }

  /**
   * @param instance
   *     data tuple that has multiple attributes and values
   *
   * @return runtime of the black-box method for the instances input parameter configutation as double value.
   */
  public abstract double getRuntime(Instance instance);

  /**
   * @return a list of all input parameters
   */
  public List<InputParameter> getInputParameters() {
    return inputParameters;
  }

  /**
   * @return the name of dataset
   */
  public String getDatasetName() {
    return datasetName;
  }

  /**
   * @return the number of input parameters
   */
  public int getNumberOfParameters() {
    return numberOfParameters;
  }

  /**
   * @return the number of input parameters
   */
  public boolean hasNominal() {
    return hasNominal;
  }

  /**
   * Sets input parameters.
   *
   * @param inputParameters
   *     numeric input parameters that influence the applications runtime
   */
  public void setInputParameters(List<InputParameter> inputParameters) {
    for (InputParameter inputParameter : inputParameters) {
      if (inputParameter.isNominal) {
        this.hasNominal = true;
      }
    }
    this.inputParameters = inputParameters;
  }
}

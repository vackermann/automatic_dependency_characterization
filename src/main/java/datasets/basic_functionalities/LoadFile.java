package datasets.basic_functionalities;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import datasets.DatasetCreator;
import datasets.InputParameter;
import weka.core.Instance;

import static org.boon.core.Typ.file;

/**
 * Created by Vanessa Ackermann on 19.03.18.
 *
 * @author Vanessa Ackermann
 * @version 1.0
 */
public class LoadFile extends DatasetCreator {

  private static final String DATASETNAME = "LoadFile";
  private static final int NUMBEROFPARAMETERS = 1;

  public LoadFile() {
    super(DATASETNAME, NUMBEROFPARAMETERS);
    List<InputParameter> inputParameters = new LinkedList<InputParameter>();
    inputParameters.add(new InputParameter("FileSize(kB)", 1, 1024, true, false));
    this.setInputParameters(inputParameters);
  }

  public double getRuntime(Instance instance) {
    int size = (int) instance.value(0);
    createFileOfSize(size);

    long startTime = System.nanoTime();
    File file = new File("data/randomAccessFile.txt");
    Path fromFile = file.toPath();
    byte[] fileArray;
    try {
      fileArray = Files.readAllBytes(fromFile);
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    long stopTime = System.nanoTime();
    return (stopTime - startTime);
  }

  public void createFileOfSize(int size) {
    File file = new File("data/randomAccessFile.txt");
    try {
      RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
      randomAccessFile.setLength(size * 1024);
      randomAccessFile.close();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

}

package datasets;

import java.io.File;

import datasets.basic_functionalities.SortArray;

public class Main {

  public static void main(String[] args) {
    SortArray a1 = new SortArray();
    a1.createDatasetFile(10000);
  }
}

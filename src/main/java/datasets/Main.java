package datasets;

import datasets.basic_functionalities.FilterArray;
import datasets.basic_functionalities.SearchArray;
import datasets.basic_functionalities.SortArray;
import datasets.encryption.RSADecryption;
import datasets.encryption.RSAEncryption;
import datasets.encryption.SHAHashing;
import datasets.image_processing.BlurImage;
import datasets.image_processing.CropImage;
import datasets.image_processing.HistogramEquilization;
import datasets.image_processing.RGBFilter;
import datasets.image_processing.ScaleImage;
import datasets.math_functions.AckermannFunction;
import datasets.math_functions.Fibonacci;
import datasets.math_functions.SubsetSum;

public class Main {

  public static void main(String[] args) {

    // Java functionalities
    SortArray a1 = new SortArray();
    SearchArray a2 = new SearchArray();
    FilterArray a3 = new FilterArray();

    // Math functions
    AckermannFunction m1 = new AckermannFunction();
    Fibonacci m2 = new Fibonacci();
    SubsetSum m3 = new SubsetSum();

    //Image Processing
    RGBFilter i1 = new RGBFilter();
    CropImage i2 = new CropImage();
    ScaleImage i3 = new ScaleImage();
    BlurImage i4 = new BlurImage();
    HistogramEquilization i5 = new HistogramEquilization();

    //Encryption
    SHAHashing e1 = new SHAHashing();
    RSAEncryption e2 = new RSAEncryption();
    RSADecryption e3 = new RSADecryption();

    //e1.createDatasetFile(100);
    //e2.createDatasetFile(100);
    //e3.createDatasetFile(100);
  }
}

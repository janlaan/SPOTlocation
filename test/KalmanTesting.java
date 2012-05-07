import java.io.*;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Arrays;


public class KalmanTesting {
  
  private static ArrayList<ArrayList<ArrayList<Double>>> data;
  private static ArrayList<ArrayList<Integer>> rssi;
  private static int num_nodes = 0;

  static void getTestData() {

    String filename = "testdata2";
    
    String[] keys = new String[10];
    
    data = new ArrayList<ArrayList<ArrayList<Double>>>();
    rssi = new ArrayList<ArrayList<Integer>>();

    Scanner scanner = null;
    try {
      scanner = new Scanner(new FileInputStream(filename));
    } catch(FileNotFoundException e) {
      System.err.println("input file not found!");
      System.exit(0);
    }
    
    while(scanner.hasNextLine()) {
      String line = scanner.nextLine();
      String[] linesplit = line.split(",");
      int idx = -1;
      if(Arrays.asList(keys).contains(linesplit[0])) {
        idx = Arrays.asList(keys).indexOf(linesplit[0]);
      }
      else {
        idx = num_nodes++;
        keys[idx] = linesplit[0];
        data.add(new ArrayList<ArrayList<Double>>());
        rssi.add(new ArrayList<Integer>());
      }
      ArrayList<Double> dt = new ArrayList<Double>();
      dt.add(Double.parseDouble(linesplit[1]));
      dt.add(Double.parseDouble(linesplit[2]));
      dt.add(Double.parseDouble(linesplit[3]));
      rssi.get(idx).add(Integer.parseInt(linesplit[4]));
      data.get(idx).add(dt);
    }
  }

  static void runSimul() {

    Double[][] accel = new Double[10][3];
    Double[][] pos = new Double[10][3];
    
    for(int i = 0; i < data.get(0).size(); i++) {
      Double[] reading0 = new Double[data.get(0).get(i).size()];
      data.get(0).get(i).toArray(reading0);
      Double[] reading1 = new Double[data.get(0).get(i).size()];
      data.get(1).get(i).toArray(reading1);
      int rssi0 = rssi.get(0).get(i);
      int rssi1 = rssi.get(1).get(i);

      
    }
  }

  public static void main(String[] args) {
    getTestData();
    runSimul();
  }
}